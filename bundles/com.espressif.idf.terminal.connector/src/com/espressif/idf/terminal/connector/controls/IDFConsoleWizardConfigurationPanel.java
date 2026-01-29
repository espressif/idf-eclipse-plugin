/*******************************************************************************
 * Copyright (c) 2012, 2018 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Kondal Kolipaka <kkolipaka@espressif.com> - ESP-IDF Console implementation
 *******************************************************************************/
package com.espressif.idf.terminal.connector.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.terminal.view.core.ITerminalsConnectorConstants;
import org.eclipse.terminal.view.ui.launcher.AbstractExtendedConfigurationPanel;
import org.eclipse.terminal.view.ui.launcher.IConfigurationPanelContainer;
import org.eclipse.ui.WorkbenchEncoding;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.espressif.idf.core.IDFProjectNature;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.terminal.connector.controls.themes.EspressifDarkTheme;
import com.espressif.idf.terminal.connector.controls.themes.EspressifLightTheme;
import com.espressif.idf.terminal.connector.controls.themes.ITerminalTheme;
import com.espressif.idf.terminal.connector.controls.themes.PowerShellTheme;
import com.espressif.idf.terminal.connector.controls.themes.ResetTheme;
import com.espressif.idf.ui.EclipseUtil;

/**
 * IDF console wizard configuration panel implementation.
 */
public class IDFConsoleWizardConfigurationPanel extends AbstractExtendedConfigurationPanel {

	private static final String PREF_THEME_SELECTION = "IDF_CONSOLE_THEME_SELECTION"; //$NON-NLS-1$
	private static final String TERMINAL_PREF_NODE = "org.eclipse.terminal.control"; //$NON-NLS-1$

	private Combo projectCombo;
	private final List<ITerminalTheme> themes = new ArrayList<>();
	private final List<Button> themeButtons = new ArrayList<>();

	/**
	 * Constructor.
	 * @param container The configuration panel container or <code>null</code>.
	 */
	public IDFConsoleWizardConfigurationPanel(IConfigurationPanelContainer container) {
		super(container);

		themes.add(new ResetTheme());
		themes.add(new EspressifLightTheme());
		themes.add(new EspressifDarkTheme());
		themes.add(new PowerShellTheme());
	}

	@Override
	public void setupPanel(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createProjectCombo(panel);
		createEncodingUI(panel, false);
		createThemeUI(panel);

		// Set the default encoding based on OS
		if (Platform.OS_MACOSX.equals(Platform.getOS()) || Platform.OS_WIN32.equals(Platform.getOS())) {
			setEncoding("UTF-8"); //$NON-NLS-1$
		} else {
			String encoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
			if (encoding != null && !"".equals(encoding)) //$NON-NLS-1$
				setEncoding(encoding);
		}

		// Fill the rest of the panel with a spacer
		Label label = new Label(panel, SWT.HORIZONTAL);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 300;
		layoutData.heightHint = 80;
		label.setLayoutData(layoutData);

		setControl(panel);
	}

	/**
	 * Dynamically creates radio buttons for each loaded ITerminalTheme strategy.
	 */
	private void createThemeUI(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.IDFConsoleWizardConfigurationPanel_TerminalColorPresetsLbl);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		themeButtons.clear();

		for (ITerminalTheme theme : themes) {
			Button btn = new Button(group, SWT.RADIO);
			btn.setText(theme.getLabel());
			btn.setData(theme);
			themeButtons.add(btn);
		}

		Label noteLabel = new Label(group, SWT.WRAP);
		noteLabel.setText(Messages.IDFConsoleWizardConfigurationPanel_TerminalColorPresetsNote);
		noteLabel.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT));
		GridData noteData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		noteData.verticalIndent = 5;
		noteLabel.setLayoutData(noteData);
	}

	@Override
	public void extractData(Map<String, Object> data) {
		data.put(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
				"com.espressif.idf.terminal.connector.espidfConnector"); //$NON-NLS-1$

		data.put(ITerminalsConnectorConstants.PROP_ENCODING, getEncoding());

		if (projectCombo != null && !projectCombo.isDisposed() && !projectCombo.getText().isEmpty()) {
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(projectCombo.getText());
			if (p != null && p.exists() && p.getLocation() != null) {
				data.put(ITerminalsConnectorConstants.PROP_PROCESS_WORKING_DIR, p.getLocation().toOSString());
				data.put(ITerminalsConnectorConstants.PROP_TITLE, p.getName());
			}
		}

		for (Button btn : themeButtons) {
			if (btn != null && !btn.isDisposed() && btn.getSelection()) {
				ITerminalTheme strategy = (ITerminalTheme) btn.getData();
				if (strategy != null) {
					applyThemeStrategy(strategy);
				}
				break;
			}
		}
	}

	/**
	 * Instantiates the Preference Store and delegates the coloring logic 
	 * to the selected strategy.
	 */
	private void applyThemeStrategy(ITerminalTheme theme) {
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, TERMINAL_PREF_NODE);

		theme.apply(store);

		if (store instanceof ScopedPreferenceStore preferenceStore) {
			try {
				preferenceStore.save();
			} catch (Exception ex) {
				Logger.log(ex);
			}
		}
	}

	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		// Save encoding settings
		doSaveEncodingsWidgetValues(settings, idPrefix);

		// Save selected theme ID
		if (settings != null) {
			for (Button btn : themeButtons) {
				if (btn.getSelection()) {
					ITerminalTheme theme = (ITerminalTheme) btn.getData();
					settings.put(PREF_THEME_SELECTION, theme.getId());
					break;
				}
			}
		}
	}

	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		doRestoreEncodingsWidgetValues(settings, idPrefix);

		// Restore theme selection
		if (settings != null) {
			String savedId = settings.get(PREF_THEME_SELECTION);

			boolean found = false;
			// Iterate over buttons to find the one matching the saved ID
			for (Button btn : themeButtons) {
				ITerminalTheme theme = (ITerminalTheme) btn.getData();
				if (theme.getId().equals(savedId)) {
					btn.setSelection(true);
					found = true;
				} else {
					btn.setSelection(false);
				}
			}

			// Fallback: If no setting saved (or ID not found), select the first one (Restore Defaults)
			if (!found && !themeButtons.isEmpty()) {
				themeButtons.get(0).setSelection(true);
			}
		} else {
			// No settings at all? Default to the first option
			if (!themeButtons.isEmpty()) {
				themeButtons.get(0).setSelection(true);
			}
		}
	}

	private void createProjectCombo(Composite parent) {
		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label projectLabel = new Label(panel, SWT.NONE);
		projectLabel
				.setText(Messages.IDFConsoleWizardConfigurationPanel_IDFConsoleWizardConfigurationPanel_ProjectLabel);

		projectCombo = new Combo(panel, SWT.READ_ONLY);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				if (project.hasNature(IDFProjectNature.ID)) {
					projectCombo.add(project.getName());
				}
			} catch (CoreException e) {
				Logger.log(e);
			}
		}

		Optional<IProject> optProject = Optional.ofNullable(EclipseUtil.getSelectedIDFProjectInExplorer());
		optProject.ifPresentOrElse(project -> projectCombo.setText(project.getName()), () -> {
			if (projectCombo.getItemCount() > 0)
				projectCombo.select(0);
		});
	}

	@Override
	public void setupData(Map<String, Object> data) {
		if (data == null)
			return;
		String value = (String) data.get(ITerminalsConnectorConstants.PROP_ENCODING);
		if (value != null)
			setEncoding(value);
	}

	@Override
	protected void fillSettingsForHost(String host) {
	}

	@Override
	protected void saveSettingsForHost(boolean add) {
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	protected String getHostFromSettings() {
		return null;
	}

	@Override
	public boolean isWithHostList() {
		return false;
	}
}