/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.ui.UIPlugin;
import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.ToolsInstallationHandler;
import com.espressif.idf.ui.tools.ToolsUtility;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionDetailsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;

/**
 * Shell for displaying tools information on UI
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationShell
{
	private static final String PNG_EXTENSION = ".png"; //$NON-NLS-1$
	private static final String YELLOW = "icons/tools/yellow.png"; //$NON-NLS-1$
	private static final String AVAILABLE = "available"; //$NON-NLS-1$
	private static final String RECOMMENDED = "recommended"; //$NON-NLS-1$
	private static final String ALWAYS = "always"; //$NON-NLS-1$
	private static final String ALL = "all"; //$NON-NLS-1$
	private static final String ESP_IDF_TOOLS_MANAGER_ICON = "icons/tools/ESP-IDFManageToolsInstalltion.png"; //$NON-NLS-1$
	private static final String WHITE = "icons/tools/white.png"; //$NON-NLS-1$
	private static final String MAC_OS = "mac"; //$NON-NLS-1$
	private static final String IMG_MAC_OS = "icons/tools/".concat(MAC_OS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String LINUX_OS = "linux"; //$NON-NLS-1$
	private static final String IMG_LINUX_OS = "icons/tools/".concat(LINUX_OS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private static final String WIN_OS = "win"; //$NON-NLS-1$
	private static final String GREEN = "icons/tools/green.png"; //$NON-NLS-1$
	private static final String WINDOWS = "windows"; //$NON-NLS-1$
	private static final String IMG_WINDOWS_OS = "icons/tools/".concat(WINDOWS).concat(PNG_EXTENSION); //$NON-NLS-1$
	private Display display;
	private Shell shell;
	private List<ToolsVO> toolsVOs;
	private Text descriptionText;
	private Tree toolsTree;
	private Button btnInstallTools;
	private Button btnDeleteTools;
	private Button btnSelectAll;
	private Button btnDeselectAll;
	private Combo filterTargetBox;
	private boolean itemChecked = false;
	private Button chkAvailableVersions;

	public ManageToolsInstallationShell(List<ToolsVO> toolsVOs)
	{
		this.toolsVOs = toolsVOs;
		this.display = PlatformUI.getWorkbench().getDisplay();
		this.shell = new Shell(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				SWT.CLOSE | SWT.MAX | SWT.TITLE);
		shell.setImage(UIPlugin.getImage(ESP_IDF_TOOLS_MANAGER_ICON));
		shell.setText(Messages.ToolsManagerShellHeading);
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite treeControlsComposite = new Composite(shell, SWT.NONE);
		treeControlsComposite.setLayout(new FillLayout(SWT.VERTICAL));

		Composite subControlComposite = new Composite(treeControlsComposite, SWT.NONE);
		subControlComposite.setLayout(new GridLayout(2, false));

		btnSelectAll = new Button(subControlComposite, SWT.PUSH);
		btnSelectAll.setText(Messages.SelectAllButton);
		btnSelectAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectAllItems(true);
				setButtonsEnabled(true);
			}
		});

		btnDeselectAll = new Button(subControlComposite, SWT.PUSH);
		btnDeselectAll.setText(Messages.DeselectAllButton);
		btnDeselectAll.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				selectAllItems(false);
				setButtonsEnabled(false);
			}
		});
		
		chkAvailableVersions = new Button(subControlComposite, SWT.CHECK);
		chkAvailableVersions.setText("Show Available Versions Only");
		chkAvailableVersions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 4));
		chkAvailableVersions.addSelectionListener(new SelectionAdapter()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				toolsTree.removeAll();
				addItemsToTree(toolsTree, chkAvailableVersions.getSelection());
			}
		});

		Label targetFilterLabel = new Label(subControlComposite, SWT.NONE);
		targetFilterLabel.setText(Messages.FilterTargets);

		filterTargetBox = new Combo(subControlComposite, SWT.READ_ONLY);
		filterTargetBox.setItems(getTargetFilterItems());
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
		
		toolsTree = new Tree(subControlComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		toolsTree.setHeaderVisible(true);
		toolsTree.setLinesVisible(true);
		toolsTree.setSize(424, 100);
		toolsTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolsTree.addListener(SWT.Selection, new TreeSelectionListener());

		TreeColumn trclmnName = new TreeColumn(toolsTree, SWT.NONE);
		trclmnName.setWidth(200);
		trclmnName.setText(Messages.ToolsTreeNameCol);

		TreeColumn trclmnSize = new TreeColumn(toolsTree, SWT.NONE);
		trclmnSize.setWidth(150);
		trclmnSize.setText(Messages.ToolsTreeSizeCol);

		TreeColumn trclmnStatus = new TreeColumn(toolsTree, SWT.NONE);
		trclmnStatus.setWidth(100);
		trclmnStatus.setText(Messages.ToolsTreeStatusCol);

		TreeColumn trclmnDescription = new TreeColumn(toolsTree, SWT.NONE);
		trclmnDescription.setWidth(500);
		trclmnDescription.setText(Messages.DescriptionText);

		addItemsToTree(toolsTree, false);

		Composite buttonsComposite = new Composite(shell, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(1, false));

		Label lblDescription = new Label(buttonsComposite, SWT.NONE);
		lblDescription.setText(Messages.DescriptionText);

		descriptionText = new Text(buttonsComposite,
				SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		gd_text.widthHint = 404;
		descriptionText.setLayoutData(gd_text);

		Composite composite = new Composite(buttonsComposite, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, true, 1, 1));

		btnInstallTools = new Button(composite, SWT.NONE);
		btnInstallTools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnInstallTools.setBounds(0, 0, 75, 25);
		btnInstallTools.setText(Messages.InstallToolsText);
		btnInstallTools.addSelectionListener(new InstallButtonSelectionAdapter());
		btnInstallTools.setEnabled(false);

		btnDeleteTools = new Button(composite, SWT.NONE);
		btnDeleteTools.setBounds(0, 0, 75, 25);
		btnDeleteTools.setText(Messages.DeleteToolsText);
		btnDeleteTools.addSelectionListener(new DeleteButtonSelectionAdapter());
		btnDeleteTools.setEnabled(false);

		setButtonsEnabled(itemChecked);
	}

	private String[] getTargetFilterItems()
	{
		Set<String> targets = new HashSet<>();
		for (ToolsVO toolsVO : toolsVOs)
		{
			targets.addAll(toolsVO.getSupportedTargets());
		}
		return targets.toArray(String[]::new);
	}

	private void addItemsToTree(Tree toolsTree, boolean availableOnly)
	{
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
			boolean windowsx86_64 = false;
			
			if (availableOnly)
			{
				addAvailableToolVersions(toolsVO, mainItem);
				platformAvailable = true;
			}
			else
			{
				for (VersionsVO versionsVO : toolsVO.getVersionVO())
				{
					isInstalled = ToolsUtility.isToolInstalled(toolsVO.getName(), versionsVO.getName());
					for (String key : versionsVO.getVersionOsMap().keySet())
					{
						if (Platform.getOS().equals(Platform.OS_WIN32))
						{
							if (!key.toLowerCase().contains(WIN_OS) || windowsx86_64)
							{
								continue;
							}

							windowsx86_64 = true;
						}
						else if (Platform.getOS().equals(Platform.OS_LINUX))
						{
							if (!key.toLowerCase().contains(LINUX_OS))
							{
								continue;
							}
						}
						else if (Platform.getOS().equals(Platform.OS_MACOSX))
						{
							if (!key.toLowerCase().contains(MAC_OS))
							{
								continue;
							}
						}

						TreeItem subItem = new TreeItem(mainItem, SWT.NONE);
						String[] subItemText = getSubItemText(key, versionsVO.getVersionOsMap(), versionsVO.getName(),
								versionsVO.getStatus(), isInstalled ? 1 : 2);
						subItem.setText(subItemText);
						subItem.setData(versionsVO);
						Image image = getOsImageForItem(subItem);
						subItem.setImage(0, image);
						subItem.setImage(2, isInstalled ? UIPlugin.getImage(GREEN)
								: UIPlugin.getImage(WHITE));
						subItem.setChecked(alwaysInstall);
						platformAvailable = true;
					}
				}
			}

			String[] itemText = getMainItemText(toolsVO, isInstalled);
			mainItem.setText(itemText);
			mainItem.setData(toolsVO);
			Image installedImage = isInstalled ? UIPlugin.getImage(GREEN)
					: UIPlugin.getImage(WHITE);
			mainItem.setImage(2, installedImage);

			if (!platformAvailable)
			{
				mainItem.dispose();
			}
		}
	}

	private void addAvailableToolVersions(ToolsVO toolsVO, TreeItem mainItem)
	{
		List<String> availableVersions = ToolsUtility.getAvailableToolVersions(toolsVO);
		for (String availableVersion : availableVersions)
		{
			List<TreeItem> subItems = Arrays.asList(mainItem.getItems());
			subItems = subItems.stream().filter(sb -> ((VersionsVO) sb.getData()).getName().equals(availableVersion))
					.collect(Collectors.toList());
			if (subItems.size() > 0)
			{
				continue;
			}
			boolean isInstalled = ToolsUtility.isToolInstalled(toolsVO.getName(), availableVersion);
			Image installedImage = isInstalled ? UIPlugin.getImage(GREEN)
					: UIPlugin.getImage(YELLOW);
			TreeItem subItem = new TreeItem(mainItem, SWT.NONE);
			VersionsVO versionsVO = new VersionsVO();
			versionsVO.setName(availableVersion);
			versionsVO.setStatus(AVAILABLE);
			String[] subItemText = getSubItemText(Platform.getOS(), null, availableVersion,
					versionsVO.getStatus(), isInstalled ? 1 : 3);
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
		textArr[0] = key;
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

		textArr[3] = name.concat(" (").concat(status).concat(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return textArr;
	}

	private String[] getMainItemText(ToolsVO toolsVO, boolean isInstalled)
	{
		String[] textArr = new String[4];
		textArr[0] = toolsVO.getName();
		textArr[1] = toolsVO.getReadableSize();
		textArr[2] = isInstalled ? Messages.Installed : Messages.NotInstalled;
		textArr[3] = toolsVO.getDescription();
		return textArr;
	}

	private List<ToolsVO> getSelectedTools()
	{
		List<ToolsVO> toolsVOs = new ArrayList<ToolsVO>();
		for (TreeItem item : toolsTree.getItems())
		{
			for (TreeItem subItem : item.getItems())
			{
				if (subItem.getChecked())
				{
					toolsVOs.add((ToolsVO) item.getData());
				}
			}
		}

		return toolsVOs;
	}

	public void openShell()
	{
		centerScreen();
		shell.open();
	}

	private void centerScreen()
	{
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		/** calculate the centre */
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	private void setButtonsEnabled(boolean enabled)
	{
		btnDeleteTools.setEnabled(enabled);
		btnInstallTools.setEnabled(enabled);
		btnDeleteTools.redraw();
		btnInstallTools.redraw();
	}

	private void selectAllItems(boolean selectAll)
	{
		for (TreeItem item : toolsTree.getItems())
		{
			item.setChecked(selectAll);
			for (TreeItem subItem : item.getItems())
			{
				subItem.setChecked(selectAll);
			}
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
				sb.append(Messages.SupportedTargetsDescriptionText);
				sb.append(toolsVO.getSupportedTargets().toString());
				sb.append(System.lineSeparator());
				sb.append(versionsVO.getName());
				sb.append(System.lineSeparator());

				String name = item.getText(0);

				sb.append(name);
				sb.append(System.lineSeparator());
				sb.append(Messages.SizeDescriptionText);
				sb.append(versionsVO.getVersionOsMap().get(name).getReadableSize());
				sb.append(System.lineSeparator());
				sb.append(Messages.UrlDescriptionText);
				sb.append(versionsVO.getVersionOsMap().get(name).getUrl());

				descriptionText.setText(sb.toString());
			}
			else
			{
				ToolsVO toolsVO = (ToolsVO) item.getData();
				sb.append(toolsVO.getName());
				sb.append(System.lineSeparator());
				sb.append(Messages.SupportedTargetsDescriptionText);
				sb.append(toolsVO.getSupportedTargets().toString());
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
				versionsVO.getVersionOsMap().get(key).setSelected(checked);
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
			List<ToolsVO> toolsVOs = getSelectedTools();
			MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES | SWT.NO);
			messageBox.setMessage(Messages.RemoveToolMessageBox);
			messageBox.setText(Messages.RemoveToolMessageBoxTitle);
			int result = messageBox.open();
			if (result == SWT.YES)
			{
				ToolsInstallationHandler toolsInstallationHandler = new ToolsInstallationHandler(toolsVOs);
				shell.close();
				toolsInstallationHandler.deleteTools();
			}
		}
	}

	private class InstallButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			List<ToolsVO> toolsVOs = getSelectedTools();
			ToolsInstallationHandler toolsInstallationHandler = new ToolsInstallationHandler(toolsVOs);
			shell.close();
			toolsInstallationHandler.installTools();
		}
	}
}
