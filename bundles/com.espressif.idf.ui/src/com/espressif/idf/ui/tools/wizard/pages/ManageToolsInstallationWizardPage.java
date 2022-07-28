/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.osgi.service.prefs.Preferences;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.LogMessagesThread;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.ToolsInstallationHandler;
import com.espressif.idf.ui.tools.ToolsJsonParser;
import com.espressif.idf.ui.tools.ToolsUtility;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionDetailsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;
import com.espressif.idf.ui.tools.wizard.IToolsInstallationWizardConstants;
import com.espressif.idf.ui.tools.wizard.ToolsManagerWizardDialog;

/**
 * Shell for displaying tools information on UI
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationWizardPage extends WizardPage implements IToolsWizardPage
{
	private static final String PNG_EXTENSION = ".png"; //$NON-NLS-1$
	private static final String YELLOW = "icons/tools/yellow.png"; //$NON-NLS-1$
	private static final String AVAILABLE = "available"; //$NON-NLS-1$
	private static final String RECOMMENDED = "recommended"; //$NON-NLS-1$
	private static final String ALWAYS = "always"; //$NON-NLS-1$
	private static final String ALL = "all"; //$NON-NLS-1$
	private static final String WHITE = "icons/tools/white.png"; //$NON-NLS-1$
	private static final String MAC_OS = "mac"; //$NON-NLS-1$
	private static final String IMG_MAC_OS = "icons/tools/".concat(MAC_OS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String LINUX_OS = "linux"; //$NON-NLS-1$
	private static final String IMG_LINUX_OS = "icons/tools/".concat(LINUX_OS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String WIN_OS = "win"; //$NON-NLS-1$
	private static final String GREEN = "icons/tools/green.png"; //$NON-NLS-1$
	private static final String WINDOWS = "windows"; //$NON-NLS-1$
	private static final String IMG_WINDOWS_OS = "icons/tools/".concat(WINDOWS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String SELECT_ALL = "icons/tools/select-all".concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String UNSELECT_ALL = "icons/tools/unselect-all".concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String SELECT_RECOMMENDED = "icons/tools/select-recommended".concat(PNG_EXTENSION); //$NON-NLS-1$

	private List<ToolsVO> toolsVOs;
	private Text descriptionText;
	private Tree toolsTree;
	private Button btnDeleteTools;
	private Button btnSelectButton;
	private boolean selectionFlag;
	private Combo filterTargetBox;
	private boolean itemChecked = false;
	private Button chkAvailableVersions;
	private Button selectRecommendedButton;
	private Composite parentComposite;
	private ToolsJsonParser toolsJsonParser;
	private Composite pageComposite;
	private WizardDialog parentWizardDialog;
	private Text logText;
	private Queue<String> logQueue;
	private LogMessagesThread logMessagesThread;
	private Button btnCancel;
	private Button btnFinish;
	private ProgressBar progressBar;
	private ToolsInstallationHandler toolsInstallationHandler;
	private IDFEnvironmentVariables idfEnvironmentVariables;
	private Preferences scopedPreferenceStore;
	private Button forceDownloadBtn;
	private Listener[] listenersForFinish;

	public ManageToolsInstallationWizardPage(WizardDialog parentWizardDialog)
	{
		super(Messages.ManageToolsInstallation);
		setTitle(Messages.ManageToolsInstallation);
		setDescription(Messages.ManageToolsInstallationDescription);
		toolsJsonParser = new ToolsJsonParser();
		this.parentWizardDialog = parentWizardDialog;
		this.logQueue = new ConcurrentLinkedQueue<String>();
		idfEnvironmentVariables = new IDFEnvironmentVariables();
		scopedPreferenceStore = InstanceScope.INSTANCE.getNode(UIPlugin.PLUGIN_ID);
	}

	@Override
	public void createControl(Composite parent)
	{
		this.parentComposite = parent;
		initializeJson();

		Composite treeControlsComposite = new Composite(parent, SWT.NONE);
		treeControlsComposite.setLayout(new GridLayout(1, false));

		Composite subControlComposite = new Composite(treeControlsComposite, SWT.NONE);
		subControlComposite.setLayout(new GridLayout(1, false));
		subControlComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		setPageComplete(false);

		Label filterTextLabel = new Label(subControlComposite, SWT.NONE);
		filterTextLabel.setText(Messages.FilterLabel);
		Text filterText = new Text(subControlComposite, SWT.SINGLE | SWT.BORDER);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		filterText.addKeyListener(new KeyAdapter()
		{
			private int filterLength = 0;

			@Override
			public void keyReleased(KeyEvent e)
			{
				if (filterText.getText().length() > filterLength)
				{
					String filter = filterText.getText();
					filterLength = filterText.getText().length();
					filterItems(filter, toolsTree);
				}
				else if (filterText.getText().length() < filterLength)
				{
					toolsTree.removeAll();
					addItemsToTree(toolsTree, false);
					String filter = filterText.getText();
					filterLength = filterText.getText().length();
					filterItems(filter, toolsTree);
				}

				if (StringUtil.isEmpty(filterText.getText()))
				{
					toolsTree.removeAll();
					addItemsToTree(toolsTree, false);
				}
			}

			private void filterItems(String filter, Tree toolsTree)
			{
				for (TreeItem mainItem : toolsTree.getItems())
				{
					if (!getText(mainItem).toLowerCase().contains(filter.toLowerCase()))
					{
						boolean foundInSubItem = false;
						for (TreeItem subItem : mainItem.getItems())
						{
							if (!getText(subItem).toLowerCase().contains(filter.toLowerCase()))
							{
								subItem.dispose();
							}
							else
							{
								foundInSubItem = true;
							}
						}

						if (!foundInSubItem)
						{
							mainItem.dispose();
						}
					}
				}
			}

			private String getText(TreeItem treeItem)
			{
				return treeItem.getText(0).concat(treeItem.getText(1)).concat(treeItem.getText(2))
						.concat(treeItem.getText(3));
			}
		});

		Composite treeContainingComposite = new Composite(subControlComposite, SWT.NONE);
		treeContainingComposite.setLayout(new GridLayout(2, false));
		treeContainingComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		toolsTree = new Tree(treeContainingComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		toolsTree.setHeaderVisible(true);
		toolsTree.setLinesVisible(true);
		GridData treeGridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		treeGridData.heightHint = 150;
		toolsTree.setLayoutData(treeGridData);
		toolsTree.addListener(SWT.Selection, new TreeSelectionListener());

		TreeColumn trclmnName = new TreeColumn(toolsTree, SWT.NONE);
		trclmnName.setWidth(200);
		trclmnName.setText(Messages.ToolsTreeNameCol);

		TreeColumn trclmnSize = new TreeColumn(toolsTree, SWT.NONE);
		trclmnSize.setWidth(70);
		trclmnSize.setText(Messages.ToolsTreeSizeCol);

		TreeColumn trclmnStatus = new TreeColumn(toolsTree, SWT.NONE);
		trclmnStatus.setWidth(150);
		trclmnStatus.setText(Messages.ToolsTreeStatusCol);

		TreeColumn trclmnDescription = new TreeColumn(toolsTree, SWT.NONE);
		trclmnDescription.setWidth(500);
		trclmnDescription.setText(Messages.DescriptionText);

		addItemsToTree(toolsTree, false);

		Composite logAreaComposite = new Composite(treeControlsComposite, SWT.NONE);
		logAreaComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
		logAreaComposite.setLayout(new GridLayout(1, false));

		descriptionText = new Text(treeContainingComposite,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_text.widthHint = 150;
		descriptionText.setLayoutData(gd_text);

		Label logLabel = new Label(logAreaComposite, SWT.NONE);
		logLabel.setText(Messages.InstallPreRquisitePage_lblLog_text);
		btnCancel = new Button(logAreaComposite, SWT.PUSH);
		btnCancel.setText(Messages.BtnCancel);
		btnCancel.setVisible(false);
		btnCancel.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				toolsInstallationHandler.setCancelled(true);
				logQueue.clear();
			}
		});

		logText = new Text(logAreaComposite,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_text_log = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		gd_text_log.heightHint = 200;
		logText.setLayoutData(gd_text_log);

		progressBar = new ProgressBar(logAreaComposite, SWT.HORIZONTAL);
		progressBar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		progressBar.setVisible(false);

		createButtonsBar(logAreaComposite);

		setButtonsEnabled(itemChecked);

		parent.layout();
		parent.computeSize(800, 600, true);
		parent.getShell().layout(true);
		parent.getShell().computeSize(800, 600, true);

		parentWizardDialog.getShell().computeSize(800, 600, true);
		setControl(treeControlsComposite);
		logMessagesThread = new LogMessagesThread(logQueue, logText, getShell().getDisplay());
		if (!logMessagesThread.isAlive())
		{
			logMessagesThread.start();
		}

		btnFinish.setEnabled(true);
		setPageStatus();
	}

	public void refreshTree() throws Exception
	{
		toolsTree.removeAll();
		addItemsToTree(toolsTree, chkAvailableVersions.getSelection());
	}

	public void setPageStatus()
	{
		boolean pageCompletion = true;
		for (TreeItem mainItem : toolsTree.getItems())
		{
			ToolsVO toolsVO = (ToolsVO) mainItem.getData();
			boolean alwaysInstall = toolsVO.getInstallType().equalsIgnoreCase(ALWAYS)
					|| toolsVO.getInstallType().equalsIgnoreCase(RECOMMENDED);
			if (alwaysInstall)
				pageCompletion &= toolsVO.isInstalled();
		}
		
		
		scopedPreferenceStore.putBoolean(IToolsInstallationWizardConstants.INSTALL_TOOLS_FLAG, pageCompletion);
		setPageComplete(pageCompletion);
	}
	
	private void initializeJson()
	{
		try
		{
			toolsJsonParser.loadJson();
			this.toolsVOs = toolsJsonParser.getToolsList();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private void createButtonsBar(Composite subControlComposite)
	{
		Composite topBarComposite = new Composite(subControlComposite, SWT.BORDER);
		topBarComposite.setLayout(new GridLayout(5, false));
		GridData topBarLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		topBarLayoutData.heightHint = 100;
		topBarComposite.setLayoutData(topBarLayoutData);

		btnSelectButton = new Button(topBarComposite, SWT.PUSH);
		btnSelectButton.setImage(UIPlugin.getImage(SELECT_ALL));
		btnSelectButton.redraw();

		btnSelectButton.setToolTipText(Messages.SelectAllButtonToolTip);
		btnSelectButton.setText(Messages.SelectAllButton);
		btnSelectButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectAllItems(!selectionFlag);
				setButtonsEnabled(!selectionFlag);
				btnSelectButton
						.setImage(selectionFlag ? UIPlugin.getImage(SELECT_ALL) : UIPlugin.getImage(UNSELECT_ALL));
				btnSelectButton.setToolTipText(
						selectionFlag ? Messages.SelectAllButtonToolTip : Messages.DeselectAllButtonToolTip);
				btnSelectButton.setText(selectionFlag ? Messages.SelectAllButton : Messages.DeselectAllButton);
				btnSelectButton.redraw();
				selectionFlag = !selectionFlag;
				btnSelectButton.redraw();
			}
		});

		selectRecommendedButton = new Button(topBarComposite, SWT.PUSH);
		selectRecommendedButton.setImage(UIPlugin.getImage(SELECT_RECOMMENDED));
		selectRecommendedButton.setToolTipText(Messages.SelectRecommendedToolTip);
		selectRecommendedButton.setText(Messages.SelectRecommended);
		selectRecommendedButton.addSelectionListener(new SelectRecommendedButtonSelectionAdapter());

		String[] filterItems = getTargetFilterItems();
		boolean filterVisibilityForTargets = filterItems != null && filterItems.length != 0;
		Label targetFilterLabel = new Label(topBarComposite, SWT.NONE);
		targetFilterLabel.setText(Messages.FilterTargets);
		targetFilterLabel.setVisible(filterVisibilityForTargets);

		filterTargetBox = new Combo(topBarComposite, SWT.READ_ONLY);
		filterTargetBox.setItems(filterItems);
		filterTargetBox.setToolTipText(Messages.FilterTargetBoxToolTip);
		filterTargetBox.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String selectedTarget = filterTargetBox.getItem(filterTargetBox.getSelectionIndex());
				toolsTree.removeAll();
				addItemsToTree(toolsTree, chkAvailableVersions.getSelection());
				if (selectedTarget.equalsIgnoreCase(ALL))
				{
					return;
				}
				for (TreeItem item : toolsTree.getItems())
				{
					ToolsVO toolsVO = (ToolsVO) item.getData();
					if (toolsVO.getSupportedTargets().contains(selectedTarget)
							|| toolsVO.getSupportedTargets().contains(ALL))
					{
						continue;
					}

					item.dispose();
				}
			}
		});
		filterTargetBox.setVisible(filterVisibilityForTargets);

		chkAvailableVersions = new Button(topBarComposite, SWT.CHECK);
		chkAvailableVersions.setText(Messages.ShowAvailableVersionsOnly);
		chkAvailableVersions.setToolTipText(Messages.ShowAvailableVersionsOnlyToolTip);
		chkAvailableVersions.addSelectionListener(new SelectionAdapter()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				toolsTree.removeAll();
				addItemsToTree(toolsTree, chkAvailableVersions.getSelection());
			}
		});
		chkAvailableVersions.setVisible(false); // This will be enhanced in the feature update

		btnFinish = ((ToolsManagerWizardDialog) parentWizardDialog).getButton(IDialogConstants.FINISH_ID);
		btnFinish.setText(Messages.InstallToolsText);
		btnFinish.setEnabled(true);
		listenersForFinish = btnFinish.getListeners(SWT.Selection);
		for (Listener listener : listenersForFinish)
		{
			btnFinish.removeListener(SWT.Selection, listener);
		}
		btnFinish.addSelectionListener(new InstallButtonSelectionAdapter());

		btnDeleteTools = new Button(topBarComposite, SWT.NONE);
		btnDeleteTools.setText(Messages.DeleteToolsText);
		btnDeleteTools.setToolTipText(Messages.DeleteToolsTextToolTip);
		btnDeleteTools.addSelectionListener(new DeleteButtonSelectionAdapter());
		btnDeleteTools.setEnabled(false);
		btnDeleteTools.setVisible(false); // This will be enhanced in the feature update

		forceDownloadBtn = new Button(topBarComposite, SWT.CHECK);
		forceDownloadBtn.setText(Messages.ForceDownload);
		forceDownloadBtn.setToolTipText(Messages.ForceDownloadToolTip);
		forceDownloadBtn.setVisible(false);
		new Label(topBarComposite, SWT.NONE);

	}

	private String[] getTargetFilterItems()
	{
		Set<String> targets = new HashSet<>();
		for (ToolsVO toolsVO : toolsVOs)
		{
			if (toolsVO.getSupportedTargets() != null && toolsVO.getSupportedTargets().size() > 0)
			{
				targets.addAll(toolsVO.getSupportedTargets());
			}
		}
		return targets.toArray(String[]::new);
	}

	private void addItemsToTree(Tree toolsTree, boolean availableOnly)
	{
		toolsTree.setRedraw(false);
		final String os = Platform.getOS();
		final String architecture = System.getProperty("os.arch").toLowerCase(); //$NON-NLS-1$
		
		for (ToolsVO toolsVO : toolsVOs)
		{
			TreeItem mainItem = new TreeItem(toolsTree, SWT.NONE);
			boolean isInstalled = false;

			boolean alwaysInstall = toolsVO.getInstallType().equalsIgnoreCase(ALWAYS)
					|| toolsVO.getInstallType().equalsIgnoreCase(RECOMMENDED);
			if (alwaysInstall)
			{
				itemChecked = true;
			}

			mainItem.setChecked(alwaysInstall);
			boolean platformAvailable = false;

			if (availableOnly)
			{
				addAvailableToolVersions(toolsVO, mainItem);
				platformAvailable = true;
			}
			else
			{
				for (VersionsVO versionsVO : toolsVO.getVersionVO())
				{
					for (String key : versionsVO.getVersionOsMap().keySet())
					{
						if (os.equals(Platform.OS_WIN32))
						{
							if (!key.toLowerCase().contains(WIN_OS))
							{
								continue;
							}
							
							if (architecture.contains("amd64") && !key.contains(WIN_OS.concat("64"))) //$NON-NLS-1$ //$NON-NLS-2$
							{
								continue;
							}
						}
						else if (Platform.getOS().equals(Platform.OS_LINUX))
						{
							String check = LINUX_OS.concat("-").concat(architecture); //$NON-NLS-1$
							if (!key.toLowerCase().contains(check))
							{
								continue;
							}
						}
						else if (Platform.getOS().equals(Platform.OS_MACOSX))
						{
							if (!key.toLowerCase().contains(MAC_OS) && architecture.contains("x86_64")) //$NON-NLS-1$
							{
								continue;
							}
							
							String check = MAC_OS.concat("-").concat(architecture); //$NON-NLS-1$
							if (!key.toLowerCase().contains(check))
							{
								continue;
							}
						}
						
						alwaysInstall |= versionsVO.getStatus().equalsIgnoreCase(RECOMMENDED);
						isInstalled = ToolsUtility.isToolInstalled(toolsVO.getName(), versionsVO.getName());
						toolsVO.setInstalled(isInstalled);
						versionsVO.getVersionOsMap().get(key).setSelected(alwaysInstall);

						TreeItem subItem = new TreeItem(mainItem, SWT.NONE);
						String[] subItemText = getSubItemText(key, versionsVO.getVersionOsMap(), versionsVO.getName(),
								versionsVO.getStatus(), isInstalled ? 1 : 2);
						subItem.setText(subItemText);
						subItem.setData(versionsVO);
						Image image = getOsImageForItem(subItem);
						subItem.setImage(0, image);
						subItem.setImage(2, isInstalled ? UIPlugin.getImage(GREEN) : UIPlugin.getImage(WHITE));
						subItem.setChecked(alwaysInstall);
						platformAvailable = true;
					}
				}
			}
			
			// check if all subitems were checked check the main item
			boolean check = true;
			for (TreeItem subItem : mainItem.getItems())
			{
				check &= subItem.getChecked();
			}
			
			mainItem.setChecked(check);
			String[] itemText = getMainItemText(toolsVO, isInstalled, mainItem.getItems());
			mainItem.setText(itemText);
			mainItem.setData(toolsVO);
			Image installedImage = isInstalled ? UIPlugin.getImage(GREEN) : UIPlugin.getImage(WHITE);
			mainItem.setImage(2, installedImage);
			mainItem.setExpanded(alwaysInstall);

			if (!platformAvailable)
			{
				mainItem.dispose();
			}
		}

		toolsTree.setRedraw(true);
	}

	private void addAvailableToolVersions(ToolsVO toolsVO, TreeItem mainItem)
	{
		Map<String, String> availableVersions = ToolsUtility.getAvailableToolVersions(toolsVO);
		for (String availableVersion : availableVersions.keySet())
		{
			List<TreeItem> subItems = Arrays.asList(mainItem.getItems());
			subItems = subItems.stream().filter(sb -> ((VersionsVO) sb.getData()).getName().equals(availableVersion))
					.collect(Collectors.toList());
			if (subItems.size() > 0)
			{
				continue;
			}
			boolean isInstalled = ToolsUtility.isToolInstalled(toolsVO.getName(), availableVersion);
			Image installedImage = isInstalled ? UIPlugin.getImage(GREEN) : UIPlugin.getImage(YELLOW);
			TreeItem subItem = new TreeItem(mainItem, SWT.NONE);
			VersionsVO versionsVO = new VersionsVO();
			versionsVO.setName(availableVersion);
			versionsVO.setStatus(AVAILABLE);
			String[] subItemText = getSubItemText(Platform.getOS(), null, availableVersion, versionsVO.getStatus(),
					isInstalled ? 1 : 3);
			VersionDetailsVO versionDetailsVO = new VersionDetailsVO();
			Map<String, VersionDetailsVO> versionMap = new HashMap<>();
			versionMap.put(Platform.getOS(), versionDetailsVO);
			versionsVO.setVersionOsMap(versionMap);
			versionsVO.setAvailable(true);
			versionsVO.setAvailablePath(availableVersions.get(availableVersion));
			subItem.setText(subItemText);
			subItem.setData(versionsVO);
			subItem.setImage(2, installedImage);
		}
	}

	private Image getOsImageForItem(TreeItem item)
	{
		if (item.getText(0).toLowerCase().contains(LINUX_OS))
		{
			return UIPlugin.getImage(IMG_LINUX_OS);
		}
		else if (item.getText(0).toLowerCase().contains(WIN_OS))
		{
			return UIPlugin.getImage(IMG_WINDOWS_OS);
		}
		else if (item.getText(0).toLowerCase().contains(MAC_OS))
		{
			return UIPlugin.getImage(IMG_MAC_OS);
		}

		return null;
	}

	private String[] getSubItemText(String key, Map<String, VersionDetailsVO> versionOsMap, String name, String status,
			int isInstalled)
	{
		String[] textArr = new String[4];
		textArr[0] = name;
		if (versionOsMap != null)
		{
			textArr[1] = versionOsMap.get(key).getReadableSize();
		}
		else
		{
			textArr[1] = StringUtil.EMPTY;
		}
		switch (isInstalled)
		{
		case 1:
			textArr[2] = Messages.Installed;
			break;
		case 2:
			textArr[2] = Messages.NotInstalled;
			break;
		case 3:
			textArr[2] = Messages.Available;
		default:
			textArr[2] = StringUtil.EMPTY;
			break;
		}

		textArr[3] = key.concat(" (").concat(status).concat(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return textArr;
	}

	private String[] getMainItemText(ToolsVO toolsVO, boolean isInstalled, TreeItem[] subItems)
	{
		String[] textArr = new String[4];
		textArr[0] = toolsVO.getName();
		int total = 0;
		for (TreeItem subItem : subItems)
		{
			if (!StringUtil.isEmpty(subItem.getText(1)))
			{
				total += Integer.parseInt(subItem.getText(1).split(" ")[0]); //$NON-NLS-1$
			}
		}
		textArr[1] = String.valueOf(total).concat(" MB"); //$NON-NLS-1$
		textArr[2] = isInstalled ? Messages.Installed : Messages.NotInstalled;
		textArr[3] = toolsVO.getDescription();
		return textArr;
	}

	private Map<ToolsVO, List<VersionsVO>> getSelectedTools()
	{
		Map<ToolsVO, List<VersionsVO>> selectedItems = new HashMap<>();
		for (TreeItem item : toolsTree.getItems())
		{
			for (TreeItem subItem : item.getItems())
			{
				if (subItem.getChecked())
				{
					if (selectedItems.get(item.getData()) != null && selectedItems.get(item.getData()).size() > 0)
					{
						selectedItems.get(item.getData()).add((VersionsVO) subItem.getData());
					}
					else
					{
						selectedItems.put((ToolsVO) item.getData(), new ArrayList<>());
						selectedItems.get(item.getData()).add((VersionsVO) subItem.getData());
					}
				}
			}
		}

		return selectedItems;
	}

	public void setButtonsEnabled(boolean enabled)
	{
		if (btnFinish.getText().equals(Messages.InstallToolsText))
		{
			btnFinish.setEnabled(enabled);
			btnFinish.redraw();
		}
		btnDeleteTools.setEnabled(enabled);
		btnDeleteTools.redraw();
	}

	private void selectAllItems(boolean selectAll)
	{
		for (TreeItem item : toolsTree.getItems())
		{
			item.setChecked(selectAll);
			Event swtCheckedTreeEvent = new Event();
			swtCheckedTreeEvent.detail = SWT.CHECK;
			swtCheckedTreeEvent.item = item;
			toolsTree.notifyListeners(SWT.Selection, swtCheckedTreeEvent);
		}
	}

	@Override
	public Composite getPageComposite()
	{
		return pageComposite;
	}

	public void disableControls(boolean disable)
	{
		toolsTree.setEnabled(disable);
		btnDeleteTools.setEnabled(disable);
		btnSelectButton.setEnabled(disable);
		filterTargetBox.setEnabled(disable);
		chkAvailableVersions.setEnabled(disable);
		selectRecommendedButton.setEnabled(disable);
	}

	public void visibleCancelBtn(boolean visible)
	{
		this.btnCancel.setVisible(visible);
	}

	public void setPageComposite(Composite pageComposite)
	{
		this.pageComposite = pageComposite;
	}

	public ProgressBar getProgressBar()
	{
		return progressBar;
	}

	@Override
	public void cancel()
	{
		if (toolsInstallationHandler != null)
		{
			toolsInstallationHandler.setCancelled(true);
		}

		logQueue.clear();
	}

	/**
	 * Restore the finish button to the original wizard style. Also call this method from the back pressed in Wizard
	 * Dialog
	 */
	public void restoreFinishButton()
	{
		btnFinish.setText(IDialogConstants.FINISH_LABEL);

		for (Listener listener : listenersForFinish)
		{
			btnFinish.addListener(SWT.Selection, listener);
		}
	}

	private class TreeSelectionListener implements Listener
	{
		@Override
		public void handleEvent(Event event)
		{
			switch (event.detail)
			{
			case SWT.CHECK:
				swtCheckEventHandle(event);
				break;
			case 0:
				swtSelectedEventHandle(event);
				break;
			default:
				break;
			}

			buttonEnableCheck();
		}

		private void buttonEnableCheck()
		{
			for (TreeItem item : toolsTree.getItems())
			{
				if (item.getChecked())
				{
					setButtonsEnabled(true);
					return;
				}

				for (TreeItem subItem : item.getItems())
				{
					if (subItem.getChecked())
					{
						setButtonsEnabled(true);
						return;
					}
				}
			}

			setButtonsEnabled(false);
		}

		private void swtSelectedEventHandle(Event event)
		{
			TreeItem item = (TreeItem) event.item;
			StringBuilder sb = new StringBuilder();
			if (item.getParentItem() != null)
			{
				ToolsVO toolsVO = (ToolsVO) item.getParentItem().getData();
				VersionsVO versionsVO = (VersionsVO) item.getData();
				sb.append(toolsVO.getName());
				sb.append(System.lineSeparator());
				if (toolsVO.getSupportedTargets() != null && toolsVO.getSupportedTargets().size() > 0)
				{
					sb.append(Messages.SupportedTargetsDescriptionText);
					sb.append(toolsVO.getSupportedTargets().toString());
				}
				sb.append(System.lineSeparator());
				sb.append(versionsVO.getName());
				sb.append(System.lineSeparator());

				String name = item.getText(0);

				sb.append(name);
				sb.append(System.lineSeparator());
				sb.append(Messages.SizeDescriptionText);
				if (versionsVO.getVersionOsMap().get(name) != null)
				{
					sb.append(versionsVO.getVersionOsMap().get(name).getReadableSize());
				}
				sb.append(System.lineSeparator());
				sb.append(Messages.UrlDescriptionText);
				if (versionsVO.getVersionOsMap().get(name) != null)
				{
					sb.append(versionsVO.getVersionOsMap().get(name).getUrl());
				}

				descriptionText.setText(sb.toString());
			}
			else
			{
				ToolsVO toolsVO = (ToolsVO) item.getData();
				sb.append(toolsVO.getName());
				sb.append(System.lineSeparator());
				if (toolsVO.getSupportedTargets() != null && toolsVO.getSupportedTargets().size() > 0)
				{
					sb.append(Messages.SupportedTargetsDescriptionText);
					sb.append(toolsVO.getSupportedTargets().toString());
				}
				descriptionText.setText(sb.toString());
			}
		}

		private void swtCheckEventHandle(Event event)
		{
			TreeItem item = (TreeItem) event.item;
			boolean checked = item.getChecked();
			if (checked)
			{
				setButtonsEnabled(true);
			}
			checkItems(item, checked);
		}

		private void checkItems(TreeItem item, boolean checked)
		{
			item.setChecked(checked);
			if (item.getParentItem() != null)
			{
				String key = item.getText(0);
				VersionsVO versionsVO = (VersionsVO) item.getData();
				VersionDetailsVO versionDetailsVO = versionsVO.getVersionOsMap().get(key);
				if (versionDetailsVO != null)
				{
					versionDetailsVO.setSelected(checked);	
				}
			}
			TreeItem[] items = item.getItems();
			for (int i = 0; i < items.length; i++)
			{
				checkItems(items[i], checked);
			}
		}
	}

	private class DeleteButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			Map<ToolsVO, List<VersionsVO>> selectedItems = getSelectedTools();
			MessageBox messageBox = new MessageBox(parentComposite.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setMessage(Messages.RemoveToolMessageBox);
			messageBox.setText(Messages.RemoveToolMessageBoxTitle);
			int result = messageBox.open();
			if (result == SWT.YES)
			{
				toolsInstallationHandler = new ToolsInstallationHandler(logQueue,
						ManageToolsInstallationWizardPage.this, idfEnvironmentVariables);
				try
				{
					toolsInstallationHandler.operationToPerform(selectedItems, forceDownloadBtn.getSelection(),
							ToolsInstallationHandler.DELETING_TOOLS);
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
				toolsInstallationHandler.start();
			}
		}
	}

	private class InstallButtonSelectionAdapter extends SelectionAdapter
	{
		private boolean installPressed;

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			if (!installPressed)
			{
				btnFinish.setEnabled(false);
				toolsInstallationHandler = new ToolsInstallationHandler(logQueue,
						ManageToolsInstallationWizardPage.this, idfEnvironmentVariables);
				Map<ToolsVO, List<VersionsVO>> selectedItems = getSelectedTools();
				try
				{
					toolsInstallationHandler.operationToPerform(selectedItems, forceDownloadBtn.getSelection(),
							ToolsInstallationHandler.INSTALLING_TOOLS);
				}
				catch (Exception e1)
				{
					Logger.log(e1);
				}
				toolsInstallationHandler.start();
				installPressed = true;
				return;
			}

			restoreFinishButton();
		}
	}

	public void afterDeleteToolMessage()
	{
		MessageBox messageBox = new MessageBox(parentComposite.getShell(), SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
		messageBox.setMessage(Messages.RemoveToolMessageBoxFinish);
		messageBox.setText(Messages.RemoveToolMessageBoxTitle);
		int result = messageBox.open();
		if (result == SWT.YES)
		{
			restoreFinishButton();
			((ToolsManagerWizardDialog) parentWizardDialog).finishPressed();
		}
	}

	private class SelectRecommendedButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			toolsTree.setRedraw(false);
			for (TreeItem mainItem : toolsTree.getItems())
			{
				ToolsVO toolsVO = (ToolsVO) mainItem.getData();
				boolean alwaysInstall = toolsVO.getInstallType().equalsIgnoreCase(ALWAYS)
						|| toolsVO.getInstallType().equalsIgnoreCase(RECOMMENDED);
				mainItem.setChecked(alwaysInstall);

				for (TreeItem subItem : mainItem.getItems())
				{
					subItem.setChecked(alwaysInstall);
				}

				mainItem.setExpanded(alwaysInstall);
			}

			toolsTree.setRedraw(true);
			setButtonsEnabled(true);
		}
	}
}
