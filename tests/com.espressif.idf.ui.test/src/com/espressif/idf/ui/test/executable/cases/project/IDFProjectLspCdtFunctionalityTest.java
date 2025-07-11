/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.test.executable.cases.project;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotStyledText;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import com.espressif.idf.ui.test.common.WorkBenchSWTBot;
import com.espressif.idf.ui.test.common.utility.TestWidgetWaitUtility;
import com.espressif.idf.ui.test.operations.EnvSetupOperations;
import com.espressif.idf.ui.test.operations.ProjectTestOperations;

/**
 * Test LSP features like content assist (proposals), hover and diagnostics.
 */
@SuppressWarnings("restriction")
@RunWith(SWTBotJunit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IDFProjectLspCdtFunctionalityTest
{

	private static SWTWorkbenchBot bot;
	private static final String PROJECT_NAME = "LspTestProject"; //$NON-NLS-1$

	@BeforeClass
	public static void setupEnv() throws Exception
	{
		bot = WorkBenchSWTBot.getBot();
		EnvSetupOperations.setupEspressifEnv(bot);
		ProjectTestOperations.deleteAllProjects(bot);
		ProjectTestOperations.setupProjectWithReconfigureCommand(PROJECT_NAME, "EspressIf", "Espressif IDF Project", //$NON-NLS-1$ //$NON-NLS-2$
				bot);
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
	}

	@AfterClass
	public static void tearDown()
	{
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishAsync(bot);
		ProjectTestOperations.closeAllProjects(bot);
		ProjectTestOperations.deleteAllProjects(bot);
	}

	@Test
	public void test_ContentAssist_ProposalsAppear()
	{
		openMainCFile();

		SWTBotEditor editor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		editor.setFocus();
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		editor.toTextEditor().insertText(5, 5, "\n"); //$NON-NLS-1$
		editor.toTextEditor().click(5, 5);
		List<String> proposals = editor.toTextEditor().getAutoCompleteProposals(""); //$NON-NLS-1$
		bot.sleep(3000);

		assertTrue("Expected LSP proposals not found", !proposals.isEmpty()); //$NON-NLS-1$
	}

	@Test
	public void test_HoverInformationAppears() throws ParseException
	{
		openMainCFile();
		SWTBotEditor editor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		editor.setFocus();
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);

		bot.sleep(2000);
		editor.toTextEditor().navigateTo(0, 5);
		editor.toTextEditor().pressShortcut(KeyStroke.getInstance("F2")); //$NON-NLS-1$
		bot.sleep(2000);

		// lsp hover contains the code field
		boolean hoverShown = editor.toTextEditor().bot().shell("").bot().browser().getText().contains("code"); //$NON-NLS-1$ //$NON-NLS-2$

		assertTrue("Hover documentation not shown", hoverShown); //$NON-NLS-1$
	}

	@Test
	public void test_Diagnostics_NoErrorThenErrorAppears()
	{
		openMainCFile();

		SWTBotEditor editor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		editor.setFocus();
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);

		editor.toTextEditor().insertText("\n"); //$NON-NLS-1$
		editor.save();
		bot.sleep(4000);

		SWTBotView problemsView = bot.views().stream().filter(v -> v.getTitle().toLowerCase().contains("problem")) //$NON-NLS-1$
				.findFirst().orElseThrow(() -> new RuntimeException("Problems view not found")); //$NON-NLS-1$

		problemsView.show();
		problemsView.setFocus();

		assertTrue("No error expected, but found some markers", problemsView.bot().tree().rowCount() == 0); //$NON-NLS-1$

		editor.toTextEditor().insertText("\nundefined_function();\n"); //$NON-NLS-1$
		editor.save();
		bot.sleep(4000);

		assertTrue("Expected error marker not found after inserting invalid code", //$NON-NLS-1$
				problemsView.bot().tree().rowCount() != 0);
	}

	@Test
	public void test_SyntaxHighlighting_ColorsApplied()
	{
		openMainCFile();

		SWTBotEditor botEditor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		botEditor.setFocus();
		TestWidgetWaitUtility.waitForOperationsInProgressToFinishSync(bot);
		bot.sleep(2000); // allow LSP highlight to apply

		SWTBotStyledText botStyledText = botEditor.toTextEditor().getStyledText();
		StyledText styledText = botStyledText.widget;

		final StyleRange[][] styleRangesHolder = new StyleRange[1][];
		Display.getDefault().syncExec(() -> styleRangesHolder[0] = styledText.getStyleRanges());

		StyleRange[] ranges = styleRangesHolder[0];

		boolean foundColoredText = false;
		for (StyleRange range : ranges)
		{
			if (range.foreground != null || range.background != null || range.fontStyle != SWT.NORMAL)
			{
				foundColoredText = true;
				break;
			}
		}

		assertTrue("Expected some styled (highlighted) text ranges, but none were found", foundColoredText); //$NON-NLS-1$
	}

	private static void openMainCFile()
	{
		SWTBotEditor editor;
		try
		{
			editor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			bot.viewByTitle("Project Explorer").show(); //$NON-NLS-1$
			bot.tree().getTreeItem(PROJECT_NAME).expand().getNode("main").expand().getNode("main.c").doubleClick(); //$NON-NLS-1$ //$NON-NLS-2$
			editor = bot.editorByTitle("main.c"); //$NON-NLS-1$
		}
		editor.show();
		editor.setFocus();
	}
}
