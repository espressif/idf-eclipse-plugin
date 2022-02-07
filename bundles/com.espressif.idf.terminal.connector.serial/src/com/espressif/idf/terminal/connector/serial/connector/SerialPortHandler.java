package com.espressif.idf.terminal.connector.serial.connector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.resources.IProject;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;

import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.util.StringUtil;
import com.espressif.idf.serial.monitor.handlers.SerialMonitorHandler;
import com.espressif.idf.serial.monitor.server.SocketServerHandler;
import com.espressif.idf.terminal.connector.serial.activator.Activator;
import com.espressif.idf.terminal.connector.serial.launcher.GDBStubDebuggerLauncher;

public class SerialPortHandler
{

	private final String portName;
	private boolean isOpen;
	private boolean isPaused;
	private Object pauseMutex = new Object();
	private IProject project;

	private static final Map<String, LinkedList<WeakReference<SerialPortHandler>>> openPorts = new HashMap<>();

	private Process process;
	private Thread thread;
	private SerialConnector serialConnector;
	private String socketServerMessage;

	private Thread socketServerThread;
	private SocketServerHandler socketServerHandler;

	private static String adjustPortName(String portName)
	{
		if (System.getProperty("os.name").startsWith("Windows") && !portName.startsWith("\\\\.\\")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			return "\\\\.\\" + portName; //$NON-NLS-1$
		}
		else
		{
			return portName;
		}
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
		this.project = project;
	}

	public String getPortName()
	{
		return portName;
	}

	private void handleSocketServerMessage(String message)
	{
		if (StringUtil.isEmpty(message))
		{
			return;
		}

		Logger.log("GDB Stub Event Received on Socket Server"); //$NON-NLS-1$
		GDBStubDebuggerLauncher gdbStubDebuggerLauncher = new GDBStubDebuggerLauncher(message, project);
		try
		{
			gdbStubDebuggerLauncher.launchDebugSession();
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
	}

	private void startSocketServerThread()
	{
		socketServerHandler = new SocketServerHandler();
		CountDownLatch latch = new CountDownLatch(1);
		socketServerThread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					socketServerHandler.startServer();
					latch.countDown();
					Queue<String> messagesQueue = socketServerHandler.getMessagesQueue();

					while (messagesQueue.isEmpty())
					{
						sleep(250);
					}

					String message = messagesQueue.poll();
					if (message.contains("\"event\": \"gdb_stub\"")) //$NON-NLS-1$
					{
						socketServerHandler.broadcastMessageToClients("{\"event\" : \"debug_finished\"}"); //$NON-NLS-1$
						socketServerMessage = message;
						serialConnector.disconnect();
					}
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
			}
		};

		socketServerThread.start();
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			Logger.log(e);
		}
	}

	public synchronized void open()
	{

		// set state
		serialConnector.control.setState(TerminalState.CONNECTING);
		boolean withSocketServer = false;
		if (SocketServerHandler.needSocketServer(project))
		{
			startSocketServerThread();
			withSocketServer = true;
		}
		// Hook IDF Monitor with the CDT serial monitor
		SerialMonitorHandler serialMonitorHandler = new SerialMonitorHandler(serialConnector.project, portName,
				serialConnector.filterOptions);

		process = serialMonitorHandler.invokeIDFMonitor(withSocketServer);
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
				catch (IOException e)
				{
					Activator.log(e);
					serialConnector.control.setState(TerminalState.CLOSED);
				}
			}
		};

		thread.start();

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
				process.destroy();
			}
			if (thread != null)
			{
				thread.interrupt();
			}
			if (socketServerThread != null)
			{
				socketServerThread.interrupt();
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

			if (SocketServerHandler.needSocketServer(project))
			{
				handleSocketServerMessage(socketServerMessage);
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

}
