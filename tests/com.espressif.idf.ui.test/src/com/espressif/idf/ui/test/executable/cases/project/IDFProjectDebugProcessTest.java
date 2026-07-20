/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarConfigSelector;
import com.espressif.idf.ui.test.operations.selectors.LaunchBarTargetSelector;

/**
 * Hardware E2E test: create → build → UART flash (ESP32) → switch to debug config with
 * ESP32-ETHERNET-KIT → start debugging and verify the session.
 * <p>
 * Mirrors the VS Code hardware debug flow from {@code project-hardware-e2e-test.ts}.
 *
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IDFProjectDebugProcessTest
{
	private static final String PROJECT_NAME = "NewProjectDebugProcessTest";
	private static final String ESP32_TARGET = "esp32";
	private static final String ETHERNET_KIT_BOARD_PREFIX = "ESP32-ETHERNET-KIT";
	private static final Pattern DEBUG_FATAL_ERROR_PATTERN = Pattern.compile(
			"Target failure|Error: .*failed to halt|OpenOCD failed|LIBUSB_ERROR|failed to connect",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern[] TARGET_DETECTION_PATTERNS = new Pattern[] {
			Pattern.compile("Connected to\\s+(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE),
			Pattern.compile("Chip type:\\s*(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE),
			Pattern.compile("Detecting chip type\\.\\.\\.\\s*(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE)
	};

	@BeforeClass
	public static void beforeTestClass() throws Exception
	{
		Fixture.loadEnv();
	}

	@AfterClass
	public static void tearDown()
	{
		Fixture.cleanupEnvironment();
	}

	@After
	public void afterEachTest()
	{
		// Always stop OpenOCD/GDB even when an assertion failed mid-test.
		Fixture.stopDebugSessionAndKillProcesses();
	}

	@Test
	public void givenNewProjectBuiltAndFlashedViaUartWhenDebugWithEthernetKitThenDebugSessionStarts()
			throws Exception
	{
		assumeTrue("Linux only: hardware debug test requires Linux CI/lab boards", SystemUtils.IS_OS_LINUX);

		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs(PROJECT_NAME);
		Fixture.whenNewProjectIsSelected();
		Fixture.whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig();

		String esp32SerialPort = Fixture.whenDetectAndSelectEsp32UartSerialPort();
		assumeTrue("Skipping debug test: no ESP32 UART target detected from Serial Port auto-detection",
				esp32SerialPort != null);

		Fixture.whenProjectIsBuiltUsingContextMenu();
		Fixture.whenFlashProject();
		Fixture.thenVerifyFlashDoneSuccessfully();

		assumeTrue("Skipping debug test: ESP32-ETHERNET-KIT board not detected",
				Fixture.whenSelectEsp32EthernetKitBoard());

		// Start debug only via Debug As — do not flip Launch Bar mode/config first.
		// LaunchBarListener toggles RUN↔DEBUG on descriptor changes and can terminate
		// an active OpenOCD session when the Debug perspective opens.
		Fixture.whenStartDebuggingUsingContextMenu();
		Fixture.thenVerifyDebugSessionStarted();
		Fixture.thenVerifyNoFatalOpenOcdErrors();
		Fixture.whenStepOver();
		Fixture.thenVerifyDebugSessionStillActive();
		Fixture.whenStopDebugging();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;

		private static void loadEnv() throws Exception
		{
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void givenNewEspressifIDFProjectIsSelected(String category, String subCategory)
		{
			Fixture.category = category;
			Fixture.subCategory = subCategory;
		}

		private static void givenProjectNameIs(String projectName)
		{
			Fixture.projectName = projectName;
		}

		private static void whenNewProjectIsSelected() throws Exception
		{
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig() throws Exception
		{
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.clickEdit();

			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit Configuration", 20000);

			bot.cTabItem("Main").activate();

			SWTBotCheckBox checkBox = bot.checkBox("Open Serial Monitor After Flashing");
			if (checkBox.isChecked())
			{
				checkBox.click();
			}

			bot.button("OK").click();
		}

		/**
		 * Opens New ESP Target, scans serial ports with detailed output, and stops as soon as
		 * an esp32 chip is detected. Finishes the dialog with that port selected.
		 *
		 * @return the selected ESP32 serial port, or {@code null} if none was found
		 */
		private static String whenDetectAndSelectEsp32UartSerialPort() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();

			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);

			SWTBotShell shell = bot.shell("New ESP Target");
			shell.setFocus();

			SWTBotCheckBox detailedOutput = bot.checkBox("Enable detailed output");
			if (!detailedOutput.isChecked())
			{
				detailedOutput.click();
			}

			SWTBotCombo serialPortCombo = bot.comboBoxWithLabel("Serial Port:");
			String[] serialPorts = serialPortCombo.items();

			for (String serialPort : serialPorts)
			{
				if (serialPort == null || serialPort.trim().isEmpty())
				{
					continue;
				}

				System.out.println("Checking serial port: " + serialPort);

				String outputBeforeSelection = readTargetDetectionOutput();
				serialPortCombo.setSelection(serialPort);

				// Wait for target auto-detection output to be printed.
				bot.sleep(3000);

				String outputAfterSelection = readTargetDetectionOutput();
				String newOutput = getNewOutputPart(outputBeforeSelection, outputAfterSelection);
				String detectedTarget = extractTargetFromDetectionOutput(newOutput);

				if (detectedTarget == null || detectedTarget.trim().isEmpty())
				{
					System.out.println("No ESP target detected for serial port: " + serialPort);
					continue;
				}

				System.out.println("Detected ESP target: " + detectedTarget + " on port: " + serialPort);

				if (ESP32_TARGET.equals(detectedTarget))
				{
					System.out.println("ESP32 UART port found — stopping discovery and applying: " + serialPort);
					TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
					shell.setFocus();
					bot.button("Finish").click();
					TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
					return serialPort;
				}
			}

			System.out.println("No esp32 target among detected ports");
			bot.button("Cancel").click();
			return null;
		}

		/**
		 * Selects an ESP32-ETHERNET-KIT board entry from the New ESP Target Board combo.
		 *
		 * @return true if a matching board was found and selected
		 */
		private static boolean whenSelectEsp32EthernetKitBoard() throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();

			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);

			SWTBotShell shell = bot.shell("New ESP Target");
			shell.setFocus();

			// Ensure IDF target is esp32 so Ethernet Kit boards are listed.
			try
			{
				bot.comboBoxWithLabel("IDF Target").setSelection(ESP32_TARGET);
				bot.sleep(2000);
			}
			catch (WidgetNotFoundException ignored)
			{
				// Label text may differ slightly across versions; Board combo is still attempted.
			}

			SWTBotCombo boardCombo = bot.comboBoxWithLabel("Board:");
			String[] boards = boardCombo.items();
			String match = null;

			for (String board : boards)
			{
				if (board != null && board.startsWith(ETHERNET_KIT_BOARD_PREFIX))
				{
					match = board;
					break;
				}
			}

			if (match == null)
			{
				System.out.println("ESP32-ETHERNET-KIT not found in Board combo. Available: "
						+ String.join(", ", boards));
				bot.button("Cancel").click();
				return false;
			}

			System.out.println("Selecting board: " + match);
			boardCombo.setSelection(match);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			shell.setFocus();
			bot.button("Finish").click();
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			return true;
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenFlashProject() throws IOException
		{
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Run Configurations...");

			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Run Configurations", 10000);

			bot.tree().getTreeItem("ESP-IDF Application").select();
			bot.tree().getTreeItem("ESP-IDF Application").expand();
			bot.tree().getTreeItem("ESP-IDF Application").getNode(projectName).select();

			bot.waitUntil(widgetIsEnabled(bot.button("Run")), 5000);
			bot.button("Run").click();
		}

		private static void thenVerifyFlashDoneSuccessfully() throws Exception
		{
			ProjectTestOperations.waitForProjectFlash(bot);
		}

		private static void whenStartDebuggingUsingContextMenu()
		{
			ProjectTestOperations.startDebuggingUsingContextMenu(projectName, bot);
		}

		private static void thenVerifyDebugSessionStarted() throws Exception
		{
			ProjectTestOperations.waitForDebugSessionStarted(bot);
		}

		private static void thenVerifyNoFatalOpenOcdErrors()
		{
			String consoleText = ProjectTestOperations.readDebugRelatedConsoleText(bot);
			assertFalse("Fatal OpenOCD error detected during debug session.\nConsole:\n" + consoleText,
					DEBUG_FATAL_ERROR_PATTERN.matcher(consoleText).find());
			assertFalse("Debug session already shut down before assertions.\nConsole:\n" + consoleText,
					consoleText.contains("shutdown command invoked")
							|| consoleText.contains("dropped 'gdb' connection"));
			assertTrue("Expected an active debug launch after suspend", ProjectTestOperations.hasActiveLaunch());
		}

		private static void whenStepOver()
		{
			ProjectTestOperations.waitForDebugStepActionsAvailable(bot, 15000);
			try
			{
				bot.toolbarButtonWithTooltip("Step Over (F6)").click();
				bot.sleep(3000);
			}
			catch (WidgetNotFoundException e)
			{
				bot.toolbarButtonWithTooltip("Step Over").click();
				bot.sleep(3000);
			}
		}

		private static void thenVerifyDebugSessionStillActive()
		{
			String consoleText = ProjectTestOperations.readDebugRelatedConsoleText(bot);
			assertFalse("Fatal OpenOCD error after Step Over.\nConsole:\n" + consoleText,
					DEBUG_FATAL_ERROR_PATTERN.matcher(consoleText).find());
			assertFalse("Debug session terminated unexpectedly after Step Over.\nConsole:\n" + consoleText,
					consoleText.contains("shutdown command invoked"));
			assertTrue("Debug launch terminated unexpectedly after Step Over",
					ProjectTestOperations.hasActiveLaunch());
		}

		private static void whenStopDebugging()
		{
			stopDebugSessionAndKillProcesses();
			bot.sleep(2000);
		}

		private static void stopDebugSessionAndKillProcesses()
		{
			ProjectTestOperations.stopDebugSessionAndKillProcesses(bot);
		}

		private static void cleanupEnvironment()
		{
			try
			{
				stopDebugSessionAndKillProcesses();
			}
			catch (Exception ignored)
			{
			}

			try
			{
				ProjectTestOperations.closeAllProjects(bot);
				ProjectTestOperations.deleteAllProjects(bot);
			}
			catch (Exception ignored)
			{
			}
			finally
			{
				ProjectTestOperations.killDebugProcesses();
			}
		}

		private static String getNewOutputPart(String outputBeforeSelection, String outputAfterSelection)
		{
			if (outputAfterSelection == null)
			{
				return "";
			}
			if (outputBeforeSelection == null || outputBeforeSelection.isEmpty())
			{
				return outputAfterSelection;
			}
			if (outputAfterSelection.startsWith(outputBeforeSelection))
			{
				return outputAfterSelection.substring(outputBeforeSelection.length());
			}
			return outputAfterSelection;
		}

		private static String readTargetDetectionOutput()
		{
			try
			{
				return bot.styledText().getText();
			}
			catch (Exception ignored)
			{
			}

			String bestCandidate = "";
			for (int i = 0; i < 10; i++)
			{
				try
				{
					String text = bot.text(i).getText();
					if (text != null && containsChipInfo(text))
					{
						return text;
					}
					if (text != null && text.length() > bestCandidate.length())
					{
						bestCandidate = text;
					}
				}
				catch (Exception ignored)
				{
					break;
				}
			}
			return bestCandidate == null ? "" : bestCandidate;
		}

		private static boolean containsChipInfo(String text)
		{
			return text != null && (text.contains("Connected to ESP32") || text.contains("Chip type:")
					|| text.contains("Detecting chip type"));
		}

		private static String extractTargetFromDetectionOutput(String output)
		{
			if (output == null || output.trim().isEmpty())
			{
				return null;
			}
			for (Pattern pattern : TARGET_DETECTION_PATTERNS)
			{
				Matcher matcher = pattern.matcher(output);
				if (matcher.find())
				{
					return normalizeDetectedChipToIdfTarget(matcher.group(1));
				}
			}
			return null;
		}

		private static String normalizeDetectedChipToIdfTarget(String chipName)
		{
			if (chipName == null)
			{
				return null;
			}
			String chip = chipName.trim().toUpperCase(Locale.ROOT);
			if (chip.startsWith("ESP32-C61"))
			{
				return "esp32c61";
			}
			if (chip.startsWith("ESP32-C6"))
			{
				return "esp32c6";
			}
			if (chip.startsWith("ESP32-C5"))
			{
				return "esp32c5";
			}
			if (chip.startsWith("ESP32-H2"))
			{
				return "esp32h2";
			}
			if (chip.startsWith("ESP32-S3"))
			{
				return "esp32s3";
			}
			if (chip.startsWith("ESP32-S2"))
			{
				return "esp32s2";
			}
			if (chip.startsWith("ESP32"))
			{
				return "esp32";
			}
			return null;
		}
	}
}
