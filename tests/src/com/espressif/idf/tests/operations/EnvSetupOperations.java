package com.espressif.idf.tests.operations;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;

import com.espressif.idf.tests.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.tests.common.utility.TestWidgetWaitUtility;

public class EnvSetupOperations
{
	private static final String ESP_IDF_PATH_PROPERTY = "default.env.esp.idf.path";
	private static final String GIT_PATH_PROPERTY = "default.env.esp.git.path";
	private static final String PYTHON_VERSION_PROPERTY = "default.env.esp.python.version";

	public static void setupEspressifEnv(SWTWorkbenchBot bot) throws Exception
	{
		bot.viewByTitle("Welcome").close();
		bot.menu("Help").menu("ESP-IDF Tools Manager").menu("Install Tools").click();
		bot.textWithLabel("ESP-IDF Directory:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_PATH_PROPERTY, ""));
		bot.textWithLabel("Git Executable Location:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(GIT_PATH_PROPERTY, ""));
		bot.comboBox().setSelection(DefaultPropertyFetcher.getStringPropertyValue(PYTHON_VERSION_PROPERTY, ""));
		bot.button("Install Tools").click();
		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 60000);
	}

}
