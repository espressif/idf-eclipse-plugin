/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.telemetry;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import com.espressif.idf.core.IDFCorePlugin;
import com.espressif.idf.core.logging.Logger;

/**
 * Reports anonymous usage events to Azure Application Insights so that installations, updates and active users of
 * Espressif-IDE can be counted.
 * <p>
 * Every report is best effort: it runs in a background job, never blocks the workbench and silently gives up when the
 * endpoint cannot be reached. Nothing is sent when the user opted out.
 *
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public final class TelemetryService
{
	/** Prefix identifying events coming from Espressif-IDE and the IDF Eclipse plugin. */
	public static final String EVENT_PREFIX = "espressif-ide/"; //$NON-NLS-1$

	/** Fired the first time an installation reports, before any other event. */
	public static final String EVENT_INSTALL = EVENT_PREFIX + "install"; //$NON-NLS-1$

	/** Fired when the installed Espressif plugin version changed since the previous start. */
	public static final String EVENT_UPDATE = EVENT_PREFIX + "update"; //$NON-NLS-1$

	/** Fired at most once a day to count active users. */
	public static final String EVENT_SESSION = EVENT_PREFIX + "session"; //$NON-NLS-1$

	static final long SESSION_INTERVAL_MS = TimeUnit.HOURS.toMillis(24);

	private static final String CONNECTION_STRING_PROPERTY = "idf.telemetry.connectionString"; //$NON-NLS-1$
	private static final String CONNECTION_STRING_ENV = "APPLICATIONINSIGHTS_CONNECTION_STRING"; //$NON-NLS-1$
	private static final String DEFAULT_CONNECTION_STRING = "InstrumentationKey=0cc48b26-38e9-453d-81fe-15849ac04c36;IngestionEndpoint=https://southeastasia-1.in.applicationinsights.azure.com/"; //$NON-NLS-1$

	private static final String ESPRESSIF_IDE_PRODUCT_ID = "com.espressif.idf.branding.idf"; //$NON-NLS-1$
	private static final String BRANDING_BUNDLE_ID = "com.espressif.idf.branding"; //$NON-NLS-1$
	private static final String PLATFORM_BUNDLE_ID = "org.eclipse.platform"; //$NON-NLS-1$
	private static final String DISTRIBUTION_IDE = "espressif-ide"; //$NON-NLS-1$
	private static final String DISTRIBUTION_PLUGIN = "eclipse-plugin"; //$NON-NLS-1$

	private static final Duration TIMEOUT = Duration.ofSeconds(15);
	private static final String UNKNOWN = "unknown"; //$NON-NLS-1$

	private static final TelemetryService INSTANCE = new TelemetryService();

	private final String sessionId = UUID.randomUUID().toString();

	private HttpClient httpClient;

	private TelemetryService()
	{
	}

	public static TelemetryService getInstance()
	{
		return INSTANCE;
	}

	public boolean isEnabled()
	{
		return TelemetryPreferences.isEnabled() && getConnection().isPresent();
	}

	/**
	 * Reports the installation, the update and the daily session events for the running installation. Intended to be
	 * called once per workbench start.
	 */
	public void reportSessionStart()
	{
		if (!isEnabled())
		{
			return;
		}
		schedule(this::reportStartupEvents);
	}

	/**
	 * Sends the startup events one after the other and remembers what was reported only once the endpoint accepted it,
	 * so that an installation started without a network connection is still counted on a later start.
	 */
	private void reportStartupEvents()
	{
		try
		{
			String version = getPluginVersion();
			String lastVersion = TelemetryPreferences.getLastReportedVersion();
			if (lastVersion.isBlank())
			{
				if (send(EVENT_INSTALL, Map.of()))
				{
					TelemetryPreferences.setLastReportedVersion(version);
				}
			}
			else if (!lastVersion.equals(version)
					&& send(EVENT_UPDATE, Map.of("previousVersion", lastVersion))) //$NON-NLS-1$
			{
				TelemetryPreferences.setLastReportedVersion(version);
			}

			long now = System.currentTimeMillis();
			if (shouldReportSession(TelemetryPreferences.getLastSessionReport(), now) && send(EVENT_SESSION, Map.of()))
			{
				TelemetryPreferences.setLastSessionReport(now);
			}
		}
		catch (Exception e)
		{
			Logger.log(e, true);
		}
	}

	/**
	 * @param lastReport time of the previous session report, in milliseconds since the epoch
	 * @param now        current time, in milliseconds since the epoch
	 * @return <code>true</code> when a session event is due
	 */
	public static boolean shouldReportSession(long lastReport, long now)
	{
		if (lastReport <= 0 || lastReport > now)
		{
			return true;
		}
		return now - lastReport >= SESSION_INTERVAL_MS;
	}

	/**
	 * Queues an anonymous event. The call returns immediately and the event is dropped when telemetry is disabled.
	 *
	 * @param eventName  name of the event
	 * @param properties additional string properties, which must not contain personal or project specific data
	 */
	public void sendEvent(String eventName, Map<String, String> properties)
	{
		if (!isEnabled())
		{
			return;
		}
		schedule(() -> send(eventName, properties));
	}

	private void schedule(Runnable reporter)
	{
		Job job = new Job("Espressif-IDE usage report") //$NON-NLS-1$
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				reporter.run();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.DECORATE);
		job.schedule();
	}

	/**
	 * @return <code>true</code> when the endpoint accepted the event
	 */
	private boolean send(String eventName, Map<String, String> properties)
	{
		Optional<TelemetryConnection> connection = getConnection();
		if (connection.isEmpty())
		{
			return false;
		}

		try
		{
			Map<String, String> eventProperties = new HashMap<>(getCommonProperties());
			if (properties != null)
			{
				eventProperties.putAll(properties);
			}

			TelemetryConnection telemetryConnection = connection.get();
			String payload = TelemetryEnvelope.build(telemetryConnection.getInstrumentationKey(), eventName, getTags(),
					eventProperties, Instant.now());
			HttpRequest request = HttpRequest.newBuilder(telemetryConnection.getTrackUri()).timeout(TIMEOUT)
					.header("Content-Type", "application/json") //$NON-NLS-1$ //$NON-NLS-2$
					.POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8)).build();
			HttpResponse<Void> response = getHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
			if (response.statusCode() >= 300)
			{
				Logger.log("Usage report rejected with status " + response.statusCode(), true); //$NON-NLS-1$
				return false;
			}
			return true;
		}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}
		catch (Exception e)
		{
			// Telemetry must never surface as an error to the user
			Logger.log(e, true);
		}
		return false;
	}

	private synchronized HttpClient getHttpClient()
	{
		if (httpClient == null)
		{
			httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
		}
		return httpClient;
	}

	private Map<String, String> getTags()
	{
		Map<String, String> tags = new LinkedHashMap<>();
		tags.put("ai.user.id", TelemetryPreferences.getInstallId()); //$NON-NLS-1$
		tags.put("ai.session.id", sessionId); //$NON-NLS-1$
		tags.put("ai.application.ver", getPluginVersion()); //$NON-NLS-1$
		tags.put("ai.device.osVersion", //$NON-NLS-1$
				getSystemProperty("os.name") + ' ' + getSystemProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
		tags.put("ai.internal.sdkVersion", "espressif-ide:" + getPluginVersion()); //$NON-NLS-1$ //$NON-NLS-2$
		return tags;
	}

	private Map<String, String> getCommonProperties()
	{
		Map<String, String> properties = new LinkedHashMap<>();
		properties.put("pluginVersion", getPluginVersion()); //$NON-NLS-1$
		properties.put("ideVersion", getIdeVersion()); //$NON-NLS-1$
		properties.put("eclipseVersion", getBundleVersion(PLATFORM_BUNDLE_ID)); //$NON-NLS-1$
		properties.put("productId", getProductId()); //$NON-NLS-1$
		properties.put("distribution", getDistribution()); //$NON-NLS-1$
		properties.put("os", getSystemProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("osVersion", getSystemProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("arch", getSystemProperty("os.arch")); //$NON-NLS-1$ //$NON-NLS-2$
		properties.put("javaVersion", getSystemProperty("java.version")); //$NON-NLS-1$ //$NON-NLS-2$
		return properties;
	}

	private String getDistribution()
	{
		IProduct product = Platform.getProduct();
		if (product != null && ESPRESSIF_IDE_PRODUCT_ID.equals(product.getId()))
		{
			return DISTRIBUTION_IDE;
		}
		return DISTRIBUTION_PLUGIN;
	}

	private String getProductId()
	{
		IProduct product = Platform.getProduct();
		return product != null && product.getId() != null ? product.getId() : UNKNOWN;
	}

	/**
	 * @param symbolicName bundle to look up
	 * @return the installed version, or {@value #UNKNOWN} when the bundle is not part of the installation
	 */
	private String getBundleVersion(String symbolicName)
	{
		Bundle bundle = Platform.getBundle(symbolicName);
		return bundle != null ? bundle.getVersion().toString() : UNKNOWN;
	}

	/**
	 * Returns the Espressif release version, which is the same value for the standalone IDE and for the plugin
	 * installed into a plain Eclipse. The product version is only a fallback, because for update site users it
	 * reports the version of their Eclipse package rather than the Espressif release.
	 *
	 * @return the Espressif release version, for instance <code>4.4.0.202608051100</code>
	 */
	private String getIdeVersion()
	{
		String brandingVersion = getBundleVersion(BRANDING_BUNDLE_ID);
		if (!UNKNOWN.equals(brandingVersion))
		{
			return brandingVersion;
		}

		IProduct product = Platform.getProduct();
		if (product != null)
		{
			Bundle definingBundle = product.getDefiningBundle();
			if (definingBundle != null)
			{
				return definingBundle.getVersion().toString();
			}
		}
		return getPluginVersion();
	}

	private String getPluginVersion()
	{
		Bundle bundle = IDFCorePlugin.getPlugin() != null ? IDFCorePlugin.getPlugin().getBundle() : null;
		return bundle != null ? bundle.getVersion().toString() : UNKNOWN;
	}

	private String getSystemProperty(String key)
	{
		String value = System.getProperty(key);
		return value != null ? value : UNKNOWN;
	}

	private Optional<TelemetryConnection> getConnection()
	{
		String connectionString = System.getProperty(CONNECTION_STRING_PROPERTY);
		if (connectionString == null || connectionString.isBlank())
		{
			connectionString = System.getenv(CONNECTION_STRING_ENV);
		}
		if (connectionString == null || connectionString.isBlank())
		{
			connectionString = DEFAULT_CONNECTION_STRING;
		}
		return TelemetryConnection.parse(connectionString);
	}
}
