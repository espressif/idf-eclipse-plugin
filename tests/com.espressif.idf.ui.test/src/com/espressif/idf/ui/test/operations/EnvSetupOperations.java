package com.espressif.idf.ui.test.operations;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import com.espressif.idf.ui.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;

public class EnvSetupOperations
{
	private static final String ESP_IDF_PATH_PROPERTY = "default.env.esp.idf.path";
	private static final String GIT_PATH_PROPERTY = "default.env.esp.git.path";
	private static final String PYTHON_PATH_PROPERTY = "default.env.esp.python.path";
	private static final String PYTHON_VERSION_PROPERTY = "default.env.esp.python.version";

	public static void setupEspressifEnv(SWTWorkbenchBot bot) throws Exception
	{
		for (SWTBotView view : bot.views(withPartName("Welcome")))
		{
			view.close();
		}
		bot.menu("Window").menu("Perspective").menu("Open Perspective").menu("Other...").click();
		bot.table().select("C/C++");
		bot.button("Open").click();
		bot.menu("Espressif").menu("ESP-IDF Tools Manager").menu("Install Tools").click();
		bot.sleep(5500);
		bot.textWithLabel("ESP-IDF Directory:", 1)
				.setText(DefaultPropertyFetcher.getStringPropertyValue(ESP_IDF_PATH_PROPERTY, ""));
		bot.textWithLabel("Git Executable Location:")
				.setText(DefaultPropertyFetcher.getStringPropertyValue(GIT_PATH_PROPERTY, ""));
		try
		{
			bot.comboBox().setSelection(DefaultPropertyFetcher.getStringPropertyValue(PYTHON_VERSION_PROPERTY, ""));
		}
		catch (WidgetNotFoundException e)
		{
			bot.textWithLabel("Python Executable Location:")
			.setText(DefaultPropertyFetcher.getStringPropertyValue(PYTHON_PATH_PROPERTY, ""));
		}
		bot.button("Install Tools").click();
		SWTBotView consoleView = bot.viewById("org.eclipse.ui.console.ConsoleView");
		consoleView.show();
		consoleView.setFocus();
		TestWidgetWaitUtility.waitUntilViewContains(bot, "Install tools completed", consoleView, 60000);
	}

}
