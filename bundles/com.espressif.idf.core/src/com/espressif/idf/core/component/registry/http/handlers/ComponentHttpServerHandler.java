package com.espressif.idf.core.component.registry.http.handlers;

import org.eclipse.jetty.server.Handler;

public interface ComponentHttpServerHandler
{
	String getPath();
    Handler.Abstract getHandler();

}
