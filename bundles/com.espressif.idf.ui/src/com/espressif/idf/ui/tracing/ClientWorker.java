package com.espressif.idf.ui.tracing;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.espressif.idf.core.logging.Logger;

public class ClientWorker implements Runnable
{

	private TclClient client;
	private Text outputTextField;

	ClientWorker(TclClient client, Text outputTextField)
	{
		this.client = client;
		this.outputTextField = outputTextField;
	}
	@Override
	public void run()
	{
		BufferedReader in = client.getInBuffer();
		try
		{
			while (true)
			{
				int firstByte = in.read();
				String line = firstByte == 0x1a ? null : (char) firstByte + in.readLine();
				if (line == null)
				{
					break;
				}
				Display.getDefault().asyncExec(new Runnable()
				{
					@Override
					public void run()
					{
						if (!outputTextField.isDisposed()) {
							outputTextField.append(line + "\n"); //$NON-NLS-1$
						}

					}
				});
			}
		}
		catch (IOException e1)
		{
			Logger.log(e1);
		}

	}

}
