package com.espressif.idf.ui.nvs.dialog;

import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.EditorPart;

public class NvsEditor extends EditorPart
{
	// Unique ID for the editor, used in plugin.xml and the Handler
	public static final String ID = "com.espressif.idf.ui.nvs.nvsEditor"; //$NON-NLS-1$

	private NvsCsvEditorPage editorPage;
	private IFile csvFile;
	private boolean isDirty = false;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException
	{
		// 1. Set Site and Input
		setSite(site);
		setInput(input);

		// 2. Validate and retrieve the IFile resource
		csvFile = ResourceUtil.getFile(input);
		if (csvFile == null)
		{
			throw new PartInitException("Editor input must be a file resource."); //$NON-NLS-1$
		}

		// 3. Set the editor's title
		setPartName(csvFile.getName());
		setTitleToolTip(csvFile.getFullPath().toString());
	}

	@Override
	public void createPartControl(Composite parent)
	{
		// Pass a callback to the page so it can notify the editor when it becomes dirty.
		Consumer<Boolean> dirtyStateListener = this::setDirty;
		editorPage = new NvsCsvEditorPage(parent, csvFile, dirtyStateListener);
		editorPage.createControl(); // The page creates its UI elements
	}

	@Override
	public void doSave(IProgressMonitor monitor)
	{
		monitor.beginTask("Saving NVS table...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$

		boolean saveSuccessful = editorPage.getSaveAction();
		if (saveSuccessful)
		{
			setDirty(false); // Only clear dirty state if save was successful
		}

		monitor.done();
	}

	@Override
	public void doSaveAs()
	{
		// Nothing to do
	}

	@Override
	public boolean isSaveAsAllowed()
	{
		return false;
	}

	@Override
	public boolean isDirty()
	{
		return isDirty;
	}

	private void setDirty(boolean dirty)
	{
		if (this.isDirty != dirty)
		{
			this.isDirty = dirty;
			firePropertyChange(PROP_DIRTY);
			if (editorPage != null)
			{
				editorPage.updateErrorMessage();
			}
		}
	}

	@Override
	public void setFocus()
	{
		if (editorPage != null)
		{
			editorPage.setFocus();
		}
	}
}
