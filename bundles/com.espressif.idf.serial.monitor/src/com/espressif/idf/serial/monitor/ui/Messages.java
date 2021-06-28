package com.espressif.idf.serial.monitor.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.serial.monitor.SerialMonitorBundle;

public class Messages
{
	private static final String MESSAGES = SerialMonitorBundle.PLUGIN_ID + ".ui.messages"; //$NON-NLS-1$

	public static String SerialMonitorPage_Field_NumberOfCharsInLine;
	public static String SerialMonitorPage_Field_NumberOfLines;
	public static String SerialMonitorPagePropertyPage_description;
	static
	{
		// initialise resource bundle
		NLS.initializeMessages(MESSAGES, Messages.class);
	}

	private static ResourceBundle RESOURCE_BUNDLE;
	static
	{
		try
		{
			RESOURCE_BUNDLE = ResourceBundle.getBundle(MESSAGES);
		}
		catch (MissingResourceException e)
		{
			IDFCorePlugin.getPlugin().getLog().error("Error Loading Resource Bundle", e);
		}
	}

	private Messages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}

	public static ResourceBundle getResourceBundle()
	{
		return RESOURCE_BUNDLE;
	}

}
