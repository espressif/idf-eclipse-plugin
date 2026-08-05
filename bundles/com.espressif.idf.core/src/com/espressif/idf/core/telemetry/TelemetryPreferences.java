/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry;

import java.util.UUID;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.IDFCorePreferenceConstants;
import com.espressif.idf.core.logging.Logger;

/**
 * Stores the opt-out flag and the anonymous state needed to count installations and updates.
 * <p>
 * The anonymous identifier and the report timestamps live in the configuration scope so that all workspaces of one
 * installation are counted as a single user.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public final class TelemetryPreferences
{
	/** System property to disable telemetry, for instance <code>-Didf.telemetry=false</code>. */
	public static final String TELEMETRY_SYSTEM_PROPERTY = "idf.telemetry"; //$NON-NLS-1$

	/** Environment variable to disable telemetry, for instance <code>IDF_TELEMETRY=0</code>. */
	public static final String TELEMETRY_ENV_VARIABLE = "IDF_TELEMETRY"; //$NON-NLS-1$

	private static final String INSTALL_ID = "telemetryInstallId"; //$NON-NLS-1$
	private static final String LAST_SESSION_REPORT = "telemetryLastSessionReport"; //$NON-NLS-1$
	private static final String LAST_REPORTED_VERSION = "telemetryLastReportedVersion"; //$NON-NLS-1$
	private static final String NOTICE_SHOWN = "telemetryNoticeShown"; //$NON-NLS-1$

	private TelemetryPreferences()
	{
	}

	/**
	 * @return <code>false</code> when the user opted out through the preference page, the system property or the
	 *         environment variable
	 */
	public static boolean isEnabled()
	{
		return !isDisabledByOverride() && isEnabledByPreference();
	}

	/**
	 * Tells whether reporting is switched off outside of the preference page, which is how shared and automated
	 * installations opt out. The override can only disable reporting, so that it can never overrule a user who opted
	 * out through the preference page.
	 *
	 * @return <code>true</code> when the system property or the environment variable disables reporting
	 */
	public static boolean isDisabledByOverride()
	{
		String property = System.getProperty(TELEMETRY_SYSTEM_PROPERTY);
		if (property == null || property.isBlank())
		{
			property = System.getenv(TELEMETRY_ENV_VARIABLE);
		}
		return property != null && isDisabledValue(property.trim());
	}

	/**
	 * @return the stored opt-out, which is off as soon as any scope switched reporting off
	 */
	public static boolean isEnabledByPreference()
	{
		boolean defaultValue = DefaultScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID).getBoolean(
				IDFCorePreferenceConstants.TELEMETRY_ENABLED,
				IDFCorePreferenceConstants.TELEMETRY_ENABLED_DEFAULT);
		return ConfigurationScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID)
				.getBoolean(IDFCorePreferenceConstants.TELEMETRY_ENABLED, defaultValue)
				&& InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID)
						.getBoolean(IDFCorePreferenceConstants.TELEMETRY_ENABLED, defaultValue);
	}

	/**
	 * Stores the opt-out for the whole installation, so that opting out in one workspace also applies to the others.
	 *
	 * @param enabled <code>true</code> to report usage statistics
	 */
	public static void setEnabled(boolean enabled)
	{
		for (IEclipsePreferences node : new IEclipsePreferences[] {
				ConfigurationScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID),
				InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID) })
		{
			node.putBoolean(IDFCorePreferenceConstants.TELEMETRY_ENABLED, enabled);
			flush(node);
		}
	}

	private static boolean isDisabledValue(String value)
	{
		return "false".equalsIgnoreCase(value) || "0".equals(value) || "no".equalsIgnoreCase(value) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				|| "off".equalsIgnoreCase(value) || "disabled".equalsIgnoreCase(value); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @return <code>true</code> once the installation told the user that usage statistics are reported
	 */
	public static boolean isNoticeShown()
	{
		return getStateNode().getBoolean(NOTICE_SHOWN, false);
	}

	public static void setNoticeShown()
	{
		IEclipsePreferences node = getStateNode();
		node.putBoolean(NOTICE_SHOWN, true);
		flush(node);
	}

	/**
	 * @return a random identifier created on first use, which is not derived from any machine or user attribute
	 */
	public static String getInstallId()
	{
		IEclipsePreferences node = getStateNode();
		String installId = node.get(INSTALL_ID, null);
		if (installId == null || installId.isBlank())
		{
			installId = UUID.randomUUID().toString();
			node.put(INSTALL_ID, installId);
			flush(node);
		}
		return installId;
	}

	public static long getLastSessionReport()
	{
		return getStateNode().getLong(LAST_SESSION_REPORT, 0L);
	}

	public static void setLastSessionReport(long timestamp)
	{
		IEclipsePreferences node = getStateNode();
		node.putLong(LAST_SESSION_REPORT, timestamp);
		flush(node);
	}

	public static String getLastReportedVersion()
	{
		return getStateNode().get(LAST_REPORTED_VERSION, ""); //$NON-NLS-1$
	}

	public static void setLastReportedVersion(String version)
	{
		IEclipsePreferences node = getStateNode();
		node.put(LAST_REPORTED_VERSION, version);
		flush(node);
	}

	private static IEclipsePreferences getStateNode()
	{
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
		if (node == null)
		{
			node = InstanceScope.INSTANCE.getNode(IDFCorePlugin.PLUGIN_ID);
		}
		return node;
	}

	private static void flush(IEclipsePreferences node)
	{
		try
		{
			node.flush();
		}
		catch (BackingStoreException e)
		{
			// A read-only configuration area only means the state is recomputed on the next start
			Logger.log(e, true);
		}
	}
}
