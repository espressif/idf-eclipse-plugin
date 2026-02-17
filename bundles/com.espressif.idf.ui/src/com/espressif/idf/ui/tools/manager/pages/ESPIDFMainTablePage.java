/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.manager.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.console.MessageConsoleStream;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.tools.EimConstants;
import com.espressif.idf.core.tools.EimIdfConfiguratinParser;
import com.espressif.idf.core.tools.SetupToolsInIde;
import com.espressif.idf.core.tools.ToolInitializer;
import com.espressif.idf.core.tools.exceptions.EimVersionMismatchException;
import com.espressif.idf.core.tools.util.ToolsUtility;
import com.espressif.idf.core.tools.vo.EimJson;
import com.espressif.idf.core.tools.vo.IdfInstalled;
import com.espressif.idf.ui.IDFConsole;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.EimButtonLaunchListener;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.SetupToolsJobListener;

/**
 * Main UI class for all listing and interacting with the tools
 * 
 * @author Ali Azam Rana
 *
 */
public class ESPIDFMainTablePage
{
	private Composite container;
	private Button eimLaunchBtn;
	private Browser browser;
	private List<IdfInstalled> idfInstalledList;
	private static EimJson eimJson;
	private EimIdfConfiguratinParser eimIdfConfiguratinParser;
	private ToolInitializer toolInitializer;
	private Map<String, Boolean> activatingStates = new HashMap<>();

	private static ESPIDFMainTablePage espidfMainTablePage;

	private ESPIDFMainTablePage()
	{
		eimIdfConfiguratinParser = new EimIdfConfiguratinParser();
		toolInitializer = new ToolInitializer(
				org.eclipse.core.runtime.preferences.InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID));
	}

	public static ESPIDFMainTablePage getInstance(EimJson eimJson)
	{
		if (espidfMainTablePage == null)
		{
			espidfMainTablePage = new ESPIDFMainTablePage();
		}

		ESPIDFMainTablePage.eimJson = eimJson;
		return espidfMainTablePage;
	}

	public Composite createPage(Composite composite)
	{
		idfInstalledList = eimJson != null ? eimJson.getIdfInstalled() : null;
		container = new Composite(composite, SWT.NONE);
		final int numColumns = 1;
		GridLayout gridLayout = new GridLayout(numColumns, false);
		container.setLayout(gridLayout);
		createEimLaunchButton(container);
		createIdfWebView(container);
		return container;
	}

	private void createEimLaunchButton(Composite composite)
	{
		eimLaunchBtn = new Button(composite, SWT.PUSH);
		eimLaunchBtn.setText(
				!toolInitializer.isEimInstalled() ? Messages.EIMButtonDownloadText : Messages.EIMButtonLaunchText);
		eimLaunchBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		eimLaunchBtn.addSelectionListener(new EimButtonLaunchListener(espidfMainTablePage, Display.getDefault(),
				getConsoleStream(false), getConsoleStream(true)));
	}

	public void setupInitialEspIdf()
	{
		if (idfInstalledList != null && idfInstalledList.size() == 1)
		{
			// activate the only available esp-idf first check if its not already active
			Preferences scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
			if (!scopedPreferenceStore.getBoolean(EimConstants.INSTALL_TOOLS_FLAG, false))
			{
				SetupToolsInIde setupToolsInIde = new SetupToolsInIde(idfInstalledList.get(0), eimJson,
						getConsoleStream(true), getConsoleStream(false));
				SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(ESPIDFMainTablePage.this,
						setupToolsInIde);
				setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
				setupToolsInIde.schedule();
			}
		}
	}

	public void refreshEditorUI()
	{
		if (container == null || container.isDisposed())
			return;

		Display.getDefault().asyncExec(() -> {
			if (container.isDisposed() || browser == null || browser.isDisposed())
				return;

			try
			{
				try
				{
					eimJson = eimIdfConfiguratinParser.getEimJson(true);
				}
				catch (EimVersionMismatchException e)
				{
					Logger.log(e);
					MessageDialog.openError(Display.getDefault().getActiveShell(), e.msgTitle(), e.getMessage());
					return;
				}
				// eimJson is null if EIM was closed before tool installation completed
				if (eimJson == null)
				{
					return;
				}
			}
			catch (IOException e)
			{
				Logger.log(e);
				return;
			}

			idfInstalledList = eimJson.getIdfInstalled();
			updateWebView();
			eimLaunchBtn.setText(
					!toolInitializer.isEimInstalled() ? Messages.EIMButtonDownloadText : Messages.EIMButtonLaunchText);
		});
	}

	private void createIdfWebView(Composite parent)
	{
		Group idfToolsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		idfToolsGroup.setText("IDF Tools"); //$NON-NLS-1$
		idfToolsGroup.setLayout(new FillLayout());
		idfToolsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		try
		{
			browser = new Browser(idfToolsGroup, SWT.NONE);
			
			// Add progress listener to wait for page to load
			final boolean[] isUsingSetText = new boolean[1];
			browser.addProgressListener(new ProgressListener()
			{
				@Override
				public void changed(ProgressEvent event)
				{
					// Page loading progress changed
				}

				@Override
				public void completed(ProgressEvent event)
				{
					// Page finished loading, now setup bridge and update view
					Display.getDefault().asyncExec(() -> {
						if (!browser.isDisposed())
						{
							setupJavaScriptBridge();
							// Small delay to ensure JavaScript is ready
							Display.getDefault().timerExec(100, () -> {
								if (!browser.isDisposed())
								{
									updateWebView();
								}
							});
						}
					});
				}
			});
			
			loadHtmlContent(isUsingSetText);
			
			// If we used setText, the progress listener won't fire, so setup immediately
			if (isUsingSetText[0])
			{
				Display.getDefault().timerExec(200, () -> {
					if (!browser.isDisposed())
					{
						setupJavaScriptBridge();
						Display.getDefault().timerExec(100, () -> {
							if (!browser.isDisposed())
							{
								updateWebView();
							}
						});
					}
				});
			}
		}
		catch (Exception e)
		{
			Logger.log("Failed to create browser widget: " + e.getMessage()); //$NON-NLS-1$
			Logger.log(e);
		}
	}

	private void loadHtmlContent(boolean[] isUsingSetText)
	{
		try
		{
			Bundle bundle = FrameworkUtil.getBundle(ESPIDFMainTablePage.class);
			// Try multiple paths to find the HTML file
			URL htmlUrl = FileLocator.find(bundle, new Path("idf-manager.html"), null); //$NON-NLS-1$
			
			if (htmlUrl == null)
			{
				htmlUrl = FileLocator.find(bundle, new Path("src/com/espressif/idf/ui/tools/manager/pages/idf-manager.html"), null); //$NON-NLS-1$
			}
			
			if (htmlUrl == null)
			{
				// Try as resource entry
				htmlUrl = bundle.getEntry("idf-manager.html"); //$NON-NLS-1$
			}

			if (htmlUrl != null)
			{
				try
				{
					URL fileUrl = FileLocator.toFileURL(htmlUrl);
					Logger.log("Loading HTML from URL: " + fileUrl.toString()); //$NON-NLS-1$
					browser.setUrl(fileUrl.toString());
				}
				catch (Exception e)
				{
					Logger.log("Failed to convert URL to file URL, using text content: " + e.getMessage()); //$NON-NLS-1$
					// Fallback to text content
					String htmlContent = loadHtmlFromResource();
					if (htmlContent != null && !htmlContent.isEmpty())
					{
						browser.setText(htmlContent);
						isUsingSetText[0] = true;
					}
					else
					{
						browser.setText(getEmbeddedHtml());
						isUsingSetText[0] = true;
					}
				}
			}
			else
			{
				Logger.log("HTML URL not found, loading from resource stream"); //$NON-NLS-1$
				// Fallback: load HTML as string from resource
				String htmlContent = loadHtmlFromResource();
				if (htmlContent != null && !htmlContent.isEmpty())
				{
					browser.setText(htmlContent);
					isUsingSetText[0] = true;
				}
				else
				{
					Logger.log("Using embedded HTML fallback"); //$NON-NLS-1$
					// Ultimate fallback: embedded HTML
					browser.setText(getEmbeddedHtml());
					isUsingSetText[0] = true;
				}
			}
		}
		catch (Exception e)
		{
			Logger.log("Error loading HTML content: " + e.getMessage()); //$NON-NLS-1$
			Logger.log(e);
			String htmlContent = loadHtmlFromResource();
			if (htmlContent != null && !htmlContent.isEmpty())
			{
				browser.setText(htmlContent);
			}
			else
			{
				browser.setText(getEmbeddedHtml());
			}
		}
	}

	private String loadHtmlFromResource()
	{
		try
		{
			Bundle bundle = FrameworkUtil.getBundle(ESPIDFMainTablePage.class);
			InputStream inputStream = null;
			
			// Try different paths
			URL entry = bundle.getEntry("idf-manager.html"); //$NON-NLS-1$
			if (entry != null)
			{
				inputStream = entry.openStream();
			}
			
			if (inputStream == null)
			{
				entry = bundle.getEntry("src/com/espressif/idf/ui/tools/manager/pages/idf-manager.html"); //$NON-NLS-1$
				if (entry != null)
				{
					inputStream = entry.openStream();
				}
			}

			if (inputStream != null)
			{
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) //$NON-NLS-1$
				{
					return reader.lines().collect(Collectors.joining("\n")); //$NON-NLS-1$
				}
			}
		}
		catch (Exception e)
		{
			Logger.log("Error reading HTML resource: " + e.getMessage()); //$NON-NLS-1$
			Logger.log(e);
		}
		return null;
	}
	
	private String getEmbeddedHtml()
	{
		// Return a minimal HTML that will be replaced by updateWebView
		// This is a fallback if the HTML file cannot be loaded
		return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>ESP-IDF Manager</title><style>body{font-family:Arial,sans-serif;padding:20px;background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);min-height:100vh;color:#333}.container{max-width:1200px;margin:0 auto}.header{text-align:center;color:white;margin-bottom:30px}.guide-section{background:rgba(255,255,255,0.15);border-radius:12px;padding:20px;margin-bottom:25px}.guide-text{color:white;font-size:0.95em;line-height:1.6;margin:0;text-align:center}.guide-link{color:#ffd700;text-decoration:none;font-weight:600}.cards-container{display:grid;grid-template-columns:repeat(auto-fill,minmax(400px,1fr));gap:25px}.card{background:white;border-radius:16px;padding:25px;box-shadow:0 10px 30px rgba(0,0,0,0.2)}.empty-state{text-align:center;padding:60px 20px;color:white}</style></head><body><div class='container'><div class='header'><h1>🚀 ESP-IDF Manager</h1><p>Manage and activate your ESP-IDF installations</p></div><div class='guide-section'><p class='guide-text'>Select the version of ESP-IDF you want to use. Click the <strong>Activate</strong> button next to the version you want. For help in choosing the correct version, visit the <a href='https://dl.espressif.com/dl/esp-idf/support-periods.svg' target='_blank' class='guide-link'>ESP-IDF Version Guide</a>.</p></div><div id='cardsContainer' class='cards-container'></div><div id='emptyState' class='empty-state' style='display:none'><h2>No ESP-IDF installations found</h2><p>Launch ESP-IDF Installation Manager(EIM) to install ESP-IDF versions</p></div></div><script>function updateCards(data){var c=document.getElementById('cardsContainer');var e=document.getElementById('emptyState');if(!data||data.length===0){if(c)c.style.display='none';if(e)e.style.display='block';return;}if(c){c.style.display='grid';c.innerHTML='';data.forEach(function(idf,i){var card=document.createElement('div');card.className='card';card.innerHTML='<h3>'+idf.name+'</h3><p>Version: '+idf.version+'</p><p>Path: '+idf.path+'</p><button onclick=\"handleActivate(\\''+idf.id+'\\')\" '+(idf.active||idf.activating?'disabled':'')+'>'+(idf.active?'Active':idf.activating?'Activating...':'Activate')+'</button>';c.appendChild(card);});}if(e)e.style.display='none';}function handleActivate(id){try{if(typeof javaActivate==='function')javaActivate(id);}catch(e){console.error(e);}}function handleReload(id){try{if(typeof javaReload==='function')javaReload(id);}catch(e){console.error(e);}}</script></body></html>"; //$NON-NLS-1$
	}

	private void setupJavaScriptBridge()
	{
		// Activate function
		new BrowserFunction(browser, "javaActivate") //$NON-NLS-1$
		{
			@Override
			public Object function(Object[] arguments)
			{
				if (arguments != null && arguments.length > 0 && arguments[0] instanceof String)
				{
					String idfId = (String) arguments[0];
					handleActivate(idfId);
				}
				return null;
			}
		};

		// Reload function
		new BrowserFunction(browser, "javaReload") //$NON-NLS-1$
		{
			@Override
			public Object function(Object[] arguments)
			{
				if (arguments != null && arguments.length > 0 && arguments[0] instanceof String)
				{
					String idfId = (String) arguments[0];
					handleReload(idfId);
				}
				return null;
			}
		};
	}

	private void handleActivate(String idfId)
	{
		if (idfInstalledList == null)
			return;

		IdfInstalled targetIdf = idfInstalledList.stream().filter(idf -> idf.getId().equals(idfId)).findFirst()
				.orElse(null);

		if (targetIdf != null && !ToolsUtility.isIdfInstalledActive(targetIdf))
		{
			activatingStates.put(idfId, true);
			updateWebView(); // Update UI to show loading state

			SetupToolsInIde setupToolsInIde = new SetupToolsInIde(targetIdf, eimJson, getConsoleStream(true),
					getConsoleStream(false));
			SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(ESPIDFMainTablePage.this,
					setupToolsInIde);
			setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
			setupToolsInIde.schedule();
		}
	}

	private void handleReload(String idfId)
	{
		if (idfInstalledList == null)
			return;

		IdfInstalled targetIdf = idfInstalledList.stream().filter(idf -> idf.getId().equals(idfId)).findFirst()
				.orElse(null);

		if (targetIdf != null && ToolsUtility.isIdfInstalledActive(targetIdf))
		{
			activatingStates.put(idfId, true);
			updateWebView(); // Update UI to show loading state

			SetupToolsInIde setupToolsInIde = new SetupToolsInIde(targetIdf, eimJson, getConsoleStream(true),
					getConsoleStream(false));
			SetupToolsJobListener toolsActivationJobListener = new SetupToolsJobListener(ESPIDFMainTablePage.this,
					setupToolsInIde);
			setupToolsInIde.addJobChangeListener(toolsActivationJobListener);
			setupToolsInIde.schedule();
		}
	}

	private void updateWebView()
	{
		if (browser == null || browser.isDisposed())
		{
			Logger.log("Browser is null or disposed, cannot update web view"); //$NON-NLS-1$
			return;
		}

		if (idfInstalledList == null)
		{
			Logger.log("idfInstalledList is null, showing empty state"); //$NON-NLS-1$
			// Show empty state
			String script = "if (typeof updateCards === 'function') { updateCards([]); }"; //$NON-NLS-1$
			browser.execute(script);
			return;
		}

		try
		{
			// Convert IdfInstalled list to JSON
			String jsonData = convertToJson(idfInstalledList);
			Logger.log("Updating web view with " + idfInstalledList.size() + " IDF installations"); //$NON-NLS-1$ //$NON-NLS-2$
			Logger.log("JSON data: " + jsonData); //$NON-NLS-1$
			
			// Call JavaScript function to update cards
			String script = "try { " + //$NON-NLS-1$
					"console.log('Executing updateCards with data'); " + //$NON-NLS-1$
					"if (typeof updateCards === 'function') { " + //$NON-NLS-1$
					"updateCards(" + jsonData + "); " + //$NON-NLS-1$
					"} else { " + //$NON-NLS-1$
					"console.error('updateCards function not found'); " + //$NON-NLS-1$
					"} " + //$NON-NLS-1$
					"} catch(e) { console.error('Error in updateCards:', e); }"; //$NON-NLS-1$
			boolean executed = browser.execute(script);
			if (!executed)
			{
				Logger.log("Failed to execute JavaScript - browser may not be ready"); //$NON-NLS-1$
				// Retry after a short delay
				Display.getDefault().timerExec(500, () -> {
					if (!browser.isDisposed())
					{
						updateWebView();
					}
				});
			}
		}
		catch (Exception e)
		{
			Logger.log("Error updating web view: " + e.getMessage()); //$NON-NLS-1$
			Logger.log(e);
		}
	}

	private String convertToJson(List<IdfInstalled> idfList)
	{
		if (idfList == null || idfList.isEmpty())
		{
			return "[]"; //$NON-NLS-1$
		}
		
		StringBuilder json = new StringBuilder("[");
		for (int i = 0; i < idfList.size(); i++)
		{
			IdfInstalled idf = idfList.get(i);
			if (i > 0)
				json.append(",");

			boolean isActive = ToolsUtility.isIdfInstalledActive(idf);
			boolean isActivating = activatingStates.getOrDefault(idf.getId(), false);
			String version = ToolsUtility.getIdfVersion(idf, eimJson != null ? eimJson.getGitPath() : null);

			json.append("{");
			json.append("\"id\":\"").append(escapeJson(idf.getId())).append("\",");
			json.append("\"name\":\"").append(escapeJson(idf.getName() != null ? idf.getName() : "Unknown")).append("\","); //$NON-NLS-1$ //$NON-NLS-2$
			json.append("\"version\":\"").append(escapeJson(version != null ? version : "Unknown")).append("\","); //$NON-NLS-1$ //$NON-NLS-2$
			json.append("\"path\":\"").append(escapeJson(idf.getPath() != null ? idf.getPath() : "")).append("\","); //$NON-NLS-1$ //$NON-NLS-2$
			json.append("\"active\":").append(isActive).append(",");
			json.append("\"activating\":").append(isActivating);
			json.append("}");
		}
		json.append("]");
		return json.toString();
	}

	private String escapeJson(String str)
	{
		if (str == null)
			return "";
		return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
				.replace("\t", "\\t");
	}

	public void resetAllActivatingStates()
	{
		activatingStates.clear();
		updateWebView();
	}

	private MessageConsoleStream getConsoleStream(boolean errorStream)
	{
		IDFConsole idfConsole = new IDFConsole();
		return idfConsole.getConsoleStream(Messages.IDFToolsHandler_ToolsManagerConsole, null, errorStream, true);
	}
}

