package com.espressif.idf.ui.gcov;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.FrameworkUtil;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.GcovUtility;
import com.espressif.idf.core.util.IDFUtil;

public class GcovFileView extends ViewPart implements ISelectionListener
{

	public static final String ID = "com.espressif.idf.ui.gcov.reportsView";

	private Table table;
	private IProject selectedProject;

	@Override
	public void createPartControl(Composite parent)
	{
		table = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		String[] titles = { "File Name", "Path", "Last Modified GCNO", "Last Modified GCDA", "Size GCNO", "Size GCDA" };
		for (int i = 0; i < titles.length; i++)
		{
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(180);
			column.setText(titles[i]);
			column.addSelectionListener(new TableColumnSelectionAdapter(column, i));
		}

		table.addListener(SWT.MouseDoubleClick, new Listener()
		{
			@Override
			public void handleEvent(Event event)
			{
				Point pt = new Point(event.x, event.y);
				TableItem item = table.getItem(pt);
				IFile file = (IFile) item.getData();
				
				if (item != null && file != null)
				{
//					OpenGCDialog openGcDialog = new OpenGCDialog(Display.getDefault().getActiveShell(), IDFUtil.getELFFilePath(selectedProject).toOSString(), file.getLocation());
//					openGcDialog.open();
					try
					{
						GcovUtility.setUpDialog(file, IDFUtil.getELFFilePath(selectedProject).toOSString());
						IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
					}
					catch (PartInitException e)
					{
						e.printStackTrace();
					}
				}
			}
		});

		// Create Toolbar and Buttons
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		Action refreshAction = new Action("Refresh")
		{
			public void run()
			{
				refreshList();
			}
		};
		mgr.add(refreshAction);

		// Initial population of the list
		refreshList();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
		if (!(selection instanceof IStructuredSelection))
			return;
		IStructuredSelection ssel = (IStructuredSelection) selection;
		Object obj = ssel.getFirstElement();
		if (obj instanceof IProject)
		{
			setSelectedProject((IProject) obj);
		}
	}

	private void refreshList()
	{
		for (TableItem item : table.getItems())
		{
			item.dispose();
		}

		if (getSelectedProject() == null)
		{
			// Get the current selection
			ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getSelectionService();
			IStructuredSelection structuredSelection = (IStructuredSelection) selectionService
					.getSelection("org.eclipse.ui.navigator.ProjectExplorer");

			if (structuredSelection != null && !structuredSelection.isEmpty())
			{
				Object firstElement = structuredSelection.getFirstElement();
				if (firstElement instanceof IProject)
				{
					setSelectedProject((IProject) firstElement);
				}
			}

			// Now we check the GCOV Utility to see if any project was selected
			if (getSelectedProject() == null)
			{
				setSelectedProject(GcovUtility.getSelectedProject());
			}
			
			// If no project is selected, ask the user to choose one
			if (getSelectedProject() == null)
			{
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						new org.eclipse.ui.model.WorkbenchLabelProvider());
				dialog.setElements(root.getProjects());
				dialog.setTitle("Select a Project");

				// only continue if the user pressed "OK"
				if (dialog.open() != Window.OK)
				{
					return;
				}

				setSelectedProject((IProject) dialog.getFirstResult());
			}
		}

		IProject project = getSelectedProject();

		if (project.exists() && project.isOpen())
		{
			try
			{
				project.accept(new IResourceVisitor()
				{
					public boolean visit(IResource resource)
					{
						if (resource instanceof IFile)
						{
							IFile file = (IFile) resource;
							if ("gcno".equals(file.getFileExtension()))
							{
								String parentDir = file.getParent().getRawLocation().toString();
								String partnerFile = parentDir + "/"
										+ file.getName().substring(0, file.getName().indexOf(".gcno")) + ".gcda";
								if (Files.exists(Paths.get(partnerFile)))
								{
									TableItem item = new TableItem(table, SWT.NONE);
									Image image = PlatformUI.getWorkbench().getEditorRegistry()
											.getImageDescriptor(file.getName()).createImage();
									item.setImage(0, image);
									item.setText(0, file.getName().substring(0, file.getName().indexOf(".gcno")));
									item.setText(1, file.getParent().getFullPath().toString());

									// gcno
									IFileInfo fileInfo = EFS.getLocalFileSystem().getStore(file.getLocationURI())
											.fetchInfo();
									SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
									String date = sdf.format(new Date(fileInfo.getLastModified()));
									item.setText(2, date);

									// gcda
									fileInfo = EFS.getLocalFileSystem().getStore(Path.fromOSString(partnerFile))
											.fetchInfo();
									item.setText(3, sdf.format(new Date(fileInfo.getLastModified())));

									java.nio.file.Path path = Paths.get(file.getRawLocationURI());
									try
									{
										long size = Files.size(path);
										item.setText(4, String.valueOf(size) + " bytes");
									}
									catch (Exception e)
									{
										item.setText(4, "Unknown");
									}

									path = Paths.get(partnerFile);
									try
									{
										long size = Files.size(path);
										item.setText(5, String.valueOf(size) + " bytes");
									}
									catch (Exception e)
									{
										item.setText(5, "Unknown");
									}

									item.setData(file);
								}
							}
						}
						return true; // continue visiting children
					}
				});
			}
			catch (CoreException e)
			{
				Logger.log(e);
			}
		}

	}

	@Override
	public void setFocus()
	{
		table.setFocus();
	}

	@Override
	public void dispose()
	{
		getSite().getPage().removeSelectionListener(this);
		super.dispose();
	}

	public IProject getSelectedProject()
	{
		return selectedProject;
	}

	public void setSelectedProject(IProject selectedProject)
	{
		this.selectedProject = selectedProject;
	}

	private class TableColumnSelectionAdapter extends SelectionAdapter
	{
		private TableColumn column;
		private int colIndex;

		private class TableRowData
		{
			public String[] textData;
			public Image image;
			public Object itemData;
		}

		private TableColumnSelectionAdapter(TableColumn column, int colIndex)
		{
			this.column = column;
			this.colIndex = colIndex;
		}

		@Override
		public void widgetSelected(SelectionEvent e)
		{
			TableItem[] items = table.getItems();
			Collator collator = Collator.getInstance(Locale.getDefault());

			// Determine increasing or decreasing order
			int direction = table.getSortDirection();
			if (table.getSortColumn() == column)
			{
				direction = direction == SWT.UP ? SWT.DOWN : SWT.UP;
			}
			else
			{
				direction = SWT.DOWN;
			}

			// Update sort direction and sort column
			table.setSortDirection(direction);
			table.setSortColumn(column);

			final int finalDirection = direction;

			// Implement your sorting logic here
			Arrays.sort(items, new Comparator<TableItem>()
			{
				public int compare(TableItem item1, TableItem item2)
				{
					if (finalDirection == SWT.UP)
					{
						return collator.compare(item1.getText(colIndex), item2.getText(colIndex));
					}
					else
					{
						return collator.compare(item2.getText(colIndex), item1.getText(colIndex));
					}
				}
			});

			// Copy existing item data
			List<TableRowData> copiedData = new ArrayList<>();
			for (TableItem item : items)
			{
				TableRowData rowData = new TableRowData();
				rowData.textData = new String[table.getColumnCount()];
				for (int j = 0; j < table.getColumnCount(); j++)
				{
					rowData.textData[j] = item.getText(j);
				}
				rowData.image = item.getImage(0); // Assuming image is in the first column
				rowData.itemData = item.getData();
				copiedData.add(rowData);
			}

			// Remove all items (dispose of TableItem objects)
			table.removeAll();

			// Repopulate the table
			for (TableRowData rowData : copiedData)
			{
				TableItem newItem = new TableItem(table, SWT.NONE);
				for (int j = 0; j < table.getColumnCount(); j++)
				{
					newItem.setText(j, rowData.textData[j]);
				}
				newItem.setImage(0, rowData.image); // Assuming image is in the first column
				newItem.setData(rowData.itemData);
			}
		}

	}
}
