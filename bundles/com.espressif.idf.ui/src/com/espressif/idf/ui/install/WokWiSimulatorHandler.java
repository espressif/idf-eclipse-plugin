package com.espressif.idf.ui.install;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;

import com.espressif.idf.core.ExecutableFinder;
import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.ProcessBuilderFactory;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.IDFUtil;
import com.espressif.idf.core.util.SDKConfigJsonReader;
import com.espressif.idf.ui.handlers.EclipseHandler;

public class WokWiSimulatorHandler extends AbstractHandler
{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{

		IProject project = EclipseHandler.getSelectedResource((IEvaluationContext) event.getApplicationContext())
				.getProject();

		if (project != null)
		{
			SDKConfigJsonReader sdkconfig = new SDKConfigJsonReader(project);
			String target = sdkconfig.getValue("IDF_TARGET"); //$NON-NLS-1$

			String elfFilePath = IDFUtil.getELFFilePath(project).toOSString();

			// Look for wokwi path
			String wokwiExecutablePath = "/Users/kondal/.cargo/bin/wokwi-server";
			IPath wokwiPath = ExecutableFinder.find("wokwi-server", false); //$NON-NLS-1$
			Logger.log("wokwiPath path:" + wokwiPath); //$NON-NLS-1$
			if (wokwiExecutablePath == null)
			{
				List<String> arguments = new ArrayList<String>();
				ProcessBuilderFactory processRunner = new ProcessBuilderFactory();
				try
				{
					arguments.add("whereis"); //$NON-NLS-1$
					arguments.add("wokwi-server"); //$NON-NLS-1$

					Map<String, String> environment = new HashMap<>(System.getenv());
					IStatus status = processRunner.runInBackground(arguments, org.eclipse.core.runtime.Path.ROOT,
							environment);
					if (status == null)
					{
						Logger.log(IDFCorePlugin.getPlugin(), IDFCorePlugin.errorStatus("Status can't be null", null)); //$NON-NLS-1$
					}
					String wokwiLocation = status.getMessage().split(" ").length > 1 ? status.getMessage().split(" ")[1]
							: "";
					wokwiExecutablePath = wokwiLocation.strip();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			if (wokwiExecutablePath != null)
			{
//				wokwiExecutablePath = wokwiPath.toOSString();

				List<String> arguments = new ArrayList<String>();
				arguments.add(wokwiExecutablePath);
				arguments.add("--chip");
				arguments.add(target);
				arguments.add("--id");
				arguments.add("345932416223806035");
				arguments.add(elfFilePath);

				Display.getDefault().asyncExec(new Runnable()
				{

					@Override
					public void run()
					{
						try
						{
							IStatus runInBackground = new ProcessBuilderFactory().runInBackground(arguments, wokwiPath,
									null);
							System.out.println(runInBackground.getMessage());

//							IConsole console = CCorePlugin.getDefault().getConsole();
//							console.start(project);
//							
//							ConsoleOutputStream infoStream = console.getInfoStream();
//							ConsoleOutputStream errorStream = console.getErrorStream();
//							ConsoleOutputStream outputStream = console.getOutputStream();
//							infoStream.write(runInBackground.getMessage());

						}
						catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				});

			}

		}

		return null;
	}

//	protected int watchProcess(Process process, IConsole console) throws CoreException {
//		Thread t1 = new ReaderThread(process.getInputStream(), console.getOutputStream());
//		t1.start();
//		Thread t2 = new ReaderThread(process.getErrorStream(), console.getErrorStream());
//		t2.start();
//		try {
//			int rc = process.waitFor();
//			// Allow reader threads the chance to process all output to console
//			while (t1.isAlive()) {
//				Thread.sleep(100);
//			}
//			while (t2.isAlive()) {
//				Thread.sleep(100);
//			}
//			return rc;
//		} catch (InterruptedException e) {
//			CCorePlugin.log(e);
//			return -1;
//		}
//	}

}
