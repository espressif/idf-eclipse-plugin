/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.terminal.connector.serial.server;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.terminal.connector.serial.connector.SerialConnector;
import com.espressif.idf.terminal.connector.serial.launcher.CoreDumpPostmortemDebuggerLauncher;
import com.espressif.idf.terminal.connector.serial.launcher.GDBStubDebuggerLauncher;
import com.espressif.idf.terminal.connector.serial.launcher.ISerialWebSocketEventLauncher;
import com.espressif.idf.terminal.connector.serial.nls.Messages;

/**
 * Class to handle the socket server messages and create the required objects to process those messages
 *
 * @author Ali Azam Rana
 *
 */
public class SocketServerMessageHandler
{
	private SerialConnector serialConnector;
	private IProject project;

	public SocketServerMessageHandler(SerialConnector serialConnector, IProject project)
	{
		this.project = project;
		this.serialConnector = serialConnector;
	}

	public void parseMessage(String message) throws Exception
	{
		JSONObject messageJsonObject = (JSONObject) new JSONParser().parse(message);
		String event = messageJsonObject.get("event").toString(); //$NON-NLS-1$
		ISerialWebSocketEventLauncher iSerialWebSocketEventLauncher = null;
		Logger.log("Event Received on Socket Server: ".concat(event)); //$NON-NLS-1$
		MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(),
				SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
		messageBox.setText(Messages.MessageBox_SocketServerEventTitle);
		messageBox.setMessage(String.format(Messages.MessageBox_SocketServerEventMessage, event));
		int messageBoxResponse = -1;

		if (ITerminalSocketEvents.GDB_STUB.equals(event)) // $NON-NLS-1$
		{
			iSerialWebSocketEventLauncher = new GDBStubDebuggerLauncher(message, project);
			messageBoxResponse = messageBox.open();
		}
		else if (ITerminalSocketEvents.CORE_DUMP.equals(event))
		{
			iSerialWebSocketEventLauncher = new CoreDumpPostmortemDebuggerLauncher(message, project);
			messageBoxResponse = messageBox.open();
		}

		if (iSerialWebSocketEventLauncher != null && messageBoxResponse == SWT.YES)
		{
			iSerialWebSocketEventLauncher.launchDebugSession();
			serialConnector.disconnect();
			SocketServerHandler.getInstance().broadcastMessageToClients("{\"event\" : \"debug_finished\"}"); //$NON-NLS-1$
		}
		else
		{
			Logger.log("Event capture not yet implemented"); //$NON-NLS-1$
		}
	}

}
