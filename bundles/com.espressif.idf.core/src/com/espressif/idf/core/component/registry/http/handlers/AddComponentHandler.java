package com.espressif.idf.core.component.registry.http.handlers;

import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import com.espressif.idf.core.component.registry.ComponentToAdd;
import com.espressif.idf.core.component.vo.ComponentVO;
import com.espressif.idf.core.logging.Logger;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Handler for adding a component to eclipse.
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public class AddComponentHandler extends Handler.Abstract implements ComponentHttpServerHandler
{

	@Override
	public boolean handle(Request request, Response response, Callback callback) throws Exception
	{
		Logger.log("Handling request: " + request.toString()); //$NON-NLS-1$
		
		
		Content.Source source = Content.Source.from(request.read().getByteBuffer());
		String requestBody = Content.Source.asString(source);
		JsonObject requestJson = JsonParser.parseString(requestBody).getAsJsonObject();
		
		ComponentVO componentVO = new ComponentVO();
		componentVO.setName(requestJson.get("name").getAsString()); //$NON-NLS-1$
		componentVO.setNamespace(requestJson.get("namespace").getAsString()); //$NON-NLS-1$
		componentVO.setVersion(requestJson.get("version").getAsString()); //$NON-NLS-1$
		
		ComponentToAdd.getInstance().addComponent(componentVO);
		
		JsonObject jsonResponse = new JsonObject();
		jsonResponse.addProperty("queued", true); //$NON-NLS-1$
		response.setStatus(200);
		response.getHeaders().add("Content-Type", "application/json; charset=UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		Content.Sink.write(response, true, jsonResponse.toString(), callback);
		callback.succeeded();
		return true;
	}

	@Override
	public String getPath()
	{
		return "/addComponent"; //$NON-NLS-1$
	}

	@Override
	public Abstract getHandler()
	{
		
		return this;
	}

}
