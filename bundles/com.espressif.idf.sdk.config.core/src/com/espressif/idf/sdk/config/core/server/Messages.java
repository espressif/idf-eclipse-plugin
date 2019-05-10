package com.espressif.idf.sdk.config.core.server;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS
{
	public static String JsonConfigServerRunnable_CmdToBeExecuted;
	private static final String BUNDLE_NAME = "com.espressif.idf.sdk.config.core.server.messages"; //$NON-NLS-1$
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
