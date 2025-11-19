/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.nvs.dialog;

import java.util.stream.Stream;

/**
 * Represents the columns in the NVS TableViewer. This enum provides a type-safe way to manage column properties,
 * replacing "magic numbers" and string arrays.
 */
public enum NvsColumn
{

	KEY("Key", 100), TYPE("Type", 100), ENCODING("Encoding", 100), VALUE("Value", 150); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private final String displayName;
	private final int defaultWidth;

	NvsColumn(String displayName, int defaultWidth)
	{
		this.displayName = displayName;
		this.defaultWidth = defaultWidth;
	}

	/**
	 * @return The human-readable name for the table header.
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @return The default pixel width for the column.
	 */
	public int getDefaultWidth()
	{
		return defaultWidth;
	}

	/**
	 * @return The integer index (position) of this column.
	 */
	public int getIndex()
	{
		return this.ordinal();
	}

	/**
	 * A static helper to get an array of all display names for JFace APIs like {@code setColumnProperties}.
	 *
	 * @return ["Key", "Type", "Encoding", "Value"]
	 */
	public static String[] getColumnProperties()
	{
		return Stream.of(values()).map(NvsColumn::getDisplayName).toArray(String[]::new);
	}

	/**
	 * A static helper to get the enum constant from its index.
	 *
	 * @param index The column index.
	 * @return The matching NvsColumn (e.g., KEY for index 0).
	 */
	public static NvsColumn fromIndex(int index)
	{
		if (index >= 0 && index < values().length)
		{
			return values()[index];
		}
		// This should ideally not be reachable if validation is correct
		throw new IndexOutOfBoundsException("Invalid column index: " + index); //$NON-NLS-1$
	}
}
