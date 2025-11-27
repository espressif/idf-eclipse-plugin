package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
		}

		private static void idfToolsPath() {
			bot.table().select("IDF_TOOLS_PATH");
			bot.table().getTableItem("IDF_TOOLS_PATH").doubleClick();
			bot.sleep(1000);

			Path idfToolsPath = Paths.get(System.getProperty("user.home"), ".espressif", IDFConstants.TOOLS_FOLDER);
			String actualPath = bot.textWithLabel("Value:").getText();
			assertTrue(actualPath.equals(idfToolsPath.toString()));
		}
	}
}
