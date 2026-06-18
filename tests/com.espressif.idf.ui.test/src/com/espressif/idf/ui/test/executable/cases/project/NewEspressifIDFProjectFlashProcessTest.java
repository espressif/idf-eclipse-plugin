/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
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
 * Test class to test the Flash process.
 *
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectFlashProcessTest
{
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

	@Test
	public void givenNewProjectCreatedBuiltWhenSelectSerialPortWhenFlashThenCheckFlashedSuccessfully() throws Exception
	{
//		assumeTrue("Linux only", SystemUtils.IS_OS_LINUX);

		Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
		Fixture.givenProjectNameIs("NewProjectFlashTest");
		Fixture.whenNewProjectIsSelected();
		Fixture.whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig();
		Fixture.whenBuildAndFlashForAllDetectedTargetsSequentially();
	}

	private static class Fixture
	{
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;

		private static final Pattern[] TARGET_DETECTION_PATTERNS = new Pattern[] {
				Pattern.compile("Connected to\\s+(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE),
				Pattern.compile("Chip type:\\s*(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE),
				Pattern.compile("Detecting chip type\\.\\.\\.\\s*(ESP32[-A-Z0-9]*)\\b", Pattern.CASE_INSENSITIVE)
		};

		private static void whenBuildAndFlashForAllDetectedTargetsSequentially() throws Exception
		{
			TargetPort[] detectedTargets = whenCollectDetectedTargetsFromNewEspTargetDialog();

			assumeFalse("Skipping hardware flash test: no ESP targets were detected from Serial Port auto-detection",
					detectedTargets.length == 0);

			whenBuildAndFlashCollectedTargetsSequentially(detectedTargets);
		}

		
		private static TargetPort[] whenCollectDetectedTargetsFromNewEspTargetDialog() throws Exception
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

			List<TargetPort> detectedTargets = new ArrayList<>();

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

				if (!isSupportedTarget(detectedTarget))
				{
					System.out.println("Ignoring unsupported ESP target: " + detectedTarget + " on port: " + serialPort);
					continue;
				}

				System.out.println("Detected ESP target: " + detectedTarget + " on port: " + serialPort);
				detectedTargets.add(new TargetPort(detectedTarget, serialPort));
			}

			bot.button("Cancel").click();

			List<TargetPort> uniqueTargets = keepFirstPortPerTarget(detectedTargets);
			List<TargetPort> orderedTargets = moveEsp32TargetFirst(uniqueTargets);

			TargetPort[] detectedTargetsArray = orderedTargets.toArray(new TargetPort[0]);
			return detectedTargetsArray;
		}

		private static void whenBuildAndFlashCollectedTargetsSequentially(TargetPort[] detectedTargets) throws Exception
		{
			String currentTarget = "esp32";
			boolean projectWasBuilt = false;

			for (int i = 0; i < detectedTargets.length; i++)
			{
				TargetPort tp = detectedTargets[i];

				System.out.println("Starting build/flash for target=" + tp.target + " port=" + tp.port);

				/*
				 * Project is created with esp32 as the default launch target.
				 * If the first detected target is esp32, do not try to select it again.
				 */
				if (!currentTarget.equals(tp.target))
				{
					whenChangeLaunchTarget(tp.target, projectWasBuilt);
					currentTarget = tp.target;
				}
				else
				{
					System.out.println("Launch target is already selected: " + tp.target);
				}

				whenProjectIsBuiltUsingContextMenu();
				projectWasBuilt = true;

				whenSelectLaunchTargetSerialPort(tp.port);
				whenFlashProject();
				thenVerifyFlashDoneSuccessfully();

				System.out.println("Finished build/flash for target=" + tp.target + " port=" + tp.port);

				TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
				bot.sleep(500);
			}
		}

		private static final List<String> SUPPORTED_TARGETS = Arrays.asList(
				"esp32",
				"esp32s2",
				"esp32s3",
				"esp32c5",
				"esp32c6",
				"esp32c61",
				"esp32h2");

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
				// Fallback to SWT Text widgets below.
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
			if (text == null)
			{
				return false;
			}

			return text.contains("Connected to ESP32")
					|| text.contains("Chip type:")
					|| text.contains("Detecting chip type");
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

		private static boolean isSupportedTarget(String target)
		{
			return target != null && SUPPORTED_TARGETS.contains(target);
		}

		private static List<TargetPort> keepFirstPortPerTarget(List<TargetPort> targets)
		{
			Map<String, TargetPort> uniqueTargets = new LinkedHashMap<>();

			for (TargetPort targetPort : targets)
			{
				uniqueTargets.putIfAbsent(targetPort.target, targetPort);
			}

			return new ArrayList<>(uniqueTargets.values());
		}

		private static void whenChangeLaunchTarget(String targetText, boolean expectTargetChangeDialog) throws Exception
		{
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.selectTarget(targetText);

			if (expectTargetChangeDialog)
			{
				TestWidgetWaitUtility.waitForDialogToAppear(bot, "IDF Launch Target Changed", 20000);

				SWTBotShell shell = bot.shell("IDF Launch Target Changed");
				shell.setFocus();

				bot.button("Yes").click();
			}

			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException
		{
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenSelectLaunchTargetSerialPort(String portPrefixOrExact) throws Exception
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

			try
			{
				serialPortCombo.setSelection(portPrefixOrExact);
			}
			catch (Exception ignored)
			{
				String[] items = serialPortCombo.items();
				String match = null;

				for (String item : items)
				{
					if (item != null && item.startsWith(portPrefixOrExact))
					{
						match = item;
						break;
					}
				}

				if (match == null)
				{
					throw new AssertionError("No serial port matched: " + portPrefixOrExact + " ; available="
							+ String.join(", ", items));
				}

				serialPortCombo.setSelection(match);
			}

			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);

			shell.setFocus();
			bot.button("Finish").click();

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

		static void cleanupEnvironment()
		{
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static class TargetPort
		{
			final String target;
			final String port;

			TargetPort(String target, String port)
			{
				this.target = target;
				this.port = port;
			}
		}

		private static List<TargetPort> moveEsp32TargetFirst(List<TargetPort> targets)
		{
			if (targets == null || targets.isEmpty())
			{
				return targets;
			}

			List<TargetPort> sortedTargets = new ArrayList<>();
			TargetPort esp32Target = null;

			for (TargetPort tp : targets)
			{
				if ("esp32".equals(tp.target))
				{
					esp32Target = tp;
					break;
				}
			}

			if (esp32Target != null)
			{
				sortedTargets.add(esp32Target);
			}

			for (TargetPort tp : targets)
			{
				if (!"esp32".equals(tp.target))
				{
					sortedTargets.add(tp);
				}
			}

			return sortedTargets;
		}
	}
}