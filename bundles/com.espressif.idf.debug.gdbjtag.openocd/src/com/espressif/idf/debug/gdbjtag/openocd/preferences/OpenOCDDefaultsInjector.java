package com.espressif.idf.debug.gdbjtag.openocd.preferences;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ILaunchDefaultsContributor;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;
import com.espressif.idf.debug.gdbjtag.openocd.ConfigurationAttributes;
import com.espressif.idf.debug.gdbjtag.openocd.IIDFGDBJtagConstants;

public class OpenOCDDefaultsInjector implements ILaunchDefaultsContributor
{

	private static final String ESP_SVD_PATH = "esp_svd_path"; //$NON-NLS-1$

	@Override
	public void applyDefaults(ILaunchConfigurationWorkingCopy configuration)
	{

		PersistentPreferences persistentPrefs = Activator.getInstance().getPersistentPreferences();

		try
		{
			if (configuration.hasAttribute(ConfigurationAttributes.DO_START_GDB_SERVER))
			{
				return;
			}
		}
		catch (CoreException e)
		{
			Logger.log("Failed to check OpenOCD defaults guard", e);
		}

		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
				DefaultPreferences.PROGRAM_APP_DEFAULT);
		// --- 1. JTAG Device Setup ---
		configuration.setAttribute(IGDBJtagConstants.ATTR_JTAG_DEVICE_ID, ConfigurationAttributes.JTAG_DEVICE);

		// --- 2. OpenOCD GDB Server Setup ---
		configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_SERVER, persistentPrefs.getGdbServerDoStart());
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_EXECUTABLE,
				persistentPrefs.getGdbServerExecutable());
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_CONNECTION_ADDRESS,
				DefaultPreferences.GDB_SERVER_CONNECTION_ADDRESS_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
				DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TELNET_PORT_NUMBER,
				DefaultPreferences.GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_TCL_PORT_NUMBER,
				DefaultPreferences.GDB_SERVER_TCL_PORT_NUMBER_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_LOG, DefaultPreferences.GDB_SERVER_LOG_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
				DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE,
				DefaultPreferences.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_SERVER_OTHER,
				DefaultPreferences.GDB_SERVER_OTHER_DEFAULT);

		// --- 3. GDB Client Setup ---
		configuration.setAttribute(IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
				DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
				DefaultPreferences.GDB_CLIENT_EXECUTABLE_DYNAMIC_DEFAULT);
		configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS,
				persistentPrefs.getGdbClientOtherOptions());
		configuration.setAttribute(ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS,
				persistentPrefs.getGdbClientCommands());

		// --- 4. Thread List Setup ---
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				DefaultPreferences.UPDATE_THREAD_LIST_DEFAULT);

		// --- 5. SVD Target Setup ---
		// Using the embedcdt ConfigurationAttributes explicitly to avoid namespace collision
		configuration.setAttribute(org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes.SVD_PATH,
				VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(ESP_SVD_PATH, null));
		
		// --- 6. Initialisation Commands (from TabStartup) ---
        configuration.setAttribute(ConfigurationAttributes.DO_FIRST_RESET, persistentPrefs.getOpenOCDDoInitialReset());
        configuration.setAttribute(ConfigurationAttributes.FIRST_RESET_TYPE, persistentPrefs.getOpenOCDInitialResetType());
        configuration.setAttribute(ConfigurationAttributes.ENABLE_SEMIHOSTING, persistentPrefs.getOpenOCDEnableSemihosting());
        configuration.setAttribute(ConfigurationAttributes.OTHER_INIT_COMMANDS, persistentPrefs.getOpenOCDInitOther());

        // --- 7. Load Image & Symbols (from TabStartup) ---
        configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_IMAGE, IIDFGDBJtagConstants.DEFAULT_LOAD_IMAGE);
        configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE);
        configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_IMAGE, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_IMAGE);
        configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_FILE_NAME, IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME);
        configuration.setAttribute(IGDBJtagConstants.ATTR_IMAGE_OFFSET, IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);
        
        configuration.setAttribute(IGDBJtagConstants.ATTR_LOAD_SYMBOLS, IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
        configuration.setAttribute(IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS);
        configuration.setAttribute(IGDBJtagConstants.ATTR_USE_FILE_FOR_SYMBOLS, IGDBJtagConstants.DEFAULT_USE_FILE_FOR_SYMBOLS);
        configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME, IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
        configuration.setAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);

        // --- 8. Runtime Options & Run Commands (from TabStartup) ---
        configuration.setAttribute(ConfigurationAttributes.DO_DEBUG_IN_RAM, persistentPrefs.getOpenOCDDebugInRam());
        configuration.setAttribute(ConfigurationAttributes.DO_SECOND_RESET, persistentPrefs.getOpenOCDDoPreRunReset());
        configuration.setAttribute(ConfigurationAttributes.SECOND_RESET_TYPE, persistentPrefs.getOpenOCDPreRunResetType());
        configuration.setAttribute(ConfigurationAttributes.OTHER_RUN_COMMANDS, persistentPrefs.getOpenOCDPreRunOther());

        configuration.setAttribute(IGDBJtagConstants.ATTR_SET_PC_REGISTER, IGDBJtagConstants.DEFAULT_SET_PC_REGISTER);
        configuration.setAttribute(IGDBJtagConstants.ATTR_PC_REGISTER, IGDBJtagConstants.DEFAULT_PC_REGISTER);
        configuration.setAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, DefaultPreferences.DO_STOP_AT_DEFAULT);
        configuration.setAttribute(IGDBJtagConstants.ATTR_STOP_AT, DefaultPreferences.STOP_AT_NAME_DEFAULT);
        configuration.setAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME);
        configuration.setAttribute(ConfigurationAttributes.DO_CONTINUE, DefaultPreferences.DO_CONTINUE_DEFAULT);


		configuration.setAttribute(ConfigurationAttributes.DO_START_GDB_CLIENT,
				DefaultPreferences.DO_START_GDB_CLIENT_DEFAULT);

		configuration.setAttribute(IGDBJtagConstants.ATTR_IP_ADDRESS, "localhost");

		configuration.setAttribute(IGDBJtagConstants.ATTR_PORT_NUMBER,
				DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);

		configuration.setAttribute(ConfigurationAttributes.DO_FLASH_BEFORE_START, true);
		configuration.setAttribute(ConfigurationAttributes.ENABLE_VERBOSE_OUTPUT, false);
	}
}
