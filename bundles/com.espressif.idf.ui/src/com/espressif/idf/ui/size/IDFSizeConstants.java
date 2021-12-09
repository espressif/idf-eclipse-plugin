package com.espressif.idf.ui.size;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.update.Messages;

public class IDFSizeConstants {

	public static String DATA = "data"; // DRAM .data //$NON-NLS-1$
	public static String BSS = "bss"; // DRAM .bss //$NON-NLS-1$
	public static String IRAM = "iram"; //$NON-NLS-1$
	public static String DIRAM = "diram"; //$NON-NLS-1$
	public static String FLASH_TEXT = "flash_text"; //$NON-NLS-1$
	public static String FLASH_RODATA = "flash_rodata"; //$NON-NLS-1$
	public static String OTHER = "other"; //$NON-NLS-1$
	public static String TOTAL = "total"; //$NON-NLS-1$

	//idf_size.py overview constants
	public static String FLASH_RODATA_OVERVIEW = "flash_rodata"; //$NON-NLS-1$
	public static String DRAM_DATA = "dram_data"; //$NON-NLS-1$
	public static String DRAM_BSS = "dram_bss"; //$NON-NLS-1$
	public static String FLASH_CODE = "flash_code"; //$NON-NLS-1$
	public static String TOTAL_SIZE = "total_size"; //$NON-NLS-1$
	public static String USED_IRAM = "used_iram"; //$NON-NLS-1$
	public static String AVAILABLE_IRAM = "available_iram"; //$NON-NLS-1$
	public static String USED_IRAM_RATIO = "used_iram_ratio"; //$NON-NLS-1$
	public static String USED_DRAM = "used_dram"; //$NON-NLS-1$
	public static String AVAILABLE_DRAM = "available_dram"; //$NON-NLS-1$
	public static String USED_DRAM_RATIO = "used_dram_ratio"; //$NON-NLS-1$
	
	//esp32-s2 specific
	public static String USED_DIRAM = "used_diram"; //$NON-NLS-1$
	public static String AVAILABLE_DIRAM = "available_diram"; //$NON-NLS-1$
	public static String USED_DIRAM_RATIO = "used_diram_ratio"; //$NON-NLS-1$
	
	static {
		String versing = getEspIdfVersion();
		Pattern p = Pattern.compile("([0-9][.][0-9])"); //$NON-NLS-1$
		Matcher m = p.matcher(versing);
		if (m.find() && Double.parseDouble(m.group(0)) > 4.3) {
			FLASH_RODATA_OVERVIEW = "flash_rodata"; //$NON-NLS-1$
			DATA = ".dram0.data"; // DRAM .data //$NON-NLS-1$
			BSS = ".dram0.bss"; // DRAM .bss //$NON-NLS-1$
			IRAM = ".iram0.text"; //$NON-NLS-1$
			DIRAM = "diram"; //$NON-NLS-1$
			FLASH_TEXT = ".flash.text"; //$NON-NLS-1$
			FLASH_RODATA = ".flash.rodata"; //$NON-NLS-1$
			OTHER = "ram_st_total"; //$NON-NLS-1$
			TOTAL = "flash_total"; //$NON-NLS-1$

			//idf_size.py overview constants
			DRAM_DATA = "dram_data"; //$NON-NLS-1$
			DRAM_BSS = "dram_bss"; //$NON-NLS-1$
			FLASH_CODE = "flash_code"; //$NON-NLS-1$
			TOTAL_SIZE = "total_size"; //$NON-NLS-1$
			USED_IRAM = "used_iram"; //$NON-NLS-1$
			AVAILABLE_IRAM = "iram_remain"; //$NON-NLS-1$
			USED_IRAM_RATIO = "used_iram_ratio"; //$NON-NLS-1$
			USED_DRAM = "used_dram"; //$NON-NLS-1$
			AVAILABLE_DRAM = "dram_remain"; //$NON-NLS-1$
			USED_DRAM_RATIO = "used_dram_ratio"; //$NON-NLS-1$
			
			//esp32-s2 specific
			USED_DIRAM = "used_diram"; //$NON-NLS-1$
			AVAILABLE_DIRAM = "diram_remain"; //$NON-NLS-1$
			USED_DIRAM_RATIO = "used_diram_ratio"; //$NON-NLS-1$
		}
	}
	
	private static String getEspIdfVersion()
	{
		if (IDFUtil.getIDFPath() != null && IDFUtil.getIDFPythonEnvPath() != null)
		{
			List<String> commands = new ArrayList<>();
			commands.add(IDFUtil.getIDFPythonEnvPath());
			commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
			commands.add("--version"); //$NON-NLS-1$
			Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
			return runCommand(commands, envMap);
		}
		
		return ""; //$NON-NLS-1$
	}
	
	private static String runCommand(List<String> arguments, Map<String, String> env)
	{
		String exportCmdOp = ""; //$NON-NLS-1$
		ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
		try
		{
			IStatus status = processRunner.runInBackground(arguments, Path.ROOT, env);
			if (status == null)
			{
				Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$

			}

			// process export command output
			exportCmdOp = status.getMessage();
			Logger.log(exportCmdOp);
		}
		catch (Exception e1)
		{
			Logger.log(IDFCorePlugin.getPlugin(), e1);
		}
		return exportCmdOp;
	}

}
