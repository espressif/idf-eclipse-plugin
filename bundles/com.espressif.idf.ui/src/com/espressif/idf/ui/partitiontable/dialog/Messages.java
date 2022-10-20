package com.espressif.idf.ui.partitiontable.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String PartitionTableEditorDialog_AddRowAction;
	public static String PartitionTableEditorDialog_ConfirmDeleteMsg;
	public static String PartitionTableEditorDialog_DeleteConfirmationAction;
	public static String PartitionTableEditorDialog_DeleteSelectedAction;
	public static String PartitionTableEditorDialog_SaveAction;
	public static String PartitionTableEditorDialog_SaveErrorMsg;
	public static String PartitionTableEditorDialog_SaveErrorTitle;
	public static String PartitionTableEditorDialog_SaveInfoMsg;
	public static String PartitionTableEditorDialog_SaveInfoTitle;
	public static String PartitionTableEditorDialog_Title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
