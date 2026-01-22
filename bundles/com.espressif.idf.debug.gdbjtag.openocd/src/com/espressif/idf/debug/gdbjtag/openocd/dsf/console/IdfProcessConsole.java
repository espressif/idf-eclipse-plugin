/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.debug.gdbjtag.openocd.dsf.console;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import com.espressif.idf.core.logging.Logger;

/**
 * Idf process console created for customization of the process output to filter and color code
 * 
 * @author Ali Azam Rana
 *
 */
@SuppressWarnings("restriction")
public class IdfProcessConsole extends IOConsole implements IPropertyChangeListener
{
	public static final String CONSOLE_NAME = "IDF Process Console"; //$NON-NLS-1$
	public static final String CONSOLE_TYPE = "com.espressif.idf.debug.gdbjtag.openocd.dsf.console.IdfProcessConsole"; //$NON-NLS-1$

	private IOConsoleOutputStream outputStream;
	private IOConsoleOutputStream errorStream;
	private IOConsoleOutputStream warnStream;
	private IOConsoleInputStream inputStream;
	
	private URLPatternMatchListener urlPatternMatchListener;

	public IdfProcessConsole(Charset charset)
	{
		super(CONSOLE_NAME, CONSOLE_TYPE, null, charset, true);
		inputStream = this.getInputStream();
		outputStream = newOutputStream();
		errorStream = newOutputStream();
		warnStream = newOutputStream();
		urlPatternMatchListener = new URLPatternMatchListener(this);
	}

	@Override
	public void init()
	{
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		store.addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		outputStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR));
		errorStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
		if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP))
		{
			setConsoleWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH));
		}
		setTabWidth(store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH));

		if (store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT))
		{
			int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
			int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
			setWaterMarks(lowWater, highWater);
		}

		setHandleControlCharacters(store.getBoolean(IDebugPreferenceConstants.CONSOLE_INTERPRET_CONTROL_CHARACTERS));
		setCarriageReturnAsControlCharacter(
				store.getBoolean(IDebugPreferenceConstants.CONSOLE_INTERPRET_CR_AS_CONTROL_CHARACTER));

		DebugUIPlugin.getStandardDisplay().asyncExec(() -> {
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
			setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
		});
		
		addPatternMatchListener(urlPatternMatchListener);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		String property = evt.getProperty();
		IPreferenceStore store = DebugUIPlugin.getDefault().getPreferenceStore();
		if (property.equals(IDebugPreferenceConstants.CONSOLE_WRAP)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_WIDTH))
		{
			boolean fixedWidth = store.getBoolean(IDebugPreferenceConstants.CONSOLE_WRAP);
			if (fixedWidth)
			{
				int width = store.getInt(IDebugPreferenceConstants.CONSOLE_WIDTH);
				setConsoleWidth(width);
			}
			else
			{
				setConsoleWidth(-1);
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK)
				|| property.equals(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK))
		{
			boolean limitBufferSize = store.getBoolean(IDebugPreferenceConstants.CONSOLE_LIMIT_CONSOLE_OUTPUT);
			if (limitBufferSize)
			{
				int highWater = store.getInt(IDebugPreferenceConstants.CONSOLE_HIGH_WATER_MARK);
				int lowWater = store.getInt(IDebugPreferenceConstants.CONSOLE_LOW_WATER_MARK);
				if (highWater > lowWater)
				{
					setWaterMarks(lowWater, highWater);
				}
			}
			else
			{
				setWaterMarks(-1, -1);
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH))
		{
			int tabWidth = store.getInt(IDebugPreferenceConstants.CONSOLE_TAB_WIDTH);
			setTabWidth(tabWidth);
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT))
		{
			boolean activateOnOut = store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_OUT);
			if (outputStream != null)
			{
				outputStream.setActivateOnWrite(activateOnOut);
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR))
		{
			boolean activateOnErr = store.getBoolean(IDebugPreferenceConstants.CONSOLE_OPEN_ON_ERR);
			if (errorStream != null)
			{
				errorStream.setActivateOnWrite(activateOnErr);
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR))
		{
			if (outputStream != null)
			{
				outputStream
						.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR));
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR))
		{
			if (errorStream != null)
			{
				errorStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
			}
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR))
		{
			if (inputStream != null && inputStream instanceof IOConsoleInputStream)
			{
				inputStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR));
			}
		}
		else if (property.equals(IDebugUIConstants.PREF_CONSOLE_FONT))
		{
			setFont(JFaceResources.getFont(IDebugUIConstants.PREF_CONSOLE_FONT));
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR))
		{
			setBackground(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_BAKGROUND_COLOR));
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_INTERPRET_CONTROL_CHARACTERS))
		{
			setHandleControlCharacters(
					store.getBoolean(IDebugPreferenceConstants.CONSOLE_INTERPRET_CONTROL_CHARACTERS));
		}
		else if (property.equals(IDebugPreferenceConstants.CONSOLE_INTERPRET_CR_AS_CONTROL_CHARACTER))
		{
			setCarriageReturnAsControlCharacter(
					store.getBoolean(IDebugPreferenceConstants.CONSOLE_INTERPRET_CR_AS_CONTROL_CHARACTER));
		}
	}

	public IOConsoleOutputStream getOutputStream()
	{
		return outputStream;
	}

	public IOConsoleOutputStream getErrorStream()
	{
		return errorStream;
	}

	public IOConsoleOutputStream getWarnStream()
	{
		return warnStream;
	}

	private class URLPatternMatchListener implements IPatternMatchListener
	{
		private IdfProcessConsole idfProcessConsole;
		private Pattern urlPattern;

		public URLPatternMatchListener(IdfProcessConsole idfProcessConsole)
		{
			this.idfProcessConsole = idfProcessConsole;
			urlPattern = Pattern.compile("http[s]?://\\S+");
		}

		@Override
		public String getPattern()
		{
			return urlPattern.pattern();
		}

		@Override
		public void matchFound(PatternMatchEvent event)
		{
			try
			{
				int offset = event.getOffset();
				int length = event.getLength();
				String detectedUrl = idfProcessConsole.getDocument().get(offset, length);
				IHyperlink link = new IHyperlink()
				{
					@Override
					public void linkActivated()
					{
						try
						{
							Program.launch(detectedUrl);
						}
						catch (Exception e)
						{
							Logger.log(e);
						}
					}

					@Override
					public void linkExited()
					{
					}

					@Override
					public void linkEntered()
					{
					}
				};
				idfProcessConsole.addHyperlink(link, offset, length);
			}
			catch (Exception e)
			{
				Logger.log(e);
			}

		}

		@Override
		public void connect(TextConsole console)
		{
			
		}

		@Override
		public void disconnect()
		{
			
		}

		@Override
		public int getCompilerFlags()
		{
			return 0;
		}

		@Override
		public String getLineQualifier()
		{
			return null;
		}
	}
}
