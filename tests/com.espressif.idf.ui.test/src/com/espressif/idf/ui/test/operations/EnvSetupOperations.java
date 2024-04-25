package com.espressif.idf.ui.test.operations;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import com.espressif.idf.core.util.IDFUtil;
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

		for (SWTBotView view : bot.views(withPartName("Welcome")))
		{
			view.close();
		}
		bot.menu("Window").menu("Perspective").menu("Open Perspective").menu("Other...").click();
		bot.table().select("C/C++");
		bot.button("Open").click();

		bot.menu("Window").menu("Preferences").click();
		bot.tree().getTreeItem("General").select();
		bot.tree().getTreeItem("General").expand();
		bot.tree().getTreeItem("General").getNode("Editors").select();
		bot.tree().getTreeItem("General").getNode("Editors").expand();
		bot.tree().getTreeItem("General").getNode("Editors").getNode("File Associations").select();
		bot.comboBox().setSelection("Text Editor");
		bot.tree().getTreeItem("General").getNode("Workspace").select();
		if (bot.checkBox("Refresh using native hooks or polling").isChecked())
		{
			bot.checkBox("Refresh using native hooks or polling").click();
		}
		
		bot.tree().getTreeItem("C/C++").select();
		bot.tree().getTreeItem("C/C++").expand();
		bot.tree().getTreeItem("C/C++").getNode("Indexer").select();
		if (bot.checkBox("Enable indexer").isChecked())
		{
			bot.checkBox("Enable indexer").click();	
		}
		
		bot.button("Apply and Close").click();

		bot.toolbarButtonWithTooltip("Select and deselect filters to apply to the content in the tree").click();
		bot.table().getTableItem(".* resources").uncheck();
		bot.button("OK").click();

		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.text().setText("progress");
		bot.button("Open").click();
		bot.viewByTitle("Progress").show();

		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		bot.activeShell();

		bot.menu("Espressif").menu("ESP-IDF Tools Manager").click().menu("Install Tools").click();
		bot.activeShell().activate();
		bot.shell("Install Tools").bot().textWithLabel("ESP-IDF Directory:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_PATH_PROPERTY, ""));

		bot.shell("Install Tools").bot().textWithLabel("Git Executable Location:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(GIT_PATH_PROPERTY, ""));
		try
		{
			bot.shell("Install Tools").bot().comboBox()
					.setSelection(DefaultPropertyFetcher.getStringPropertyValue(PYTHON_VERSION_PROPERTY, ""));
		}
		catch (WidgetNotFoundException e)
		{
			bot.shell("Install Tools").bot().textWithLabel("Python Executable Location:")
					.setText(IDFUtil.getPythonExecutable());

		}
		bot.shell("Install Tools").bot().button("Install Tools").click();
		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 99000000);
		SETUP = true;
	}

}
