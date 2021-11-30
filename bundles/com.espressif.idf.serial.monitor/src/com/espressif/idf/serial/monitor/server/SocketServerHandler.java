/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.serial.monitor.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Queue;

import org.eclipse.core.resources.IProject;

import com.espressif.idf.core.util.SDKConfigJsonReader;

/**
 * Socket server handler responsible for starting and handling the socket server
 * 
 * @author Ali Azam Rana
 *
 */
public class SocketServerHandler
{
	private static TerminalWebSocketServer terminalWebSocketServer;

	public int startServer() throws Exception
	{
		int port = findPort();
		terminalWebSocketServer = new TerminalWebSocketServer(port);
		terminalWebSocketServer.start();
		return terminalWebSocketServer.getPort();
	}

	public void broadcastMessageToClients(String message)
	{
		terminalWebSocketServer.broadcast(message);
	}

	public Queue<String> getMessagesQueue()
	{
		return terminalWebSocketServer.getMessagesReceivedQueue();
	}

	private int findPort() throws IOException
	{
		ServerSocket serverSocket = new ServerSocket(0);
		int port = serverSocket.getLocalPort();
		serverSocket.close();
		return port;
	}

	public void stopServer() throws Exception
	{
		terminalWebSocketServer.stop(500);
	}

	public static int getServerPort()
	{
		return terminalWebSocketServer.getPort();
	}

	public static boolean needSocketServer(IProject project)
	{
		return Boolean.valueOf(getGdbPanicStubInfo(project)).booleanValue();
	}

	private static String getGdbPanicStubInfo(IProject project)
	{
		return new SDKConfigJsonReader(project).getValue("ESP_SYSTEM_PANIC_GDBSTUB"); //$NON-NLS-1$
	}

}
