/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.server;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.espressif.idf.core.logging.Logger;

/**
 * Websocket server class to start the server on a specific port for reading the gdb events.
 * 
 * @author Ali Azam Rana
 *
 */
public class TerminalWebSocketServer extends WebSocketServer
{
	private Queue<String> messagesReceivedQueue;

	public TerminalWebSocketServer(int port)
	{
		super(new InetSocketAddress(port));
		messagesReceivedQueue = new ConcurrentLinkedQueue<String>();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		broadcast(conn + " is terminated!"); //$NON-NLS-1$
		Logger.log(conn + " is terminated!"); //$NON-NLS-1$
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		Logger.log(ex);
	}

	@Override
	public void onMessage(WebSocket connection, String message)
	{
		Logger.log("message received:"); //$NON-NLS-1$
		Logger.log(message);
		messagesReceivedQueue.add(message);
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake clientHandshake)
	{
		Logger.log("Connection Made to: "); //$NON-NLS-1$
		Logger.log(conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	@Override
	public void onStart()
	{
		Logger.log("Server started!"); //$NON-NLS-1$
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	public Queue<String> getMessagesReceivedQueue()
	{
		return messagesReceivedQueue;
	}

	public void setMessagesReceivedQueue(Queue<String> messagesReceivedQueue)
	{
		this.messagesReceivedQueue = messagesReceivedQueue;
	}
}
