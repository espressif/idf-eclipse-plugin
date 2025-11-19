package com.espressif.idf.ui.test.operations;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;

public class EnvSetupOperations
{
	private static final String ESP_IDF_PATH_PROPERTY = "default.env.esp.idf.path";
	private static final String GIT_PATH_PROPERTY = "default.env.esp.git.path";
	private static final String PYTHON_PATH_PROPERTY = "default.env.esp.python.path";
	private static final String PYTHON_VERSION_PROPERTY = "default.env.esp.python.version";

	private static boolean SETUP = false;

	public static void setupEspressifEnv(SWTWorkbenchBot bot) throws Exception
	{
		if (SETUP)
			return;

		Display.getDefault().syncExec(() -> {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			// Maximize (Windows/Linux/macOS windowed)
			shell.setMaximized(true);
			try
			{
				shell.setFullScreen(true);
			}
			catch (Throwable ignore)
			{
			}
		});

		for (SWTBotView view : bot.views(withPartName("Welcome")))
		{
			view.close();
		}
		
		SWTBotEditor espIdfManagerView = bot.editorByTitle("ESP-IDF Manager");
		espIdfManagerView.bot().radio(0).click();
		
		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Tools Setup complete", consoleView, 99000000);
		bot.cTabItem("ESP-IDF Manager").activate();
		bot.cTabItem("ESP-IDF Manager").close();
		
		bot.menu("Window").menu("Perspective").menu("Open Perspective").menu("Other...").click();
		bot.table().select("C/C++");
		bot.button("Open").click();

		bot.menu("Window").menu("Preferences...").click();
		SWTBotShell prefrencesShell = bot.shell("Preferences");

		prefrencesShell.bot().tree().getTreeItem("General").select();
		prefrencesShell.bot().tree().getTreeItem("General").expand();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").select();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").expand();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").getNode("File Associations").select();
		prefrencesShell.bot().comboBox().setSelection("Plain Text Editor");
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Workspace").select();
		if (!prefrencesShell.bot().checkBox("Refresh using native hooks or polling").isChecked())
		{
			prefrencesShell.bot().checkBox("Refresh using native hooks or polling").click();
		}
		prefrencesShell.bot().button("Apply and Close").click();

		bot.toolbarButtonWithTooltip("Select and deselect filters to apply to the content in the tree").click();
		bot.table().getTableItem(".* resources").uncheck();
		bot.button("OK").click();

		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.text().setText("progress");
		bot.button("Open").click();
		bot.viewByTitle("Progress").show();
		SETUP = true;
	}

}
