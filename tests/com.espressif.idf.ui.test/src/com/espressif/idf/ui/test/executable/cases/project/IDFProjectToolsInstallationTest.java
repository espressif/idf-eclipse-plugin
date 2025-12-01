package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
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
	public void givenNewEnvironmentThenVerifyToolsPaths() throws Exception {
		Fixture.verifyToolsPaths();
	}

	private static class Fixture {
		private static SWTWorkbenchBot bot;
		static String home = System.getProperty("user.home").replace("\\", "/");

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

		private static void verifyToolsPaths() {
			idfToolsPath();
			idfPath();
			openOCDScripts();
			pythonExePath();
		}

		private static void idfToolsPath() {
			String actualPath = IDFUtil.getIDFToolsPath();
			Path idfToolsPath = Paths.get(System.getProperty("user.home"), ".espressif", IDFConstants.TOOLS_FOLDER);
			assertTrue("IDF_TOOLS_PATH mismatch" + actualPath, actualPath.equals(idfToolsPath.toString()));
		}

		private static void idfPath() {
			String actualPath = IDFUtil.getIDFPath();
			String pattern = "glob:" + home + "/.espressif" + "/*/esp-idf";
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("IDF_PATH mismatch" + actualPath, matcher.matches(Paths.get(actualPath)));
		}

		private static void pythonExePath() {
			String actualPath = IDFUtil.getPythonExecutable();
			String pattern = "glob:" + home + "/.espressif/" + IDFConstants.TOOLS_FOLDER + "/python/*/venv/bin/python";
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("PYTHON_EXE_PATH mismatch " + actualPath, matcher.matches(Paths.get(actualPath)));
		}

		private static void openOCDScripts() {
			bot.table().select("OPENOCD_SCRIPTS");
			bot.table().getTableItem("OPENOCD_SCRIPTS").doubleClick();
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Edit variable", 5000);

			String actualPath = IDFUtil.getOpenOCDLocation();

			String pattern = "glob:" + home + "/.espressif/" + IDFConstants.TOOLS_FOLDER
					+ "/openocd-esp32/*/openocd-esp32/share/openocd/scripts";

			PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
			assertTrue("OPENOCD_SCRIPTS mismatch" + actualPath, matcher.matches(Paths.get(actualPath)));
		}
	}
}
