package com.espressif.idf.ui.partitiontable.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String PartitionTableEditorHandler_InformationMsg;
	public static String PartitionTableEditorHandler_InformationTitle;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
