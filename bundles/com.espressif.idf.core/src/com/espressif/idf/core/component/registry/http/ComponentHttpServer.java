package com.espressif.idf.core.component.registry.http;

import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.CrossOriginHandler;

import com.espressif.idf.core.component.registry.http.handlers.ComponentHttpServerHandler;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;

public class ComponentHttpServer
{
	private Server server;
	private ContextHandlerCollection handlersCollection;
	private ServerConnector connector;
	private int port = 8080; // Default port

	public ComponentHttpServer()
	{
		server = new Server();
		connector = new ServerConnector(server, 1, 1, new HttpConnectionFactory());
		handlersCollection = new ContextHandlerCollection();
	}

	public void start() throws Exception
	{
		findPort();
		connector.setPort(port);
		connector.setHost("localhost"); //$NON-NLS-1$
		connector.setAcceptQueueSize(100);
		server.addConnector(connector);

		initiateHandlers();
		CrossOriginHandler corsHandler = new CrossOriginHandler();
		corsHandler.setAllowedOriginPatterns(Set.of("https://components.espressif.com/")); //$NON-NLS-1$
		corsHandler.setHandler(handlersCollection);
		
		server.setHandler(corsHandler);

		server.start();
		Logger.log("Component HTTP Server started on port " + port); //$NON-NLS-1$
	}

	private void findPort()
	{
		for (int p = 8080; p <= 8180; p++)
		{
			try (java.net.ServerSocket socket = new java.net.ServerSocket(p))
			{
				port = p;
				Logger.log("Found available port: " + port); //$NON-NLS-1$
				return;
			}
			catch (java.io.IOException e)
			{
				// Port is in use, try next
			}
		}
		throw new RuntimeException("No available port found in range 8080-8180"); //$NON-NLS-1$
	}

	private void initiateHandlers()
	{
		ServiceLoader<ComponentHttpServerHandler> loader = ServiceLoader.load(ComponentHttpServerHandler.class);
		for (ComponentHttpServerHandler handler : loader)
		{
			String path = handler.getPath();
			if (StringUtil.isEmpty(path))
			{
				Logger.log("Handler path is null or empty, skipping: " + handler.getClass().getName()); //$NON-NLS-1$
				continue;
			}

			ContextHandler contextHandler = new ContextHandler(path);
			contextHandler.setHandler(handler.getHandler());
			handlersCollection.addHandler(contextHandler);
			Logger.log("Added handler for path: " + path); //$NON-NLS-1$
		}
	}
}