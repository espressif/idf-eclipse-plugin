package com.espressif.idf.ui.test.operations;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

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
		SWTBotShell prefrencesShell = bot.shell("Preferences");

		prefrencesShell.bot().tree().getTreeItem("General").select();
		prefrencesShell.bot().tree().getTreeItem("General").expand();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").select();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").expand();
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Editors").getNode("File Associations").select();
		prefrencesShell.bot().comboBox().setSelection("Text Editor");
		prefrencesShell.bot().tree().getTreeItem("General").getNode("Workspace").select();
		if (!prefrencesShell.bot().checkBox("Refresh using native hooks or polling").isChecked())
		{
			prefrencesShell.bot().checkBox("Refresh using native hooks or polling").click();
		}

		prefrencesShell.bot().tree().getTreeItem("C/C++").select();
		prefrencesShell.bot().tree().getTreeItem("C/C++").expand();
		prefrencesShell.bot().tree().getTreeItem("C/C++").getNode("Indexer").select();
		if (prefrencesShell.bot().checkBox("Enable indexer").isChecked())
		{
			prefrencesShell.bot().checkBox("Enable indexer").click();
		}

		prefrencesShell.bot().button("Apply and Close").click();

		bot.toolbarButtonWithTooltip("Select and deselect filters to apply to the content in the tree").click();
		bot.table().getTableItem(".* resources").uncheck();
		bot.button("OK").click();

		bot.menu("Window").menu("Show View").menu("Other...").click();
		bot.text().setText("progress");
		bot.button("Open").click();
		bot.viewByTitle("Progress").show();

		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		bot.activeShell();

		bot.menu("Espressif").menu("ESP-IDF Manager").click();
		bot.activeShell().activate();
		bot.button("Add ESP-IDF").click();
		SWTBotShell espIdfConfigShell = bot.shell("ESP-IDF Configuration");
		espIdfConfigShell.setFocus();
		espIdfConfigShell.bot().checkBox("Use an existing ESP-IDF directory from file system").click();
		espIdfConfigShell.bot().textWithLabel("Choose existing ESP-IDF directory:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_PATH_PROPERTY, ""));
		espIdfConfigShell.bot().textWithLabel("Git: ")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(GIT_PATH_PROPERTY, ""));
		espIdfConfigShell.bot().textWithLabel("Python: ")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(PYTHON_PATH_PROPERTY, ""));
		espIdfConfigShell.bot().button("Finish").click();

		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Tools Activated", consoleView, 99000000);
		SETUP = true;
	}

}
