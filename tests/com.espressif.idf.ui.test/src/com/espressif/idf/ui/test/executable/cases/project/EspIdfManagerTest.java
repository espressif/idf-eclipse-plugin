/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;

import java.util.List;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.EimIdfJsonPathResolver;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.eimjson.EimJsonVersion;
import com.espressif.idf.core.tools.eimjson.model.EimConfigModel;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;
import com.espressif.idf.core.tools.watcher.EimJsonWatchService;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.progress.FinishedJobs;

/**
 * SWTBot tests for the ESP-IDF Manager editor shell.
 *
 * @author Andrii Filippov
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EspIdfManagerTest {
	@BeforeClass
	public static void beforeClass() throws Exception {
		Fixture.loadEnv();
	}

	@AfterClass
	public static void afterClass()
	{
	    Fixture.clearFinishedProgressJobs();
	}

	@Before
	public void beforeEach() throws Exception {
		Fixture.captureMutableState();
	}

	@Test
	public void givenEspressifEnvIsConfiguredWhenOpeningEspIdfManagerFromMenuThenEditorShowsInstalledVersionsTable()
			throws Exception {
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenEditorShowsInstalledVersionsTable();
	}

	@Test
	public void givenEspIdfManagerIsOpenWhenEnvSetupCompletedThenActiveVersionIsListed() throws Exception {
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenActiveEspIdfVersionIsListed();
	}

	@Test
	public void givenActiveEspIdfWhenRefreshEnvironmentIsClickedThenToolsSetupCompletesAndActiveIdfDoesNotChange()
			throws Exception {
		Fixture.givenEspressifEnvIsConfigured();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		String activeLocationBeforeRefresh = Fixture.getActiveInstallationLocation();
		Fixture.whenRefreshEnvironmentIsClicked();
		Fixture.thenToolsSetupCompletesInConsole();
		Fixture.thenActiveIdfLocationIs(activeLocationBeforeRefresh);
	}

	@Test
	public void givenActiveEspIdfWhenBuildEnvironmentPreferencesAreOpenedThenIdfVariablesMatchActiveVersion()
			throws Exception {
	    Fixture.givenEspressifEnvIsConfigured();
	    Fixture.whenEspIdfManagerIsOpenedFromMenu();
	    Fixture.whenInstalledVersionsTableIsLoaded();
	    Fixture.whenRefreshEnvironmentIsClicked();
	    Fixture.thenToolsSetupCompletesInConsole();
	    Fixture.thenIdfEnvironmentVariablesMatchActiveVersion(); //?
	}

	@Test
	public void givenNoInstalledIdfWhenManagerIsOpenedThenEmptyStateIsShown() throws Exception {
		Path emptyConfig = Fixture.givenCustomEimConfigWithNoInstallations();
		Fixture.givenCustomEimJsonPath(emptyConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenManagerRefreshFinishes();
		Fixture.thenManagerShowsEmptyState();
	}

	@Test
	public void givenConfiguredIdfPathsWhenManagerIsOpenedThenConfiguredLocationsAreDisplayed() throws Exception {
		EimConfigModel configuredModel = Fixture.givenCurrentEimConfigurationIsLoaded();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenConfiguredIdfLocationsAreDisplayed(configuredModel);
	}

	@Test
	public void givenInstalledIdfIsNotActiveWhenActivateSelectedIsClickedThenIdfIsActivatedAndIdfPyReportsItsVersion()
			throws Exception {
		EimInstallationModel target = Fixture.givenCurrentIdfIsTemporarilyMarkedInactive();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.whenInstallationLocationIsSelected(target.getPath());
		Fixture.whenActivateSelectedIsClicked();
		Fixture.thenToolsSetupCompletesInConsole();
		Fixture.thenActiveIdfLocationIs(target.getPath());
		Fixture.thenIdfPyVersionMatchesActiveManagerVersion();
	}

	@Test
	public void givenActiveIdfWhenManagerIsClosedAndReopenedThenActivationPersists() throws Exception {
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		String expectedLocation = Fixture.getActiveInstallationLocation();
		String expectedVersion = Fixture.getActiveInstallationTableVersion();
		Fixture.closeEspIdfManagerIfOpen();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenActiveIdfLocationIs(expectedLocation);
		assertEquals("Active ESP-IDF version must persist after reopening the manager", expectedVersion,
				Fixture.getActiveInstallationTableVersion()); // $NON-NLS-1$
	}

	@Test
	public void givenNoTableSelectionWhenManagerIsOpenThenActivateSelectedIsDisabled() throws Exception {
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.whenManagerTableSelectionIsCleared();
		Fixture.thenActivateSelectedIsDisabled();
	}

	@Test
	public void givenCustomEimJsonPathWhenPreferenceIsAppliedThenManagerUsesCustomConfiguration() throws Exception {
		Path customConfig = Fixture.givenCustomEimConfigWithNoInstallations();
		Fixture.whenEspressifPreferencesAreOpened();
		Fixture.whenEimJsonPreferenceTextIsSet(customConfig.toString());
		Fixture.whenPreferencesAreAppliedAndClosed();
		Fixture.thenStoredEimJsonPreferenceIs(customConfig.toString());
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenManagerRefreshFinishes();
		Fixture.thenResolvedEimJsonPathIs(customConfig);
		Fixture.thenManagerShowsEmptyState();
	}

	@Test
	public void givenNonExistingCustomEimJsonPathWhenManagerIsOpenedThenDefaultConfigurationIsUsed() throws Exception {
		Path missingCustomConfig = Fixture.givenNonExistingCustomEimJsonPath();
		Fixture.whenEspressifPreferencesAreOpened();
		Fixture.whenEimJsonPreferenceTextIsSet(missingCustomConfig.toString());
		Fixture.whenPreferencesAreAppliedAndClosed();
		Fixture.thenStoredEimJsonPreferenceIs(missingCustomConfig.toString());
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenResolvedEimJsonPathIs(Fixture.getDefaultEimJsonPath());
		Fixture.thenEditorShowsInstalledVersionsTable();
	}

	@Test
	public void givenManagerUsesCustomConfigWhenInstalledIdfsChangeThenChangeIsDetected() throws Exception {
		Path customConfig = Fixture.givenCustomEimConfigCopiedFromCurrentConfiguration();
		Fixture.givenCustomEimJsonPath(customConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.whenCustomEimConfigIsReplacedWithEmptyConfiguration(customConfig);
		Fixture.thenEimInstallationChangedDialogIsShown();
		Fixture.whenEimInstallationUpdateIsDeclined();
	}

	@Test
	public void givenEmptyManagerWhenEimAddsInstallationsAndUpdateIsAcceptedThenManagerReloadsInstallations()
			throws Exception {
		Path customConfig = Fixture.givenCustomEimConfigWithNoInstallations();
		Fixture.givenCustomEimJsonPath(customConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenManagerRefreshFinishes();
		Fixture.thenManagerShowsEmptyState();
		Fixture.whenCustomEimConfigIsReplacedWithOriginalConfiguration(customConfig);
		Fixture.thenEimInstallationChangedDialogIsShown();
		Fixture.whenEimInstallationUpdateIsAccepted();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.thenEditorShowsInstalledVersionsTable();
	}

	@Test
	public void givenManagerListsInstallationsWhenEimChangesAndUpdateIsDeclinedThenCurrentTableIsKept()
			throws Exception {
		Path customConfig = Fixture.givenCustomEimConfigCopiedFromCurrentConfiguration();
		Fixture.givenCustomEimJsonPath(customConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		int rowCountBeforeChange = Fixture.getManagerTableRowCount();
		Fixture.whenCustomEimConfigIsReplacedWithEmptyConfiguration(customConfig);
		Fixture.thenEimInstallationChangedDialogIsShown();
		Fixture.whenEimInstallationUpdateIsDeclined();
		Fixture.thenManagerTableRowCountRemains(rowCountBeforeChange);
	}

	@Test
	public void givenDefaultEimIdfJsonIsMissingWhenManagerIsOpenedThenManagerRemainsUsable() throws Exception {
		Fixture.givenDefaultEimJsonPathIsConfigured();
		Fixture.givenDefaultEimJsonIsTemporarilyMissing();
		try {
			Fixture.whenEspIdfManagerIsOpenedFromMenu();
			Fixture.thenManagerRefreshFinishes();
			Fixture.thenManagerShowsEmptyState();
		} finally {
			Fixture.restoreDefaultEimJsonOrFail();
		}
	}

	@Test
	public void givenMalformedEimJsonWhenManagerIsOpenedThenManagerRemainsUsable() throws Exception {
		Path malformedConfig = Fixture.givenMalformedCustomEimConfig();
		Fixture.givenCustomEimJsonPath(malformedConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenManagerRefreshFinishes();
		Fixture.thenManagerShowsEmptyState();
	}

	@Test
	public void givenUnsupportedEimJsonVersionWhenManagerIsOpenedThenManagerRemainsUsable() throws Exception {
		Path unsupportedConfig = Fixture.givenUnsupportedVersionCustomEimConfig();
		Fixture.givenCustomEimJsonPath(unsupportedConfig);
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.thenManagerRefreshFinishes();
		Fixture.thenManagerShowsEmptyState();
	}

	@Test
	public void givenGuiCapableEimWhenManageEspIdfVersionsIsClickedThenGuiEimIsLaunched() throws Exception {
		Fixture.givenInstalledEimSupportsGuiMode();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.whenManageEspIdfVersionsIsClicked();
		Fixture.thenEimLaunchIsReportedInConsoleAndProcessIsStopped();
	}

	@Test
	public void givenCliOnlyEimWhenManageVersionsIsClickedThenEimCliTerminalIsOpened() throws Exception {
		Fixture.givenInstalledEimSupportsCliMode();
		Fixture.whenEspIdfManagerIsOpenedFromMenu();
		Fixture.whenInstalledVersionsTableIsLoaded();
		Fixture.whenManageEspIdfVersionsIsClicked();
		Fixture.thenEimCliTerminalIsOpenedAndClosed();
	}

	@After
	public void afterEach() throws Exception {
		Fixture.closeEimCliTerminalIfOpen();
		Fixture.closeEimChangeDialogIfOpen();
		Fixture.closePreferencesDialogIfOpen();
		Fixture.closeEspIdfManagerIfOpen();
		Fixture.restoreMutableState();
	}

	private static class Fixture {
		private static final String TOOLS_SETUP_COMPLETE = "Tools Setup complete"; //$NON-NLS-1$
		private static final String SETTING_UP_IDE_ENVIRONMENT = "Setting up IDE environment"; //$NON-NLS-1$
		private static final String EIM_LAUNCH_CONSOLE_MARKER = "Launched EIM application:"; //$NON-NLS-1$
		private static final String TERMINAL_VIEW_TITLE = "Terminal"; //$NON-NLS-1$
		private static final String WAIT_FOR_EIM_CLOSURE_JOB = "Wait for EIM Closure"; //$NON-NLS-1$
		private static final String EMPTY_EIM_CONFIG = "{\n  \"version\": \"" + EimJsonVersion.SUPPORTED_MAX + "\",\n  \"idfInstalled\": []\n}\n";
		private static final String MALFORMED_EIM_CONFIG = "{\n  \"version\": \"" + EimJsonVersion.SUPPORTED_MAX + "\",\n  \"idfInstalled\": [\n";
		private static final String UNSUPPORTED_EIM_CONFIG = "{\n  \"version\": \"999.0\",\n  \"idfInstalled\": []\n}\n"; //$NON-NLS-1$
		private static final long TABLE_LOAD_TIMEOUT_MS = 60_000L;
		private static final long CONSOLE_WAIT_TIMEOUT_MS = 60_000L;
		private static final long REFRESH_START_TIMEOUT_MS = 60_000L;
		private static final long DIALOG_WAIT_TIMEOUT_MS = 60_000L;

		private static SWTWorkbenchBot bot;
		private static Map<String, String> originalEnvironmentVariables;
		private static int eimLaunchMarkerCountBeforeAction;
		private static int eimCliOpeningCountBeforeAction;
		private static int eimCliCompletedCountBeforeAction;
		private static int terminalTabCountBeforeAction;
		private static String activeInstallationLocation;
		private static String activeInstallationTableVersion;
		private static String originalEimJsonPreference;
		private static Path originalResolvedEimJsonPath;
		private static Path tempDirectory;
		private static boolean eimPreferenceChanged;
		private static Path defaultEimJsonBackup;
		private static boolean defaultEimJsonTemporarilyMissing;
		private static boolean watcherPausedForMissingDefault;

		static void loadEnv() throws Exception {
			SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US"; //$NON-NLS-1$
			SWTBotPreferences.SCREENSHOTS_DIR = "screenshots/EspIdfManagerEditor/"; //$NON-NLS-1$
			bot = WorkBenchSWTBot.getBot();
			EnvSetupOperations.setupEspressifEnv(bot);
		}

		static void captureMutableState() throws Exception {
			IEclipsePreferences preferences = getCorePreferences();
			originalEimJsonPreference = preferences.get(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH, ""); //$NON-NLS-1$
			originalResolvedEimJsonPath = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
			IDFEnvironmentVariables environmentVariables = new IDFEnvironmentVariables();
			originalEnvironmentVariables = new HashMap<>(environmentVariables.getEnvMap());
			eimPreferenceChanged = false;
			defaultEimJsonBackup = null;
			defaultEimJsonTemporarilyMissing = false;
			watcherPausedForMissingDefault = false;
		}
		static void givenEspressifEnvIsConfigured() {
			bot.shell().activate();
		}

		static EimConfigModel givenCurrentEimConfigurationIsLoaded() throws Exception {
			EimConfigModel model = new EimIdfConfiguratinParser().getConfigModel(true);
			assertNotNull("Expected eim_idf.json to be readable", model); //$NON-NLS-1$
			assertTrue("Expected at least one configured ESP-IDF installation", !model.getInstallations().isEmpty()); //$NON-NLS-1$
			return model;
		}

		static Path givenCustomEimConfigWithNoInstallations() throws IOException {
			return writeTempEimConfig(EMPTY_EIM_CONFIG);
		}

		static Path givenCustomEimConfigCopiedFromCurrentConfiguration() throws IOException {
			assertTrue("The current eim_idf.json must exist before creating a test fixture", //$NON-NLS-1$
					Files.isRegularFile(originalResolvedEimJsonPath));
			Path target = getTempEimJsonPath();
			Files.copy(originalResolvedEimJsonPath, target, StandardCopyOption.REPLACE_EXISTING);
			return target;
		}

		static Path givenMalformedCustomEimConfig() throws IOException {
			return writeTempEimConfig(MALFORMED_EIM_CONFIG);
		}

		static Path givenUnsupportedVersionCustomEimConfig() throws IOException {
			return writeTempEimConfig(UNSUPPORTED_EIM_CONFIG);
		}

		static Path givenNonExistingCustomEimJsonPath() throws IOException {
			ensureTempDirectory();
			Path missingDirectory = tempDirectory.resolve("missing-custom-config"); //$NON-NLS-1$
			return missingDirectory.resolve(EimConstants.EIM_JSON);
		}

		static void givenDefaultEimJsonPathIsConfigured() throws Exception {
			IEclipsePreferences preferences = getCorePreferences();
			preferences.put(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH, ""); //$NON-NLS-1$
			preferences.flush();
			eimPreferenceChanged = true;
			EimJsonWatchService.restartAfterEimIdfPathChange();
		}

		static void givenDefaultEimJsonIsTemporarilyMissing() throws IOException {
			Path defaultEimJson = getDefaultEimJsonPath();
			assertTrue("Default eim_idf.json must exist before testing the missing-file state: " + defaultEimJson, //$NON-NLS-1$
					Files.isRegularFile(defaultEimJson));
			ensureTempDirectory();
			defaultEimJsonBackup = tempDirectory.resolve("default-eim-idf-backup.json"); //$NON-NLS-1$
			Files.copy(defaultEimJson, defaultEimJsonBackup, StandardCopyOption.REPLACE_EXISTING);

			EimJsonWatchService watcher = EimJsonWatchService.getInstance();
			if (watcher != null) {
				watcher.pauseListeners();
				watcherPausedForMissingDefault = true;
			}
			Files.delete(defaultEimJson);
			defaultEimJsonTemporarilyMissing = true;
		}

		static void givenCustomEimJsonPath(Path eimJsonPath) throws Exception {
			assertEquals("Custom EIM fixture must keep the required eim_idf.json file name", EimConstants.EIM_JSON, //$NON-NLS-1$
					eimJsonPath.getFileName().toString());
			assertTrue("Custom EIM fixture must exist before configuring the watcher", //$NON-NLS-1$
					Files.isRegularFile(eimJsonPath));
			IEclipsePreferences preferences = getCorePreferences();
			preferences.put(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH, eimJsonPath.toString());
			preferences.flush();
			eimPreferenceChanged = true;
			EimJsonWatchService.restartAfterEimIdfPathChange();
		}

		static EimInstallationModel givenCurrentIdfIsTemporarilyMarkedInactive() throws Exception {
			EimConfigModel model = givenCurrentEimConfigurationIsLoaded();
			IDFEnvironmentVariables environment = new IDFEnvironmentVariables();
			String activeId = environment.getEnvValue(IDFEnvironmentVariables.ESP_IDF_EIM_ID);
			assertTrue("ESP_IDF_EIM_ID must be configured before the activation test", //$NON-NLS-1$
					activeId != null && !activeId.isBlank());
			EimInstallationModel target = model.getInstallations().stream().filter(idf -> activeId.equals(idf.getId()))
					.findFirst().orElse(null);
			assertNotNull("Active ESP_IDF_EIM_ID must match an installation in eim_idf.json", target); //$NON-NLS-1$
			assertTrue("The current installation must be activatable", target.isActivatable()); //$NON-NLS-1$

			environment.addEnvVariable(IDFEnvironmentVariables.ESP_IDF_EIM_ID, "swtbot-manager-not-active"); //$NON-NLS-1$
			return target;
		}

		static void givenInstalledEimSupportsGuiMode() {
			ToolInitializer initializer = new ToolInitializer(InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID));
			String eimExecutablePath = initializer.resolveEimExecutablePath(null);
			Assume.assumeTrue("EIM executable must already be installed for the external launch integration test", //$NON-NLS-1$
					eimExecutablePath != null && !eimExecutablePath.isBlank());
			Assume.assumeTrue("External launch integration test currently supports GUI-capable EIM only", //$NON-NLS-1$
					initializer.isEimGuiCapable(eimExecutablePath));
		}

		static void whenEspIdfManagerIsOpenedFromMenu() {
			closeEspIdfManagerIfOpen();
			bot.shell().activate();
			bot.menu("Espressif").menu(Messages.EspIdfEditorTitle).click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitForCTabToAppear(bot, Messages.EspIdfEditorTitle, TABLE_LOAD_TIMEOUT_MS);
			bot.editorByTitle(Messages.EspIdfEditorTitle).show();
		}

		static void whenInstalledVersionsTableIsLoaded() {
			waitForInstalledVersionsTableToLoad();
			readActiveInstallationDetails(bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table());
		}

		static void whenInstallationLocationIsSelected(String installationLocation) {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			SWTBotTable table = editor.bot().table();
			int row = findRowByLocation(table, installationLocation);
			assertTrue("Expected installation location in ESP-IDF Manager: " + installationLocation, row >= 0); //$NON-NLS-1$
			table.select(row);
			assertTrue("Activate Selected must be enabled for the selected inactive installation", //$NON-NLS-1$
					editor.bot().button(Messages.ESPIDFMainTablePage_ActiveBtnName).isEnabled());
		}

		static void whenActivateSelectedIsClicked() {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);
			SWTBotButton activateButton = editor.bot().button(Messages.ESPIDFMainTablePage_ActiveBtnName);
			assertTrue("Activate Selected must be enabled before clicking it", activateButton.isEnabled()); //$NON-NLS-1$
			activateButton.click();
			TestWidgetWaitUtility.waitUntilViewContains(bot, SETTING_UP_IDE_ENVIRONMENT, toolsConsole,
					REFRESH_START_TIMEOUT_MS);
		}

		static void whenRefreshEnvironmentIsClicked() {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			editor.show();
			editor.setFocus();

			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);
			toolsConsole.show();
			toolsConsole.setFocus();

			SWTBotButton refreshButton = editor.bot().button(Messages.ESPIDFMainTablePage_RefreshEnvBtnName);
			assertTrue("Refresh Environment must be enabled before clicking it", refreshButton.isEnabled()); //$NON-NLS-1$
			refreshButton.click();
			TestWidgetWaitUtility.waitUntilViewContains(bot, SETTING_UP_IDE_ENVIRONMENT, toolsConsole,
					REFRESH_START_TIMEOUT_MS);
		}

		static void whenManagerTableSelectionIsCleared() {
			SWTBotTable table = bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table();
			table.unselect();
		}

		static void whenEspressifPreferencesAreOpened() {
			bot.menu("Window").menu("Preferences...").click(); //$NON-NLS-1$ //$NON-NLS-2$
			TestWidgetWaitUtility.waitForDialogToAppear(bot, "Preferences", 10_000L); //$NON-NLS-1$
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			preferencesShell.bot().tree().getTreeItem("Espressif").select(); //$NON-NLS-1$
		}

		static void whenEimJsonPreferenceTextIsSet(String value) {
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			SWTBotText pathText = preferencesShell.bot().textWithLabel(com.espressif.idf.ui.preferences.Messages
			        .EspresssifPreferencesPage_EimIdfJsonPathLabel);
			pathText.setText(value);
		}

		static void whenPreferencesAreAppliedAndClosed() {
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			try {
				preferencesShell.bot().button("Apply and Close").click(); //$NON-NLS-1$
			} catch (WidgetNotFoundException e) {
				preferencesShell.bot().button("OK").click(); //$NON-NLS-1$
			}
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", 10_000L); //$NON-NLS-1$
			eimPreferenceChanged = true;
		}

		static void whenCustomEimConfigIsReplacedWithEmptyConfiguration(Path customConfig) throws IOException {
			Files.writeString(customConfig, EMPTY_EIM_CONFIG, StandardCharsets.UTF_8);
		}

		static void whenCustomEimConfigIsReplacedWithOriginalConfiguration(Path customConfig) throws IOException {
			Files.copy(originalResolvedEimJsonPath, customConfig, StandardCopyOption.REPLACE_EXISTING);
		}

		static void whenEimInstallationUpdateIsAccepted() {
			SWTBotShell changeDialog = bot.shell(Messages.EimJsonChangedMsgTitle);
			changeDialog.bot().button("Yes").click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, Messages.EimJsonChangedMsgTitle, DIALOG_WAIT_TIMEOUT_MS);
		}

		static void whenEimInstallationUpdateIsDeclined() {
			SWTBotShell changeDialog = bot.shell(Messages.EimJsonChangedMsgTitle);
			changeDialog.bot().button("No").click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, Messages.EimJsonChangedMsgTitle, DIALOG_WAIT_TIMEOUT_MS);
		}

		static void whenManageEspIdfVersionsIsClicked() {
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);
			String consoleText = getConsoleText(toolsConsole);
			eimLaunchMarkerCountBeforeAction = countOccurrences(consoleText, EIM_LAUNCH_CONSOLE_MARKER);
			eimCliOpeningCountBeforeAction = countOccurrences(consoleText, Messages.EimCliTerminalOpeningWizard);
			eimCliCompletedCountBeforeAction = countOccurrences(consoleText, Messages.EimCliTerminalWizardCompleted);
			terminalTabCountBeforeAction = getTerminalTabCount();
			bot.editorByTitle(Messages.EspIdfEditorTitle).bot().button(Messages.EIMButtonLaunchText).click();
		}

		static void thenEditorShowsInstalledVersionsTable() {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			SWTBotButton manageVersions = editor.bot().button(Messages.EIMButtonLaunchText);
			editor.bot().button(Messages.ESPIDFMainTablePage_ActiveBtnName);
			editor.bot().button(Messages.ESPIDFMainTablePage_RefreshEnvBtnName);

			assertTrue("Manage ESP-IDF Versions must be enabled", manageVersions.isEnabled()); //$NON-NLS-1$
			SWTBotTable table = editor.bot().table();
			assertTrue("Expected at least one installed ESP-IDF version", table.rowCount() > 0); //$NON-NLS-1$
			assertTrue("Expected four table columns", table.columnCount() >= 4); //$NON-NLS-1$
		}

		static void thenActiveEspIdfVersionIsListed() {
			SWTBotTable table = bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table();
			assertTrue("Expected an active ESP-IDF installation after env setup", hasActiveInstallation(table)); //$NON-NLS-1$
			assertTrue("Expected Refresh Environment to be enabled for an active installation", //$NON-NLS-1$
					bot.editorByTitle(Messages.EspIdfEditorTitle).bot().button(Messages.ESPIDFMainTablePage_RefreshEnvBtnName).isEnabled());
		}

		static void thenManagerShowsEmptyState() {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			SWTBotTable table = editor.bot().table();
			assertEquals("Expected no installed ESP-IDF rows", 0, table.rowCount()); //$NON-NLS-1$
			assertFalse("Activate Selected must be disabled in the empty state", //$NON-NLS-1$
					editor.bot().button(Messages.ESPIDFMainTablePage_ActiveBtnName).isEnabled());
			assertFalse("Refresh Environment must be disabled when no IDF is active", //$NON-NLS-1$
					editor.bot().button(Messages.ESPIDFMainTablePage_RefreshEnvBtnName).isEnabled());
			assertTrue("Manage ESP-IDF Versions must remain available in the empty state", //$NON-NLS-1$
					editor.bot().button(Messages.EIMButtonLaunchText).isEnabled());
		}

		static void thenConfiguredIdfLocationsAreDisplayed(EimConfigModel configuredModel) {
			SWTBotTable table = bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table();
			int checkedLocations = 0;
			for (EimInstallationModel installation : configuredModel.getInstallations()) {
				if (installation.getPath() == null || installation.getPath().isBlank()) {
					continue;
				}
				checkedLocations++;
				assertTrue("Configured ESP-IDF location is missing from Manager table: " + installation.getPath(), //$NON-NLS-1$
						findRowByLocation(table, installation.getPath()) >= 0);
			}
			assertTrue("Expected at least one configured ESP-IDF path to verify", checkedLocations > 0); //$NON-NLS-1$
		}

		static void thenActivateSelectedIsDisabled() {
			assertFalse("Activate Selected must be disabled when no row is selected", //$NON-NLS-1$
					bot.editorByTitle(Messages.EspIdfEditorTitle).bot().button(Messages.ESPIDFMainTablePage_ActiveBtnName).isEnabled());
		}

		static void thenResolvedEimJsonPathIs(Path expected) {
			Path actual = new EimIdfJsonPathResolver().resolveEimIdfJsonFile();
			assertPathEquals("Resolved eim_idf.json path does not match expected path", expected.toString(), //$NON-NLS-1$
					actual.toString());
		}

		static void thenStoredEimJsonPreferenceIs(String expected) {
			String actual = getCorePreferences().get(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH, ""); //$NON-NLS-1$
			assertPathEquals("Stored eim_idf.json preference does not match the value applied in Preferences", expected, //$NON-NLS-1$
					actual);
		}

		static void thenEimInstallationChangedDialogIsShown() {
			TestWidgetWaitUtility.waitForDialogToAppear(bot, Messages.EimJsonChangedMsgTitle, DIALOG_WAIT_TIMEOUT_MS);
			SWTBotShell changeDialog = bot.shell(Messages.EimJsonChangedMsgTitle);
			changeDialog.bot().button("Yes"); //$NON-NLS-1$
			changeDialog.bot().button("No"); //$NON-NLS-1$
		}

		static void thenManagerTableRowCountRemains(int expectedRowCount) {
			bot.sleep(500);
			assertEquals("Declining the EIM update must keep the currently displayed Manager table", expectedRowCount, //$NON-NLS-1$
					getManagerTableRowCount());
		}

		static void thenManagerRefreshFinishes() {
			waitForJobToFinish(Messages.ESPIDFMainTablePage_RefreshingIdfJobName, TABLE_LOAD_TIMEOUT_MS);
		}

		static void thenActiveIdfLocationIs(String expectedLocation) {
			assertNotNull("Expected active ESP-IDF location must not be null", expectedLocation); //$NON-NLS-1$
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			editor.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					SWTBotTable table = editor.bot().table();
					for (int row = 0; row < table.rowCount(); row++) {
						if (table.cell(row, 0).contains(Messages.ESPIDFMainTablePage_ActiveLbl)
								&& pathsEqual(expectedLocation, table.cell(row, 3))) {
							readActiveInstallationDetails(table);
							return true;
						}
					}
					return false;
				}

				@Override
				public String getFailureMessage() {
					return "Expected active ESP-IDF location: " + expectedLocation; //$NON-NLS-1$
				}
			}, CONSOLE_WAIT_TIMEOUT_MS, 500);
		}

		static void thenIdfPyVersionMatchesActiveManagerVersion() throws Exception {
			readActiveInstallationDetails(bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table());
			assertNotNull("Active installation path must be available before running idf.py --version", //$NON-NLS-1$
					activeInstallationLocation);
			assertNotNull("Active Manager version must be available before running idf.py --version", //$NON-NLS-1$
					activeInstallationTableVersion);

			IDFEnvironmentVariables environmentVariables = new IDFEnvironmentVariables();
			String pythonExecutable = environmentVariables.getEnvValue(IDFEnvironmentVariables.PYTHON_EXE_PATH);
			assertTrue("PYTHON_EXE_PATH must be configured after ESP-IDF activation", //$NON-NLS-1$
					pythonExecutable != null && !pythonExecutable.isBlank());
			Path idfPy = Paths.get(activeInstallationLocation, "tools", "idf.py"); //$NON-NLS-1$ //$NON-NLS-2$
			assertTrue("idf.py must exist in the active ESP-IDF installation: " + idfPy, Files.isRegularFile(idfPy)); //$NON-NLS-1$

			ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, idfPy.toString(), "--version"); //$NON-NLS-1$
			processBuilder.redirectErrorStream(true);
			processBuilder.environment().putAll(environmentVariables.getEnvMap());
			processBuilder.environment().put(IDFEnvironmentVariables.IDF_PATH, activeInstallationLocation);
			Process process = processBuilder.start();
			boolean finished = process.waitFor(120, TimeUnit.SECONDS);
			if (!finished) {
				process.destroyForcibly();
			}
			assertTrue("idf.py --version did not finish within 120 seconds", finished); //$NON-NLS-1$
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
			assertEquals("idf.py --version must exit successfully. Output: " + output, 0, process.exitValue()); //$NON-NLS-1$
			assertTrue("idf.py --version must report the version shown by ESP-IDF Manager. Output: " + output, //$NON-NLS-1$
					output.contains(activeInstallationTableVersion));
		}

		static void thenToolsSetupCompletesInConsole() {
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);
			TestWidgetWaitUtility.waitUntilViewContains(bot, TOOLS_SETUP_COMPLETE, toolsConsole, CONSOLE_WAIT_TIMEOUT_MS);

			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			editor.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					return editor.bot().button(Messages.ESPIDFMainTablePage_RefreshEnvBtnName).isEnabled();
				}

				@Override
				public String getFailureMessage() {
					return "Refresh Environment did not become enabled after tools setup finished"; //$NON-NLS-1$
				}
			}, CONSOLE_WAIT_TIMEOUT_MS, 3000);
		}

		static void thenIdfEnvironmentVariablesMatchActiveVersion() {
			assertNotNull("Active ESP-IDF installation location must be known", activeInstallationLocation); // $NON-NLS-1$
			assertNotNull("Active ESP-IDF version must be known", activeInstallationTableVersion); // $NON-NLS-1$
			IDFEnvironmentVariables environmentVariables = new IDFEnvironmentVariables();
			String idfPath = environmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PATH);
			String idfPythonEnvPath = environmentVariables.getEnvValue(IDFEnvironmentVariables.IDF_PYTHON_ENV_PATH);
			String pythonExePath = environmentVariables.getEnvValue(IDFEnvironmentVariables.PYTHON_EXE_PATH);
			String espIdfVersion = environmentVariables.getEnvValue(IDFEnvironmentVariables.ESP_IDF_VERSION);
			String espIdfEimId = environmentVariables.getEnvValue(IDFEnvironmentVariables.ESP_IDF_EIM_ID);
			assertTrue("IDF_PATH must be configured after Tools Setup", //$NON-NLS-1$
					idfPath != null && !idfPath.isBlank());
			assertTrue("IDF_PYTHON_ENV_PATH must be configured after Tools Setup", //$NON-NLS-1$
					idfPythonEnvPath != null && !idfPythonEnvPath.isBlank());
			assertTrue("PYTHON_EXE_PATH must be configured after Tools Setup", //$NON-NLS-1$
					pythonExePath != null && !pythonExePath.isBlank());
			assertTrue("ESP_IDF_VERSION must be configured after Tools Setup", //$NON-NLS-1$
					espIdfVersion != null && !espIdfVersion.isBlank());
			assertTrue("ESP_IDF_EIM_ID must be configured after Tools Setup", //$NON-NLS-1$
					espIdfEimId != null && !espIdfEimId.isBlank());
			assertPathEquals("IDF_PATH must point to the active ESP-IDF installation shown in ESP-IDF Manager", //$NON-NLS-1$
					activeInstallationLocation, idfPath);
			assertEquals("ESP_IDF_VERSION must match the active version shown in ESP-IDF Manager", //$NON-NLS-1$
					activeInstallationTableVersion, espIdfVersion);

			Path idfPathOnDisk = Paths.get(idfPath);
			Path pythonEnvPathOnDisk = Paths.get(idfPythonEnvPath);
			Path pythonExePathOnDisk = Paths.get(pythonExePath);

			assertTrue("IDF_PATH must point to an existing directory: " + idfPathOnDisk, //$NON-NLS-1$
					Files.isDirectory(idfPathOnDisk));
			assertTrue("IDF_PYTHON_ENV_PATH must point to an existing directory: " + pythonEnvPathOnDisk, //$NON-NLS-1$
					Files.isDirectory(pythonEnvPathOnDisk));
			assertTrue("PYTHON_EXE_PATH must point to an existing file: " + pythonExePathOnDisk, //$NON-NLS-1$
					Files.isRegularFile(pythonExePathOnDisk));
			assertTrue("PYTHON_EXE_PATH must be executable on Linux: " + pythonExePathOnDisk, //$NON-NLS-1$
					Files.isExecutable(pythonExePathOnDisk));
		}

		static void thenEimLaunchIsReportedInConsoleAndProcessIsStopped() {
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);
			toolsConsole.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					String text = getConsoleText(toolsConsole);
					return countOccurrences(text, EIM_LAUNCH_CONSOLE_MARKER) > eimLaunchMarkerCountBeforeAction;
				}

				@Override
				public String getFailureMessage() {
					return "Expected Manage ESP-IDF Versions to launch EIM"; //$NON-NLS-1$
				}
			}, 60_000L, 500);

			String newConsoleOutput = textFromNthOccurrence(getConsoleText(toolsConsole), EIM_LAUNCH_CONSOLE_MARKER,
					eimLaunchMarkerCountBeforeAction + 1);
			Pattern pidPattern = Pattern.compile("Launched EIM application:.*\\(pid=(\\d+)\\)"); //$NON-NLS-1$
			Matcher matcher = pidPattern.matcher(newConsoleOutput);
			long launchedPid = -1;
			while (matcher.find()) {
				launchedPid = Long.parseLong(matcher.group(1));
			}
			assertTrue("Expected a pid in the new EIM launch console output: " + newConsoleOutput, launchedPid > 0); //$NON-NLS-1$

			ProcessHandle.of(launchedPid).ifPresent(process -> {
				process.destroy();
				try {
					process.onExit().get(10, TimeUnit.SECONDS);
				} catch (Exception e) {
					process.destroyForcibly();
				}
			});
			waitForJobToFinish(WAIT_FOR_EIM_CLOSURE_JOB, 30_000L);
		}

		static int getManagerTableRowCount() {
			return bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table().rowCount();
		}

		static String getActiveInstallationLocation() {
			readActiveInstallationDetails(bot.editorByTitle(Messages.EspIdfEditorTitle).bot().table());
			assertNotNull("Expected an active ESP-IDF installation", activeInstallationLocation); //$NON-NLS-1$
			return activeInstallationLocation;
		}

		static String getActiveInstallationTableVersion() {
			readActiveInstallationDetails(bot.editorByTitle(com.espressif.idf.ui.tools.Messages.EspIdfEditorTitle).bot().table());
			assertNotNull("Expected active ESP-IDF version in the Manager table", activeInstallationTableVersion); //$NON-NLS-1$
			return activeInstallationTableVersion;
		}

		static Path getDefaultEimJsonPath() {
			return new EimIdfJsonPathResolver().getDefaultEimIdfJsonFile();
		}

		static void closeEspIdfManagerIfOpen() {
			try {
				bot.cTabItem(Messages.EspIdfEditorTitle).close();
			} catch (WidgetNotFoundException ignored) {
			}
		}

		static void closePreferencesDialogIfOpen() {
			try {
				bot.shell("Preferences"); //$NON-NLS-1$
				closePreferencesDialog();
			} catch (WidgetNotFoundException ignored) {
			}
		}

		static void closeEimChangeDialogIfOpen() {
			try {
				bot.shell(Messages.EimJsonChangedMsgTitle).bot().button("No").click(); //$NON-NLS-1$
			} catch (WidgetNotFoundException ignored) {
			}
		}

		static void restoreDefaultEimJsonOrFail() {
			IOException restoreFailure = restoreDefaultEimJson();
			if (restoreFailure != null) {
				throw new AssertionError("Failed to restore default eim_idf.json after the missing-file test", //$NON-NLS-1$
						restoreFailure);
			}
			assertTrue("Default eim_idf.json must be restored after the missing-file test", //$NON-NLS-1$
					Files.isRegularFile(getDefaultEimJsonPath()));
		}

		static void restoreDefaultEimJsonIfNeeded() {
			restoreDefaultEimJson();
		}

		private static IOException restoreDefaultEimJson() {
			IOException restoreFailure = null;
			if (defaultEimJsonTemporarilyMissing && defaultEimJsonBackup != null
					&& Files.exists(defaultEimJsonBackup)) {
				try {
					Files.copy(defaultEimJsonBackup, getDefaultEimJsonPath(), StandardCopyOption.REPLACE_EXISTING);
					defaultEimJsonTemporarilyMissing = false;
					bot.sleep(750);
				} catch (IOException e) {
					restoreFailure = e;
				}
			}
			if (watcherPausedForMissingDefault) {
				EimJsonWatchService watcher = EimJsonWatchService.getInstance();
				if (watcher != null) {
					watcher.unpauseListeners();
				}
				watcherPausedForMissingDefault = false;
			}
			return restoreFailure;
		}

		static void restoreMutableState() throws Exception {
			restoreDefaultEimJsonIfNeeded();
			if (eimPreferenceChanged) {
				IEclipsePreferences preferences = getCorePreferences();
				preferences.put(IDFCorePreferenceConstants.EIM_IDF_JSON_PATH,
						originalEimJsonPreference == null ? "" : originalEimJsonPreference);
				preferences.flush();
				EimJsonWatchService.restartAfterEimIdfPathChange();
			}
			restoreEnvironmentVariables();
			deleteTempDirectory();
			activeInstallationLocation = null;
			activeInstallationTableVersion = null;
		}

		private static void waitForInstalledVersionsTableToLoad() {
			SWTBotEditor editor = bot.editorByTitle(Messages.EspIdfEditorTitle);
			editor.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					SWTBotTable table = editor.bot().table();
					if (table.rowCount() == 0) {
						return false;
					}
					for (int row = 0; row < table.rowCount(); row++) {
						String version = table.cell(row, 1);
						if (!version.isEmpty() && !Messages.ESPIDFMainTablePage_VersionDetectionFailedMsg.equals(version)) {
							return true;
						}
					}
					return false;
				}

				@Override
				public String getFailureMessage() {
					return "Installed ESP-IDF versions table did not finish loading"; //$NON-NLS-1$
				}
			}, TABLE_LOAD_TIMEOUT_MS, 2000);
		}

		private static void waitForJobToFinish(String jobName, long timeoutMs) {
			bot.sleep(2500);
			bot.waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					for (Job job : Job.getJobManager().find(null)) {
						if (jobName.equals(job.getName()) && job.getState() != Job.NONE) {
							return false;
						}
					}
					return true;
				}

				@Override
				public String getFailureMessage() {
					return "Timed out waiting for job to finish: " + jobName; //$NON-NLS-1$
				}
			}, timeoutMs, 200);
			bot.sleep(250);
		}

		private static boolean hasActiveInstallation(SWTBotTable table) {
			for (int row = 0; row < table.rowCount(); row++) {
				String status = table.cell(row, 0);
				String location = table.cell(row, 3);
				if (status.contains(Messages.ESPIDFMainTablePage_ActiveLbl) && !location.isEmpty()) {
					return true;
				}
			}
			return false;
		}

		private static int findRowByLocation(SWTBotTable table, String expectedLocation) {
			for (int row = 0; row < table.rowCount(); row++) {
				if (pathsEqual(expectedLocation, table.cell(row, 3))) {
					return row;
				}
			}
			return -1;
		}

		private static String getConsoleText(SWTBotView consoleView) {
			consoleView.show();
			consoleView.setFocus();
			return consoleView.bot().styledText().getText();
		}

		private static int countOccurrences(String text, String needle) {
			int count = 0;
			int index = 0;
			while ((index = text.indexOf(needle, index)) != -1) {
				count++;
				index += needle.length();
			}
			return count;
		}

		private static String textFromNthOccurrence(String text, String needle, int occurrenceNumber) {
			int fromIndex = 0;
			int found = -1;
			for (int i = 0; i < occurrenceNumber; i++) {
				found = text.indexOf(needle, fromIndex);
				if (found < 0) {
					return ""; //$NON-NLS-1$
				}
				fromIndex = found + needle.length();
			}
			return text.substring(found);
		}

		private static void readActiveInstallationDetails(SWTBotTable table) {
			activeInstallationLocation = null;
			activeInstallationTableVersion = null;

			for (int row = 0; row < table.rowCount(); row++) {
				if (table.cell(row, 0).contains(Messages.ESPIDFMainTablePage_ActiveLbl)) {
					activeInstallationLocation = table.cell(row, 3);
					activeInstallationTableVersion = table.cell(row, 1);
					return;
				}
			}
		}

		private static void closePreferencesDialog() {
			SWTBotShell preferencesShell = bot.shell("Preferences"); //$NON-NLS-1$
			preferencesShell.bot().button("Cancel").click(); //$NON-NLS-1$
			TestWidgetWaitUtility.waitWhileDialogIsVisible(bot, "Preferences", 10_000L); //$NON-NLS-1$
		}

		private static IEclipsePreferences getCorePreferences() {
			return InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
		}

		private static Path writeTempEimConfig(String content) throws IOException {
			Path eimJson = getTempEimJsonPath();
			Files.writeString(eimJson, content, StandardCharsets.UTF_8);
			return eimJson;
		}

		private static Path getTempEimJsonPath() throws IOException {
			ensureTempDirectory();
			return tempDirectory.resolve(EimConstants.EIM_JSON);
		}

		private static void ensureTempDirectory() throws IOException {
			if (tempDirectory == null || !Files.exists(tempDirectory)) {
				tempDirectory = Files.createTempDirectory("esp-idf-manager-swtbot-"); //$NON-NLS-1$
			}
		}

		private static void deleteTempDirectory() {
			if (tempDirectory == null || !Files.exists(tempDirectory)) {
				tempDirectory = null;
				return;
			}
			try (var paths = Files.walk(tempDirectory)) {
				paths.sorted(Comparator.reverseOrder()).forEach(path -> {
					try {
						Files.deleteIfExists(path);
					} catch (IOException ignored) {
					}
				});
			} catch (IOException ignored) {
			}
			tempDirectory = null;
		}

		private static void assertPathEquals(String message, String expected, String actual) {
			String normalizedExpected = normalizePath(expected);
			String normalizedActual = normalizePath(actual);
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				assertEquals(message + " (expected: " + normalizedExpected + ", actual: " + normalizedActual + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						normalizedExpected.toLowerCase(), normalizedActual.toLowerCase());
				return;
			}
			assertEquals(message + " (expected: " + normalizedExpected + ", actual: " + normalizedActual + ")", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					normalizedExpected, normalizedActual);
		}

		private static boolean pathsEqual(String first, String second) {
			String normalizedFirst = normalizePath(first);
			String normalizedSecond = normalizePath(second);
			if (normalizedFirst == null || normalizedSecond == null) {
				return normalizedFirst == normalizedSecond;
			}
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				return normalizedFirst.equalsIgnoreCase(normalizedSecond);
			}
			return normalizedFirst.equals(normalizedSecond);
		}

		private static String normalizePath(String path) {
			if (path == null) {
				return null;
			}
			return Paths.get(path.replace('\\', '/')).normalize().toString();
		}

		static void givenInstalledEimSupportsCliMode() {
			ToolInitializer initializer = new ToolInitializer(InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID));
			String eimExecutablePath = initializer.resolveEimExecutablePath(null);
			Assume.assumeTrue("EIM executable must already be installed for the CLI integration test", //$NON-NLS-1$
					eimExecutablePath != null && !eimExecutablePath.isBlank()
							&& Files.isRegularFile(Path.of(eimExecutablePath))
							&& Files.isExecutable(Path.of(eimExecutablePath)));
			Assume.assumeFalse("CLI integration test requires an EIM executable without GUI support", //$NON-NLS-1$
					initializer.isEimGuiCapable(eimExecutablePath));
		}

		static void thenEimCliTerminalIsOpenedAndClosed() {
			SWTBotView toolsConsole = ProjectTestOperations.viewConsole(Messages.IDFToolsHandler_ToolsManagerConsole, bot);

			/*
			 * Verify that EimGuiOrCliLauncher selected CLI mode.
			 */
			toolsConsole.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					return countOccurrences(getConsoleText(toolsConsole),
							Messages.EimCliTerminalOpeningWizard) > eimCliOpeningCountBeforeAction;
				}

				@Override
				public String getFailureMessage() {
					return "Expected Manage ESP-IDF Versions to launch EIM in CLI mode"; //$NON-NLS-1$
				}
			}, 60_000L, 500);

			/*
			 * Wait for a NEW terminal tab.
			 *
			 * Do not check its title because Eclipse Terminal may display the actual shell
			 * executable instead of "EIM Setup Wizard".
			 */
			bot.waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					return getTerminalTabCount() > terminalTabCountBeforeAction;
				}

				@Override
				public String getFailureMessage() {
					return "Expected EIM CLI to open a new Eclipse Terminal tab"; //$NON-NLS-1$
				}
			}, 60_000L, 500);

			SWTBotView terminalView = bot.viewByTitle(TERMINAL_VIEW_TITLE);

			terminalView.show();
			terminalView.setFocus();

			List<? extends CTabItem> terminalTabs = terminalView.bot().widgets(widgetOfType(CTabItem.class));

			assertTrue("Expected at least one terminal tab", !terminalTabs.isEmpty());

			/*
			 * PROP_FORCE_NEW=true guarantees that the EIM CLI launch creates a new terminal
			 * session. The newest tab is the last one.
			 */
			CTabItem newTerminalTab = terminalTabs.get(terminalTabs.size() - 1);

			SWTBotCTabItem eimTerminal = new SWTBotCTabItem(newTerminalTab);

			/*
			 * At this point the feature under test has succeeded: EIM CLI wizard was
			 * launched in a new integrated terminal.
			 *
			 * Close it so the interactive wizard does not block the test.
			 */
			eimTerminal.close();

			/*
			 * Closing the terminal terminates the shell/EIM process. Production code then
			 * invokes its completion callback.
			 */
			toolsConsole.bot().waitUntil(new DefaultCondition() {
				@Override
				public boolean test() throws Exception {
					return countOccurrences(getConsoleText(toolsConsole),
							Messages.EimCliTerminalWizardCompleted) > eimCliCompletedCountBeforeAction;
				}

				@Override
				public String getFailureMessage() {
					return "Expected EIM CLI completion callback after closing terminal"; //$NON-NLS-1$
				}
			}, 30_000L, 500);
		}

		static void closeEimCliTerminalIfOpen() {
			try {
				SWTWorkbenchBot workbenchBot = WorkBenchSWTBot.getBot();
				SWTBotView terminalView = workbenchBot.viewByTitle(TERMINAL_VIEW_TITLE);
				terminalView.show();
				terminalView.bot().cTabItem(Messages.EimCliTerminalWizardTitle).close();
			} catch (WidgetNotFoundException ignored) {
			}
		}

		static int getTerminalTabCount() {
			try {
				SWTBotView terminalView = bot.viewByTitle(TERMINAL_VIEW_TITLE);

				return terminalView.bot().widgets(widgetOfType(CTabItem.class)).size();
			} catch (WidgetNotFoundException e) {
				return 0;
			}
		}

		private static void restoreEnvironmentVariables() {
			if (originalEnvironmentVariables == null) {
				return;
			}
			IDFEnvironmentVariables environmentVariables = new IDFEnvironmentVariables();
			environmentVariables.removeAllEnvVariables();
			for (Map.Entry<String, String> entry : originalEnvironmentVariables.entrySet()) {
				environmentVariables.addEnvVariable(entry.getKey(), entry.getValue());
			}
		}

		@SuppressWarnings("restriction")
		static void clearFinishedProgressJobs()
		{
		    Display.getDefault().syncExec(() -> {
		        FinishedJobs.getInstance().clearAll();
		    });
		}
	}
}
