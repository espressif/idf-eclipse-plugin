/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;

/**
 * Skips ESP-IDF Kconfig {@code NOTE:} lines so CDT's GCC parser does not treat them as compiler diagnostics.
 * <p>
 * Kconfig prints notes such as {@code NOTE: C:/.../Kconfig:150: ...}. The GCC parser takes the text before
 * {@code :line:} as a file name. On Windows that text is an opaque URI, and CDT throws a
 * {@code NullPointerException} while extracting its name. Consuming the line keeps it in the build console and stops
 * later parsers from seeing it.
 */
public class IdfKconfigNoteParser implements IErrorParser
{
	public static final String ID = "com.espressif.idf.core.IdfKconfigNoteParser"; //$NON-NLS-1$

	private static final String NOTE_PREFIX = "NOTE: "; //$NON-NLS-1$

	/**
	 * Places this parser ahead of the toolchain parsers so it sees each line first.
	 *
	 * @param parserIds toolchain error parser ids, possibly already containing this parser
	 * @return parser ids with this parser first and not duplicated
	 */
	public static String[] prepend(String[] parserIds)
	{
		List<String> orderedIds = new ArrayList<>();
		orderedIds.add(ID);
		if (parserIds != null)
		{
			for (String parserId : parserIds)
			{
				if (parserId != null && !ID.equals(parserId))
				{
					orderedIds.add(parserId);
				}
			}
		}
		return orderedIds.toArray(String[]::new);
	}

	@Override
	public boolean processLine(String line, ErrorParserManager eoParser)
	{
		return line != null && line.stripLeading().startsWith(NOTE_PREFIX);
	}
}
