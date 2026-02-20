/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.widgetIsEnabled;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
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
 * Test class to test the Flash process
 *
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NewEspressifIDFProjectFlashProcessTest {
	@BeforeClass
	public static void beforeTestClass() throws Exception {
		Fixture.loadEnv();
	}

	@AfterClass
	public static void tearDown() {
		Fixture.cleanupEnvironment();
	}

	@Test
	public void givenNewProjectCreatedBuiltWhenSelectSerialPortWhenFlashThenCheckFlashedSuccessfully()
			throws Exception {
		if (SystemUtils.IS_OS_LINUX) // temporary solution until new ESP boards arrive for Windows
		{
			Fixture.givenNewEspressifIDFProjectIsSelected("EspressIf", "Espressif IDF Project");
			Fixture.givenProjectNameIs("NewProjectFlashTest");
			Fixture.whenNewProjectIsSelected();
			Fixture.whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig();
			Fixture.whenBuildAndFlashForAllTargetsSequentially();
		} else {
			assertTrue(true);
		}
	}

	private static class Fixture {
		private static SWTWorkbenchBot bot;
		private static String category;
		private static String subCategory;
		private static String projectName;

		private static final TargetPort[] TARGETS = new TargetPort[] {
				new TargetPort("esp32", "/dev/ttyUSB1 Dual RS232-HS"),
//				new TargetPort("esp32s2", "<PORT>"),
//				new TargetPort("esp32s3", "<PORT>"),
//				new TargetPort("esp32h2", "<PORT>"),
//				new TargetPort("esp32c5", "<PORT>"),
//				new TargetPort("esp32c6", "<PORT>"),
//				new TargetPort("esp32p4", "<PORT>")
		};

		private static void loadEnv() throws Exception {
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void givenNewEspressifIDFProjectIsSelected(String category, String subCategory) {
			Fixture.category = category;
			Fixture.subCategory = subCategory;
		}

		private static void givenProjectNameIs(String projectName) {
			Fixture.projectName = projectName;
		}

		private static void whenNewProjectIsSelected() throws Exception {
			ProjectTestOperations.setupProject(projectName, category, subCategory, bot);
		}

		private static void whenBuildAndFlashForAllTargetsSequentially() throws Exception {
			for (int i = 0; i < TARGETS.length; i++) {
				TargetPort tp = TARGETS[i];

				boolean skipTargetChangeDialog = (i == 0);
				whenChangeLaunchTarget(tp.target, skipTargetChangeDialog);

				whenProjectIsBuiltUsingContextMenu();
				whenSelectLaunchTargetSerialPort(tp.port);
				whenFlashProject();
				thenVerifyFlashDoneSuccessfully();

				TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
				bot.sleep(500);
			}
		}

		private static void whenChangeLaunchTarget(String targetText, boolean skipDialogOnThisRun) throws Exception {
			// skip first iteration since esp32 target was selected during Project Creation
			if (!skipDialogOnThisRun) {
				LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
				targetSelector.selectTarget(targetText);
				TestWidgetWaitUtility.waitForDialogToAppear(bot, "IDF Launch Target Changed", 20000);
				SWTBotShell shell = bot.shell("IDF Launch Target Changed");
				shell.setFocus();
				bot.button("Yes").click();
			}

			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenProjectIsBuiltUsingContextMenu() throws IOException {
			ProjectTestOperations.buildProjectUsingContextMenu(projectName, bot);
			ProjectTestOperations.waitForProjectBuild(bot);
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		}

		private static void whenSelectLaunchTargetSerialPort(String portPrefixOrExact) throws Exception {
			LaunchBarTargetSelector targetSelector = new LaunchBarTargetSelector(bot);
			targetSelector.clickEdit();

			TestWidgetWaitUtility.waitForDialogToAppear(bot, "New ESP Target", 20000);
			SWTBotShell shell = bot.shell("New ESP Target");
			try {
				bot.comboBoxWithLabel("Serial Port:").setSelection(portPrefixOrExact);
			} catch (Exception ignored) {
				String[] items = bot.comboBoxWithLabel("Serial Port:").items();
				String match = null;

				for (String item : items) {
					if (item != null && item.startsWith(portPrefixOrExact)) {
						match = item;
						break;
					}
				}

				if (match == null) {
					throw new AssertionError("No serial port matched: " + portPrefixOrExact + " ; available="
							+ String.join(", ", items));
				}

				bot.comboBoxWithLabel("Serial Port:").setSelection(match);
			}

			TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
			shell.setFocus();
			bot.button("Finish").click();
		}

		private static void whenTurnOffOpenSerialMonitorAfterFlashingInLaunchConfig() throws Exception {
			LaunchBarConfigSelector configSelector = new LaunchBarConfigSelector(bot);
			configSelector.clickEdit();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit Configuration", 20000);
			bot.cTabItem("Main").show();
			bot.cTabItem("Main").setFocus();
			SWTBotCheckBox checkBox = bot.checkBox("Open Serial Monitor After Flashing");
			if (checkBox.isChecked()) {
				checkBox.click();
			}
			bot.button("OK").click();
		}

		private static void whenFlashProject() throws IOException {
			ProjectTestOperations.launchCommandUsingContextMenu(projectName, bot, "Run Configurations...");
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Run Configurations", 10000);
			bot.tree().getTreeItem("ESP-IDF Application").select();
			bot.tree().getTreeItem("ESP-IDF Application").expand();
			bot.tree().getTreeItem("ESP-IDF Application").getNode(projectName).select();
			bot.waitUntil(widgetIsEnabled(bot.button("Run")), 5000);
			bot.button("Run").click();
		}

		private static void thenVerifyFlashDoneSuccessfully() throws Exception {
			ProjectTestOperations.waitForProjectFlash(bot);
		}

		static void cleanupEnvironment() {
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static class TargetPort {
			final String target;
			final String port;

			TargetPort(String target, String port) {
				this.target = target;
				this.port = port;
			}
		}
	}
}