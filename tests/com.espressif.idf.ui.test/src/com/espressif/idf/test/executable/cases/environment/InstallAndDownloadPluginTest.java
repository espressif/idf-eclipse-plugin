package com.espressif.idf.test.executable.cases.environment;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withTextIgnoringCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.sphinx.platform.IExtendedPlatformConstants;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.test.common.configs.DefaultPropertyFetcher;
import com.espressif.idf.test.common.utility.TestAssertUtility;
import com.espressif.idf.test.operations.ProjectTestOperations;

@RunWith(SWTBotJunit4ClassRunner.class)
public class InstallAndDownloadPluginTest
{
	private Fixture fixture;
	
	@Before
	public void beforeEachTest() throws Exception
	{
		fixture = new Fixture();
	}

	@After
	public void afterEachTest()
	{
		fixture.cleanTestEnv();
	}
	
	@Test
	public void installPluginFromTheGlobalUrl() throws Exception
	{
		fixture.givenUrlsAreLoaded();
		fixture.whenPluginsInstallationIsTried();
		fixture.thenWillNotBeInstalledIsNotPresent();
	}

	private class Fixture
	{
		private static final String STABLE_DOWNLOAD_URL_PROPERTY = "default.download.url.stable";
		private static final String BETA_DOWNLOAD_URL_PROPERTY = "default.download.url.beta";
		private SWTWorkbenchBot bot;
		private String stableUrl;
		private String betaUrl;

		private Fixture()
		{
			bot = new SWTWorkbenchBot();
			for (SWTBotView view : bot.views(withPartName("Welcome")))
			{
				view.close();
			}
			bot.menu("Window").menu("Perspective").menu("Open Perspective").menu("Other...").click();
			bot.table().select("C/C++");
			bot.button("Open").click();
		}
		
		private void whenPluginsInstallationIsTried() throws Exception
		{
			bot.menu("Help").menu("Install New Software...").click();
			SWTBotShell installShell = bot.shell("Install");
			installShell.bot().button("Add...").click();
			installShell.bot().shell("Add Repository").bot().textWithLabel("&Location:").setText(stableUrl);
			installShell.bot().shell("Add Repository").bot().textWithLabel("&Name:").setText("ESP");
			installShell.bot().shell("Add Repository").bot().button("Add").click();
			Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_MODEL_LOADING, new NullProgressMonitor());
			installShell.bot().tree().getTreeItem("Espressif IDF").select();
			installShell.bot().tree().getTreeItem("Espressif IDF").expand();
			installShell.bot().tree().getTreeItem("Espressif IDF").toggleCheck();
			installShell.bot().button("Next >").click();
			Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_AUTOMATIC_VALIDATION, new NullProgressMonitor());
			Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_LONG_RUNNING, new NullProgressMonitor());
			Job.getJobManager().join(IExtendedPlatformConstants.FAMILY_MODEL_LOADING, new NullProgressMonitor());
//			installShell.bot().widget(withTextIgnoringCase("Install Remediation Page"));
//			Control[] controls = installShell.widget.getChildren();
			
		}

		private void givenUrlsAreLoaded() throws Exception
		{
			stableUrl = DefaultPropertyFetcher.getStringPropertyValue(STABLE_DOWNLOAD_URL_PROPERTY, "");
			betaUrl = DefaultPropertyFetcher.getStringPropertyValue(BETA_DOWNLOAD_URL_PROPERTY, "");
		}
		
		private void thenWillNotBeInstalledIsNotPresent()
		{
			bot.sleep(3000);
			SWTBotShell installShell = bot.shell("Install");
			boolean found = true;
			try
			{
				installShell.bot().label("Install Remediation Page");				
			}
			catch (WidgetNotFoundException e)
			{
				found = false;
			}
			
			
			assertFalse(found);
		}

		private void cleanTestEnv()
		{
			ProjectTestOperations.closeAllProjects(bot);
			ProjectTestOperations.deleteAllProjects(bot);
		}
	}

}
