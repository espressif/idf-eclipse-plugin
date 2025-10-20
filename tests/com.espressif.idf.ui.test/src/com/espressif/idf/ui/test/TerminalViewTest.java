package com.espressif.idf.ui.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCanvas;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.espressif.idf.ui.test.operations.EnvSetupOperations;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TerminalViewTest
{

	private static SWTWorkbenchBot bot;

	@BeforeClass
	public static void setup() throws Exception
	{

		bot = new SWTWorkbenchBot();
		EnvSetupOperations.setupEspressifEnv(bot);
	}

	@Test
	public void testOpenTerminalView() throws Exception
	{
		// Create a temporary file for capturing terminal output
		Path tempFile = Files.createTempFile("idf_version_output", ".txt");
		File outputFile = tempFile.toFile();
		outputFile.deleteOnExit(); // ensure cleanup after JVM exits

		bot.toolbarButtonWithTooltip("Open a Terminal (Ctrl+Alt+Shift+T)").click();
		bot.comboBox().setSelection("ESP-IDF Terminal");
		bot.button("OK").click();
		SWTBotView terminalView = bot.viewByTitle("Terminal");
		terminalView.show();
		terminalView.setFocus();

		// Wait for terminal to initialize
		bot.sleep(3000);

		// Type command into the Canvas terminal
		SWTBotCanvas canvas = terminalView.bot().canvas();
		canvas.setFocus();

		// Command with temporary file
		String cmd = "idf.py --version > " + outputFile.getAbsolutePath();
		canvas.display.syncExec(() -> {
			for (char c : cmd.toCharArray())
			{
				Event e = new Event();
				e.type = SWT.KeyDown;
				e.character = c;
				canvas.widget.notifyListeners(SWT.KeyDown, e);

				Event e2 = new Event();
				e2.type = SWT.KeyUp;
				e2.character = c;
				canvas.widget.notifyListeners(SWT.KeyUp, e2);
			}

			// Press Enter
			Event enterDown = new Event();
			enterDown.type = SWT.KeyDown;
			enterDown.character = '\r';
			canvas.widget.notifyListeners(SWT.KeyDown, enterDown);

			Event enterUp = new Event();
			enterUp.type = SWT.KeyUp;
			enterUp.character = '\r';
			canvas.widget.notifyListeners(SWT.KeyUp, enterUp);
		});

		// Wait for the command to execute
		bot.sleep(5000);

		// Read the output from the temporary file
		String output = new String(Files.readAllBytes(tempFile), StandardCharsets.UTF_8);

		assertTrue("Output should contain 'IDF v'", output.contains("IDF v"));
		// Optional: delete the temp file explicitly after test
		Files.deleteIfExists(tempFile);
	}

}
