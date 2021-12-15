/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.ui.tools.Messages;
import com.espressif.idf.ui.tools.ToolsInstallationHandler;
import com.espressif.idf.ui.tools.ToolsUtility;
import com.espressif.idf.ui.tools.images.ToolsImagesCache;
import com.espressif.idf.ui.tools.vo.ToolsVO;
import com.espressif.idf.ui.tools.vo.VersionDetailsVO;
import com.espressif.idf.ui.tools.vo.VersionsVO;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Shell for displaying tools information on UI
 * 
 * @author Ali Azam Rana
 *
 */
public class ManageToolsInstallationShell
{
	private static final String ESP_IDF_TOOLS_MANAGER = "ESP-IDF Tools Manager";
	private static final String WHITE = "white"; //$NON-NLS-1$
	private static final String MAC_OS = "mac"; //$NON-NLS-1$
	private static final String LINUX_OS = "linux"; //$NON-NLS-1$
	private static final String WIN_OS = "win"; //$NON-NLS-1$
	private static final String GREEN = "green"; //$NON-NLS-1$
	private static final String PNG_EXTENSION = ".png"; //$NON-NLS-1$
	private static final String WINDOWS = "windows"; //$NON-NLS-1$
	private Display display;
	private Shell shell;
	private List<ToolsVO> toolsVOs;
	private Text descriptionText;
	private Tree toolsTree;

	public ManageToolsInstallationShell(List<ToolsVO> toolsVOs)
	{
		this.toolsVOs = toolsVOs;
		this.display = PlatformUI.getWorkbench().getDisplay();
		this.shell = new Shell(display, SWT.TITLE | SWT.CLOSE);
		shell.setImage(ToolsImagesCache.getImage(ESP_IDF_TOOLS_MANAGER.concat(PNG_EXTENSION)));
		shell.setText(Messages.ToolsManagerShellHeading);
		shell.setLayout(new FillLayout(SWT.VERTICAL));

		Composite treeControlsComposite = new Composite(shell, SWT.NONE);
		treeControlsComposite.setLayout(new FillLayout(SWT.VERTICAL));

		Composite labelComposite = new Composite(treeControlsComposite, SWT.NONE);
		labelComposite.setLayout(new GridLayout(1, true));

		Label lblTreeTitle = new Label(labelComposite, SWT.NONE);
		lblTreeTitle.setText(Messages.ToolsTreeHeading);

		toolsTree = new Tree(labelComposite, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
		toolsTree.setHeaderVisible(true);
		toolsTree.setLinesVisible(true);
		toolsTree.setSize(424, 100);
		toolsTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
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

		addItemsToTree(toolsTree);

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

		Button btnInstallTools = new Button(composite, SWT.NONE);
		btnInstallTools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnInstallTools.setBounds(0, 0, 75, 25);
		btnInstallTools.setText(Messages.InstallToolsText);
		btnInstallTools.addSelectionListener(new InstallButtonSelectionAdapter());

		Button btnDeleteTools = new Button(composite, SWT.NONE);
		btnDeleteTools.setBounds(0, 0, 75, 25);
		btnDeleteTools.setText(Messages.DeleteToolsText);
	}

	private void addItemsToTree(Tree toolsTree)
	{
		for (ToolsVO toolsVO : toolsVOs)
		{
			TreeItem mainItem = new TreeItem(toolsTree, SWT.NONE);
			String[] itemText = getMainItemText(toolsVO);
			mainItem.setText(itemText);
			mainItem.setData(toolsVO);

			for (String key : toolsVO.getVersionVO().getVersionOsMap().keySet())
			{
				if (Platform.getOS().equals(Platform.OS_WIN32))
				{
					if (!key.toLowerCase().contains(WIN_OS))
					{
						continue;
					}
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
				String[] subItemText = getSubItemText(key, toolsVO.getVersionVO().getVersionOsMap(),
						ToolsUtility.isToolInstalled(toolsVO.getName(), toolsVO.getVersionVO().getName()));
				subItem.setText(subItemText);
				subItem.setData(toolsVO.getVersionVO());
				Image image = getOsImageForItem(subItem);
				Image installedImage = getInstalledImage(toolsVO.getName(), toolsVO.getVersionVO().getName());
				subItem.setImage(0, image);
				subItem.setImage(2, installedImage);
			}
		}
	}

	private Image getInstalledImage(String name, String versionName)
	{
		boolean isItemInstalled = ToolsUtility.isToolInstalled(name, versionName);
		if (isItemInstalled)
		{
			return ToolsImagesCache.getImage(GREEN.concat(PNG_EXTENSION));
		}

		return ToolsImagesCache.getImage(WHITE.concat(PNG_EXTENSION));
	}

	private Image getOsImageForItem(TreeItem item)
	{
		if (item.getText(0).toLowerCase().contains(LINUX_OS))
		{
			return ToolsImagesCache.getImage(LINUX_OS.concat(PNG_EXTENSION));
		}
		else if (item.getText(0).toLowerCase().contains(WIN_OS))
		{
			return ToolsImagesCache.getImage(WINDOWS.concat(PNG_EXTENSION));
		}
		else if (item.getText(0).toLowerCase().contains(MAC_OS))
		{
			return ToolsImagesCache.getImage(MAC_OS.concat(PNG_EXTENSION));
		}

		return null;
	}

	private String[] getSubItemText(String key, Map<String, VersionDetailsVO> versionOsMap, boolean isInstalled)
	{
		String[] textArr = new String[4];
		textArr[0] = key;
		textArr[1] = versionOsMap.get(key).getReadableSize();
		textArr[2] = isInstalled ? Messages.Installed : ""; //$NON-NLS-1$
		textArr[3] = ""; //$NON-NLS-1$
		return textArr;
	}

	private String[] getMainItemText(ToolsVO toolsVO)
	{
		String[] textArr = new String[4];
		textArr[0] = toolsVO.getName();
		textArr[1] = toolsVO.getReadableSize();
		textArr[2] = ""; //$NON-NLS-1$
		textArr[3] = toolsVO.getDescription();
		return textArr;
	}

	public void openShell()
	{
		shell.open();
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

	private class InstallButtonSelectionAdapter extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			List<VersionsVO> versionsVOs = getSelectedTools();
			ToolsInstallationHandler toolsInstallationHandler = new ToolsInstallationHandler(versionsVOs);
			shell.close();
			toolsInstallationHandler.installTools();
		}

		private List<VersionsVO> getSelectedTools()
		{
			List<VersionsVO> versionsVOs = new ArrayList<>();
			for (TreeItem item : toolsTree.getItems())
			{
				for (TreeItem subItem : item.getItems())
				{
					if (subItem.getChecked())
					{
						versionsVOs.add((VersionsVO) subItem.getData());
					}
				}
			}

			return versionsVOs;
		}
	}
}
