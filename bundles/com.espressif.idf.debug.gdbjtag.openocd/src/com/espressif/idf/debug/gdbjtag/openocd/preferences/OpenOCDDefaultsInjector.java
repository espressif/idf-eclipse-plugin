package com.espressif.idf.debug.gdbjtag.openocd.preferences;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.ILaunchDefaultsContributor;
import com.espressif.idf.core.util.LaunchAttributes;
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
			LaunchAttributes.setStringIfEmpty(configuration, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
					DefaultPreferences.PROGRAM_APP_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_JTAG_DEVICE_ID,
					ConfigurationAttributes.JTAG_DEVICE);

			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_START_GDB_SERVER,
					persistentPrefs.getGdbServerDoStart());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_SERVER_EXECUTABLE,
					persistentPrefs.getGdbServerExecutable());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_SERVER_CONNECTION_ADDRESS,
					DefaultPreferences.GDB_SERVER_CONNECTION_ADDRESS_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.GDB_SERVER_GDB_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.GDB_SERVER_TELNET_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_SERVER_TCL_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_TCL_PORT_NUMBER_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_SERVER_LOG,
					DefaultPreferences.GDB_SERVER_LOG_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_CONSOLE,
					DefaultPreferences.DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE,
					DefaultPreferences.DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_SERVER_OTHER,
					DefaultPreferences.GDB_SERVER_OTHER_DEFAULT);

			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_USE_REMOTE_TARGET,
					DefaultPreferences.USE_REMOTE_TARGET_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					DefaultPreferences.GDB_CLIENT_EXECUTABLE_DYNAMIC_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_CLIENT_OTHER_OPTIONS,
					persistentPrefs.getGdbClientOtherOptions());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.GDB_CLIENT_OTHER_COMMANDS,
					persistentPrefs.getGdbClientCommands());

			LaunchAttributes.setIfAbsent(configuration,
					IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
					DefaultPreferences.UPDATE_THREAD_LIST_DEFAULT);

			LaunchAttributes.setStringIfEmpty(configuration,
					org.eclipse.embedcdt.debug.gdbjtag.core.ConfigurationAttributes.SVD_PATH,
					VariablesPlugin.getDefault().getStringVariableManager().generateVariableExpression(ESP_SVD_PATH,
							null));

			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_FIRST_RESET,
					persistentPrefs.getOpenOCDDoInitialReset());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.FIRST_RESET_TYPE,
					persistentPrefs.getOpenOCDInitialResetType());
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.ENABLE_SEMIHOSTING,
					persistentPrefs.getOpenOCDEnableSemihosting());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.OTHER_INIT_COMMANDS,
					persistentPrefs.getOpenOCDInitOther());

			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_LOAD_IMAGE,
					IIDFGDBJtagConstants.DEFAULT_LOAD_IMAGE);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_IMAGE,
					IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_IMAGE);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_USE_FILE_FOR_IMAGE,
					IGDBJtagConstants.DEFAULT_USE_FILE_FOR_IMAGE);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_IMAGE_FILE_NAME,
					IGDBJtagConstants.DEFAULT_IMAGE_FILE_NAME);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_IMAGE_OFFSET,
					IGDBJtagConstants.DEFAULT_IMAGE_OFFSET);

			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_LOAD_SYMBOLS,
					IGDBJtagConstants.DEFAULT_LOAD_SYMBOLS);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_USE_PROJ_BINARY_FOR_SYMBOLS,
					IGDBJtagConstants.DEFAULT_USE_PROJ_BINARY_FOR_SYMBOLS);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_USE_FILE_FOR_SYMBOLS,
					IGDBJtagConstants.DEFAULT_USE_FILE_FOR_SYMBOLS);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_SYMBOLS_FILE_NAME,
					IGDBJtagConstants.DEFAULT_SYMBOLS_FILE_NAME);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_SYMBOLS_OFFSET,
					IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);

			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_DEBUG_IN_RAM,
					persistentPrefs.getOpenOCDDebugInRam());
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_SECOND_RESET,
					persistentPrefs.getOpenOCDDoPreRunReset());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.SECOND_RESET_TYPE,
					persistentPrefs.getOpenOCDPreRunResetType());
			LaunchAttributes.setStringIfEmpty(configuration, ConfigurationAttributes.OTHER_RUN_COMMANDS,
					persistentPrefs.getOpenOCDPreRunOther());

			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_SET_PC_REGISTER,
					IGDBJtagConstants.DEFAULT_SET_PC_REGISTER);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_PC_REGISTER,
					IGDBJtagConstants.DEFAULT_PC_REGISTER);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_SET_STOP_AT,
					DefaultPreferences.DO_STOP_AT_DEFAULT);
			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_STOP_AT,
					DefaultPreferences.STOP_AT_NAME_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_SET_RESUME,
					IGDBJtagConstants.DEFAULT_SET_RESUME);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_CONTINUE,
					DefaultPreferences.DO_CONTINUE_DEFAULT);

			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_START_GDB_CLIENT,
					DefaultPreferences.DO_START_GDB_CLIENT_DEFAULT);

			LaunchAttributes.setStringIfEmpty(configuration, IGDBJtagConstants.ATTR_IP_ADDRESS,
					DefaultPreferences.REMOTE_IP_ADDRESS_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, IGDBJtagConstants.ATTR_PORT_NUMBER,
					DefaultPreferences.GDB_SERVER_GDB_PORT_NUMBER_DEFAULT);

			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.DO_FLASH_BEFORE_START,
					DefaultPreferences.DO_FLASH_BEFORE_START_DEFAULT);
			LaunchAttributes.setIfAbsent(configuration, ConfigurationAttributes.ENABLE_VERBOSE_OUTPUT,
					DefaultPreferences.ENABLE_VERBOSE_OUTPUT_DEFAULT);
		}
		catch (CoreException e)
		{
			Logger.log(e);
		}
	}
}
