package com.espressif.idf.terminal.connector.serial.connector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.serial.monitor.handlers.SerialMonitorHandler;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.terminal.connector.serial.server.SocketServerHandler;
import com.espressif.idf.terminal.connector.serial.server.SocketServerMessageHandler;

public class SerialPortHandler
{

	private final String portName;
	private boolean isOpen;
	private boolean isPaused;
	private Object pauseMutex = new Object();
	private static final Map<String, LinkedList<WeakReference<SerialPortHandler>>> openPorts = new HashMap<>();

	private Process process;
	private Thread thread;
	private SerialConnector serialConnector;
	private SocketServerMessageHandler serverMessageHandler;

	private SocketServerHandler socketServerHandler;

	private static String adjustPortName(String portName)
	{
		return portName;
	}

	/**
	 * Return an the SerialPortHandler with the given name or null if it hasn't been allocated yet. This would be used
	 * by components that need to pause and resume a serial port.
	 *
	 * @param portName
	 * @return
	 * @since 1.1
	 */
	public static SerialPortHandler get(String portName)
	{
		synchronized (openPorts)
		{
			LinkedList<WeakReference<SerialPortHandler>> list = openPorts.get(adjustPortName(portName));
			if (list == null)
			{
				return null;
			}

			Iterator<WeakReference<SerialPortHandler>> i = list.iterator();
			while (i.hasNext())
			{
				WeakReference<SerialPortHandler> ref = i.next();
				SerialPortHandler port = ref.get();
				if (port == null)
				{
					i.remove();
				}
				else
				{
					return port;
				}
			}

			return null;
		}
	}

	public SerialPortHandler(String portName, SerialConnector serialConnector, IProject project)
	{
		this.portName = adjustPortName(portName);
		this.serialConnector = serialConnector;
		this.serverMessageHandler = new SocketServerMessageHandler(serialConnector, project);
	}

	public String getPortName()
	{
		return portName;
	}

	private int startSocketServerThread() throws Exception
	{
		socketServerHandler = SocketServerHandler.getInstance();
		socketServerHandler.startServer();

		return SocketServerHandler.getServerPort();
	}

	public synchronized void open()
	{

		// set state
		serialConnector.control.setState(TerminalState.CONNECTING);
		int serverPort;
		try
		{
			serverPort = startSocketServerThread();
		}
		catch (Exception e1)
		{
			Logger.log(e1);
			return;
		}

		// Hook IDF Monitor with the CDT serial monitor
		SerialMonitorHandler serialMonitorHandler = new SerialMonitorHandler(serialConnector.project, portName,
				serialConnector.filterOptions, serialConnector.encryptionOption, serverPort);
		process = serialMonitorHandler.invokeIDFMonitor();
		serialConnector.process = process;
		thread = new Thread()
		{
			@Override
			public void run()
			{
				InputStream targetIn = process.getInputStream();
				byte[] buff = new byte[256];
				int n;
				try
				{
					while ((n = targetIn.read(buff, 0, buff.length)) >= 0)
					{
						if (n != 0)
						{
							serialConnector.control.getRemoteToTerminalOutputStream().write(buff, 0, n);
						}
					}

					serialConnector.disconnect();
				}
				catch (Exception e)
				{
					Activator.log(e);
					serialConnector.disconnect();
					stopWebSocketServer();
					process.destroy();
					serialConnector.control.setState(TerminalState.CLOSED);
				}
			}
		};

		thread.start();
		if (!serverMessageHandler.isAlive())
		{
			serverMessageHandler.start();
		}

		isOpen = true;

		serialConnector.control.setState(TerminalState.CONNECTED);

		synchronized (openPorts)
		{
			LinkedList<WeakReference<SerialPortHandler>> list = openPorts.get(portName);
			if (list == null)
			{
				list = new LinkedList<>();
				openPorts.put(portName, list);
			}
			list.addFirst(new WeakReference<>(this));
		}
	}

	public synchronized void close() throws IOException
	{
		if (isOpen)
		{
			isOpen = false;

			// kill the port process and thread
			if (process != null)
			{
				stopWebSocketServer();
				process.destroy();
			}
			if (thread != null)
			{
				thread.interrupt();
			}

			synchronized (openPorts)
			{
				LinkedList<WeakReference<SerialPortHandler>> list = openPorts.get(portName);
				if (list != null)
				{
					Iterator<WeakReference<SerialPortHandler>> i = list.iterator();
					while (i.hasNext())
					{
						WeakReference<SerialPortHandler> ref = i.next();
						SerialPortHandler port = ref.get();
						if (port == null || port.equals(this))
						{
							i.remove();
						}
					}
				}
			}

			try
			{
				// Sleep for a second since some serial ports take a while to actually close
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				// nothing to do
			}
		}
	}

	public boolean isOpen()
	{
		return isOpen;
	}

	public void pause() throws IOException
	{
		if (!isOpen)
		{
			return;
		}
		synchronized (pauseMutex)
		{
			isPaused = true;
			close();
			try
			{
				// Sleep for a second since some serial ports take a while to actually close
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				// nothing to do
			}
		}
	}

	public void resume() throws IOException
	{
		synchronized (pauseMutex)
		{
			if (!isPaused)
			{
				return;
			}
			isPaused = false;
			open();
			isOpen = true;
			pauseMutex.notifyAll();
		}
	}

	private void stopWebSocketServer()
	{
		try
		{
			SocketServerHandler.getInstance().stopServer();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

}
