package com.espressif.idf.core.component.registry.http.handlers;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import com.espressif.idf.core.logging.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class HandshakeHandler extends Handler.Abstract implements ComponentHttpServerHandler
{
	@Override
	public boolean handle(Request request, Response response, Callback callback)
	{
		Logger.log("Handling request: " + request.toString()); //$NON-NLS-1$
		response.setStatus(200);
		response.getHeaders().add("Content-Type", "application/json; charset=UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("serverAvailable", true); //$NON-NLS-1$
		JsonArray availableEndpoints = new JsonArray();
		jsonResponse.add("availableEndpoints", availableEndpoints); //$NON-NLS-1$
		Content.Sink.write(response, true, jsonResponse.toString(), callback);
		return true;
	}

	@Override
	public String getPath()
	{
		return "/handshake"; //$NON-NLS-1$
	}

	@Override
	public Handler.Abstract getHandler()
	{
		return this;
	}
}