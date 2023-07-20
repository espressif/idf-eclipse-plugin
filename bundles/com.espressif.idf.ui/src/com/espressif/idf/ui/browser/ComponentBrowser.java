package com.espressif.idf.ui.browser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


public class ComponentBrowser
{
	private String htmltext = "<!DOCTYPE html>\n"
			+ "<html lang=\"en\">\n"
			+ "\n"
			+ "<head>\n"
			+ "  <meta charset=\"UTF-8\">\n"
			+ "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
			+ "  <title>IDF Component Registry</title>\n"
			+ "  <style>\n"
			+ "    body {\n"
			+ "      margin: 0px;\n"
			+ "      padding: 0px;\n"
			+ "      overflow: hidden;\n"
			+ "    }\n"
			+ "\n"
			+ "    iframe {\n"
			+ "      overflow: hidden;\n"
			+ "      overflow-x: hidden;\n"
			+ "      overflow-y: hidden;\n"
			+ "      height: 100%;\n"
			+ "      width: 100%;\n"
			+ "      position: absolute;\n"
			+ "      top: 0px;\n"
			+ "      left: 0px;\n"
			+ "      right: 0px;\n"
			+ "      bottom: 0px\n"
			+ "    }\n"
			+ "  </style>\n"
			+ "</head>\n"
			+ "\n"
			+ "<body>\n"
			+ "  <iframe src=\"https://components.espressif.com/\" frameborder=\"0\" width=\"100%\" height=\"100%\"></iframe>\n"
			+ "  <script>\n"
			+ "    const vscode = acquireVsCodeApi()\n"
			+ "    window.addEventListener(\"message\", (ev) => {\n"
			+ "      vscode.postMessage(ev.data);\n"
			+ "    })\n"
			+ "  </script>\n"
			+ "</body>\n"
			+ "\n"
			+ "</html>";
	
	public void createBrowser(Composite composite) throws ClassNotFoundException
	{
		Browser browser = new Browser(composite, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.setText(htmltext, true);

	}
}