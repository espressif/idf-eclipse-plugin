package com.espressif.idf.ui.help;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.ui.update.ListInstalledToolsHandler;
import com.espressif.idf.ui.update.Messages;


public class ProductInformationHandler extends ListInstalledToolsHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		super.execute(event);
		console.println(Messages.JavaRuntimeVersionMsg + System.getProperty("java.runtime.version"));
		console.println(Messages.OperationSystemMsg + System.getProperty("os.name").toLowerCase());
		console.println(Messages.EclipseCDTMsg + Platform.getBundle("org.eclipse.cdt").getVersion().toString());
		console.println(Messages.PythonIdfEnvMsg + getPythonExeVersion(IDFUtil.getIDFPythonEnvPath()));
		console.println(Messages.PythonPathMsg + getPythonExeVersion("python"));
		showEclipseVersion();
		showEspIdfVersion();
		return null;
	}

	private void showEspIdfVersion()
	{
		List<String> commands = new ArrayList<>();
		commands.add(IDFUtil.getIDFPythonEnvPath());
		commands.add(IDFUtil.getIDFPythonScriptFile().getAbsolutePath());
		commands.add("--version"); //$NON-NLS-1$
		Map<String, String> envMap = new IDFEnvironmentVariables().getEnvMap();
		console.println(runCommand(commands, envMap));
	}

	private String getPythonExeVersion(String pythonExePath)
	{
		List<String> commands = new ArrayList<>();
		commands.add(pythonExePath);
		commands.add("--version"); //$NON-NLS-1$
		return runCommand(commands, System.getenv());
	}

	private String runCommand(List<String> arguments, Map<String, String> env)
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

	private void showEclipseVersion()
	{
		Wini ini;
		try
		{
			ini = new Wini(new File(Platform.getInstallLocation().getURL().getPath() + "configuration/config.ini"));
			console.println(Messages.EclipseMsg + ini.get("?", "eclipse.buildId"));
		}
		catch (InvalidFileFormatException e)
		{
			Logger.log(e);
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

}
