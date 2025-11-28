package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.IDFConstants;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;

/**
 * Test class to verify tools installation
 * 
 * @author Andrii Filippov
 *
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IDFProjectToolsInstallationTest {
	@BeforeClass
	public static void beforeTestClass() throws Exception {
		Fixture.loadEnv();
	}

	@After
	public void afterEachTest() {
		Fixture.cleanTestEnv();
	}

	@Test
	public void givenNewEnvironmentWhenOpenSbomThenSbomIsDisabled() throws Exception {
		Fixture.openPreferencesBuildEnvVariables();
		Fixture.verifyToolsPaths();
	}

	private static class Fixture {
		private static SWTWorkbenchBot bot;

		private static void loadEnv() throws Exception {
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
			bot.sleep(1000);
		}

		private static void cleanTestEnv() {
			TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}

		private static void openPreferencesBuildEnvVariables() throws Exception {
			bot.menu("Window").menu("Preferences...").click();
			SWTBotShell preferencesShell = bot.shell("Preferences");
			preferencesShell.bot().tree().getTreeItem("C/C++").select();
			preferencesShell.bot().tree().getTreeItem("C/C++").expand();
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").select();
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").expand();
			preferencesShell.bot().tree().getTreeItem("C/C++").getNode("Build").getNode("Environment").select();
		}

		private static void verifyToolsPaths() {
			idfToolsPath();
			closeDialog();
			idfPath();
			closeDialog();
			openOCDScripts();
			closeDialog();
			idfPythonEnvPath();
			closeDialog();
			pythonExePath();
			closeDialog();
			cmakeAndNinjaPath();
			closeDialog();
		}

		private static void idfToolsPath() {
			bot.table().select("IDF_TOOLS_PATH");
			bot.table().getTableItem("IDF_TOOLS_PATH").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			Path idfToolsPath = Paths.get(System.getProperty("user.home"), ".espressif", IDFConstants.TOOLS_FOLDER);
			String actualPath = bot.textWithLabel("Value:").getText();
			assertTrue("IDF_TOOLS_PATH mismatch", actualPath.equals(idfToolsPath.toString()));
		}

		private static void idfPath() {
			bot.table().select("IDF_PATH");
			bot.table().getTableItem("IDF_PATH").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			Path idfPath = Paths.get(IDFUtil.getIDFPath());
			String actualPath = bot.textWithLabel("Value:").getText();
			assertTrue("IDF_PATH mismatch", actualPath.equals(idfPath.toString()));
		}

		private static void openOCDScripts() {
			bot.table().select("OPENOCD_SCRIPTS");
			bot.table().getTableItem("OPENOCD_SCRIPTS").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			String actualPath = bot.textWithLabel("Value:").getText();
			String home = System.getProperty("user.home").replace("\\", "/");
			String pattern = "glob:" + home + "/.espressif/" + IDFConstants.TOOLS_FOLDER
					+ "/openocd-esp32/*/openocd-esp32/share/openocd/scripts";

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("OPENOCD_SCRIPTS mismatch", matcher.matches(Paths.get(actualPath)));
		}

		private static void idfPythonEnvPath() {
			bot.table().select("IDF_PYTHON_ENV_PATH");
			bot.table().getTableItem("IDF_PYTHON_ENV_PATH").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			String actualPath = bot.textWithLabel("Value:").getText();
			String home = System.getProperty("user.home").replace("\\", "/");
			String pattern = "glob:" + home + "/.espressif/" + IDFConstants.TOOLS_FOLDER + "/python/*/venv";

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("IDF_PYTHON_ENV_PATH mismatch", matcher.matches(Paths.get(actualPath)));
		}

		private static void pythonExePath() {
			bot.table().select("PYTHON_EXE_PATH");
			bot.table().getTableItem("PYTHON_EXE_PATH").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			String actualPath = bot.textWithLabel("Value:").getText();
			String home = System.getProperty("user.home").replace("\\", "/");
			String pattern = "glob:" + home + "/.espressif/" + IDFConstants.TOOLS_FOLDER + "/python/*/venv/bin/python";

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("PYTHON_EXE_PATH mismatch", matcher.matches(Paths.get(actualPath)));
		}

		private static void cmakeAndNinjaPath() {
			bot.table().select("PATH");
			bot.table().getTableItem("PATH").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			String actualPath = bot.textWithLabel("Value:").getText();
			assertTrue("cmake not found in the PATH", actualPath.contains("cmake"));
			assertTrue("ninja not found in the PATH", actualPath.contains("ninja"));
		}

		private static void closeDialog() {
			bot.button("OK").click();
		}
	}
}
