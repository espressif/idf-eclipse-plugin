package com.espressif.idf.ui.nvs.dialog;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String NvsTableEditorDialog_AddRowAction;
	public static String NvsEditorDialog_ConfirmDeleteMsg;
	public static String NvsTableEditorDialog_DeleteConfirmationAction;
	public static String NvsTableEditorDialog_DeleteSelectedAction;
	public static String NvsTableEditorDialog_SaveAction;
	public static String NvsTableEditorDialog_SaveErrorMsg;
	public static String NvsTableEditorDialog_SaveErrorTitle;
	public static String NvsTableEditorDialog_SaveInfoMsg;
	public static String NvsTableEditorDialog_SaveInfoTitle;
	public static String NvsTableEditorDialog_Title;
	public static String NvsTableEditorSaveAndQuitButtonLable;
	public static String NvsTableEditorIsEncryptedActionMsg;
	public static String NvsTableEditorGeneratePartitionActionMsg;
	public static String NvsTableEditorSizeOfPartitionLblMsg;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
