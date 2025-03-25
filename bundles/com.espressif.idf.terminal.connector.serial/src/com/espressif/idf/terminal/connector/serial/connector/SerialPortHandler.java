package com.espressif.idf.terminal.connector.serial.connector;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	private static final Map<String, LinkedList<SerialPortHandler>> openPorts = new ConcurrentHashMap<>();

	private Process process;
	private Thread thread;
	private SerialConnector serialConnector;
	private SocketServerMessageHandler serverMessageHandler;

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
		LinkedList<SerialPortHandler> list = openPorts.get(portName);
		if (list == null)
		{
			return null;
		}

		synchronized (list)
		{
			Iterator<SerialPortHandler> i = list.iterator();
			while (i.hasNext())
			{
				SerialPortHandler port = i.next();
				if (port == null)
				{
					i.remove();
				}
				else
				{
					return port;
				}
			}
		}
		return null;
	}

	public SerialPortHandler(String portName, SerialConnector serialConnector, IProject project)
	{
		this.portName = portName;
		this.serialConnector = serialConnector;
		this.serverMessageHandler = new SocketServerMessageHandler(serialConnector, project);
	}

	public String getPortName()
	{
		return portName;
	}

	private int startSocketServerThread() throws Exception
	{
		SocketServerHandler socketServerHandler;
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
				serialConnector.filterOptions, serverPort);
		process = serialMonitorHandler.invokeIDFMonitor();
		serialConnector.process = process;
		thread = new Thread(() -> {
			try (InputStream targetIn = process.getInputStream())
			{
				byte[] buff = new byte[256];
				int n;
				while ((n = targetIn.read(buff)) >= 0)
				{
					if (n != 0)
					{
						serialConnector.control.getRemoteToTerminalOutputStream().write(buff, 0, n);
					}
				}
			}
			catch (Exception e)
			{
				Activator.log(e);
			} finally
			{
				serialConnector.disconnect();
				serialConnector.process.destroy();
				serialConnector.control.setState(TerminalState.CLOSED);
			}
		});

		thread.start();
		if (!serverMessageHandler.isAlive())
		{
			serverMessageHandler.start();
		}

		isOpen = true;

		serialConnector.control.setState(TerminalState.CONNECTED);
		openPorts.computeIfAbsent(portName, k -> new LinkedList<>()).addFirst(this);
	}

	public synchronized void close()
	{
		if (isOpen)
		{
			isOpen = false;

			// kill the port process and thread
			if (process != null)
			{
				process.destroy();
			}
			if (thread != null)
			{
				thread.interrupt();
			}

			openPorts.computeIfPresent(portName, (k, list) -> {
				list.remove(this);
				return list.isEmpty() ? null : list;
			});

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

	public void pause()
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

	public void resume()
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

}
