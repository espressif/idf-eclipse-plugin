/*******************************************************************************
 * Copyright (c) 2015 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package com.espressif.idf.debug.gdbjtag.openocd.preferences;

import org.eclipse.embedcdt.core.EclipseUtils;
import org.eclipse.embedcdt.core.preferences.Discoverer;

import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.core.variable.GdbClientDynamicVariable;
import com.espressif.idf.core.variable.JtagDynamicVariable;
import com.espressif.idf.core.variable.OpenocdDynamicVariable;
import com.espressif.idf.debug.gdbjtag.openocd.Activator;

public class DefaultPreferences extends org.eclipse.embedcdt.debug.gdbjtag.core.preferences.DefaultPreferences
{

	// ------------------------------------------------------------------------

	// Constants
	public static final String REMOTE_IP_ADDRESS_LOCALHOST = "localhost"; //$NON-NLS-1$

	// ------------------------------------------------------------------------

	// Preferences
	protected static final boolean TAB_MAIN_CHECK_PROGRAM_DEFAULT = false;

	public static final String GDB_SERVER_EXECUTABLE_DEFAULT = "${openocd_path}/bin/${openocd_executable}"; //$NON-NLS-1$

	public static final String GDB_SERVER_EXECUTABLE_DEFAULT_NAME = "openocd"; //$NON-NLS-1$
	protected static final String GDB_CLIENT_EXECUTABLE_DEFAULT = "${cross_prefix}gdb${cross_suffix}"; //$NON-NLS-1$

	// ------------------------------------------------------------------------

	// Not yet preferences
	public static final boolean DO_START_GDB_SERVER_DEFAULT = true;
	public static final String GDB_SERVER_CONNECTION_ADDRESS_DEFAULT = ""; //$NON-NLS-1$
	public static final int GDB_SERVER_GDB_PORT_NUMBER_DEFAULT = 3333;
	public static final int GDB_SERVER_TELNET_PORT_NUMBER_DEFAULT = 4444;
	public static final String GDB_SERVER_TCL_PORT_NUMBER_DEFAULT = "6666"; //$NON-NLS-1$
	public static final String GDB_SERVER_LOG_DEFAULT = ""; //$NON-NLS-1$
	public static final String GDB_SERVER_OTHER_DEFAULT = String.format("-s ${%s} ${%s}", //$NON-NLS-1$
			OpenocdDynamicVariable.OPENOCD_SCRIPTS, JtagDynamicVariable.JTAG_FLASH_ARGS);
	public static final boolean DO_GDB_SERVER_ALLOCATE_CONSOLE_DEFAULT = true;
	public static final boolean DO_GDB_SERVER_ALLOCATE_TELNET_CONSOLE_DEFAULT = false;

	public static final boolean DO_START_GDB_CLIENT_DEFAULT = true;
	public static final String GDB_CLIENT_EXECUTABLE_DYNAMIC_DEFAULT = String.format("${%s}", //$NON-NLS-1$
			GdbClientDynamicVariable.GDB_CLIENT_EXECUTABLE);
	public static final String GDB_CLIENT_OTHER_OPTIONS_DEFAULT = ""; //$NON-NLS-1$

	public static final boolean USE_REMOTE_TARGET_DEFAULT = true;
	public static final String REMOTE_IP_ADDRESS_DEFAULT = REMOTE_IP_ADDRESS_LOCALHOST; // $NON-NLS-1$
	public static final int REMOTE_PORT_NUMBER_DEFAULT = GDB_SERVER_GDB_PORT_NUMBER_DEFAULT;

	public static final boolean UPDATE_THREAD_LIST_DEFAULT = false;

	public static final boolean DO_FIRST_RESET_DEFAULT = true;
	public static final String FIRST_RESET_TYPE_DEFAULT = "init"; //$NON-NLS-1$

	public static final boolean ENABLE_SEMIHOSTING_DEFAULT = true;

	public static final boolean DO_DEBUG_IN_RAM_DEFAULT = false;

	public static final boolean DO_SECOND_RESET_DEFAULT = true;

	public static final String SECOND_RESET_TYPE_DEFAULT = "halt"; //$NON-NLS-1$

	public static final boolean DO_STOP_AT_DEFAULT = true;
	public static final String STOP_AT_NAME_DEFAULT = "app_main"; //$NON-NLS-1$

	public static final boolean DO_CONTINUE_DEFAULT = true;

	// ------------------------------------------------------------------------

	// Debugger commands
	public static final String GDB_CLIENT_OTHER_COMMANDS_DEFAULT = "set mem inaccessible-by-default off\nset remotetimeout 20"; //$NON-NLS-1$
	public static final String DO_FIRST_RESET_COMMAND = "monitor reset "; //$NON-NLS-1$
	public static final String HALT_COMMAND = "monitor halt"; //$NON-NLS-1$
	public static final String ENABLE_SEMIHOSTING_COMMAND = "monitor arm semihosting enable"; //$NON-NLS-1$
	public static final String DO_SECOND_RESET_COMMAND = "monitor reset "; //$NON-NLS-1$
	public static final String DO_CONTINUE_COMMAND = "continue"; //$NON-NLS-1$
	public static final String IDF_TARGET_CPU_WATCHPOINT_NUM = "{IDF_TARGET_CPU_WATCHPOINT_NUM}"; //$NON-NLS-1$
	public static final String OTHER_INIT_COMMANDS_DEFAULT = "mon reset halt\n" + "flushregs\n" //$NON-NLS-1$ //$NON-NLS-2$
			+ "set remote hardware-watchpoint-limit " + IDF_TARGET_CPU_WATCHPOINT_NUM; //$NON-NLS-1$
	public static final String OTHER_RUN_COMMANDS_DEFAULT = ""; //$NON-NLS-1$

	// ------------------------------------------------------------------------

	// C/C++ project variables

	public static final String PROGRAM_APP_DEFAULT = "${default_app}"; //$NON-NLS-1$

	// ------------------------------------------------------------------------

	// HKCU & HKLM LOCAL_MACHINE
	private static final String REG_SUBKEY = "\\GNU ARM Eclipse\\OpenOCD"; //$NON-NLS-1$
	// Standard Microsoft recommendation.
	private static final String REG_NAME = "InstallLocation"; //$NON-NLS-1$

	// ------------------------------------------------------------------------

	public DefaultPreferences(String pluginId)
	{
		super(pluginId);
	}

	// ------------------------------------------------------------------------

	public String getGdbServerExecutable()
	{
		String value = getString(PersistentPreferences.GDB_SERVER_EXECUTABLE, GDB_SERVER_EXECUTABLE_DEFAULT);
		return value;
	}

	public String getGdbClientExecutable()
	{
		String value = getString(PersistentPreferences.GDB_CLIENT_EXECUTABLE, GDB_CLIENT_EXECUTABLE_DEFAULT);
		return value;
	}

	// ------------------------------------------------------------------------

	public String getOpenocdConfig()
	{
		return getString(PersistentPreferences.GDB_SERVER_OTHER_OPTIONS, DefaultPreferences.GDB_SERVER_OTHER_DEFAULT);
	}

	// ------------------------------------------------------------------------

	public boolean getTabMainCheckProgram()
	{
		return getBoolean(PersistentPreferences.TAB_MAIN_CHECK_PROGRAM,
				PersistentPreferences.TAB_MAIN_CHECK_PROGRAM_DEFAULT);
	}

	// ------------------------------------------------------------------------

	public String getExecutableName()
	{

		String key = PersistentPreferences.EXECUTABLE_NAME;
		String value = getString(key, ""); //$NON-NLS-1$

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.getExecutableName() = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	public String getExecutableNameOs()
	{

		String key = EclipseUtils.getKeyOs(PersistentPreferences.EXECUTABLE_NAME_OS);
		String value = getString(key, ""); //$NON-NLS-1$

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.getExecutableNameOs() = \"" + value + "\" (" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return value;
	}

	public void putExecutableName(String value)
	{

		String key = PersistentPreferences.EXECUTABLE_NAME;

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.putExecutableName(\"" + value + "\")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		putString(key, value);
	}

	// ------------------------------------------------------------------------

	public String getInstallFolder()
	{

		String key = PersistentPreferences.INSTALL_FOLDER;
		String value = getString(key, ""); //$NON-NLS-1$

		if (StringUtil.isEmpty(value))
		{
			value = IDFUtil.getOpenOCDLocation();
		}

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.getInstallFolder() = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	public void putInstallFolder(String value)
	{

		String key = PersistentPreferences.INSTALL_FOLDER;

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.putInstallFolder(\"" + value + "\")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		putString(key, value);
	}

	// ------------------------------------------------------------------------

	@Override
	public String getSearchPath()
	{

		String key = PersistentPreferences.SEARCH_PATH;
		String value = getString(key, ""); //$NON-NLS-1$

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.getSearchPath() = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	@Override
	public String getSearchPathOs()
	{

		String key = EclipseUtils.getKeyOs(PersistentPreferences.SEARCH_PATH_OS);
		String value = getString(key, ""); //$NON-NLS-1$

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.getSearchPathOs() = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	@Override
	public void putSearchPath(String value)
	{

		String key = PersistentPreferences.SEARCH_PATH;

		if (Activator.getInstance().isDebugging())
		{
			System.out.println("openocd.DefaultPreferences.putSearchPath(\"" + value + "\")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		putString(key, value);
	}

	// ------------------------------------------------------------------------

	@Override
	protected String getRegistryInstallFolder(String subFolder, String executableName)
	{

		String path = Discoverer.getRegistryInstallFolder(executableName, subFolder, REG_SUBKEY, REG_NAME);
		return path;
	}

	// ------------------------------------------------------------------------
}
