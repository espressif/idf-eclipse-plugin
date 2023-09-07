/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.espressif.idf.core.build.PartitionTableBean;
import com.espressif.idf.core.logging.Logger;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public class PartitionTableDataUtil
{
	public static List<PartitionTableBean> parseCsv(Path csvFile) throws IOException
	{
		try (Reader reader = Files.newBufferedReader(csvFile))
		{
			CsvToBean<PartitionTableBean> csvToBean = null;
			csvToBean = new CsvToBeanBuilder<PartitionTableBean>(reader).withSkipLines(2)
					.withType(PartitionTableBean.class).build();
			List<PartitionTableBean> beansList = csvToBean.parse();
			return beansList;
		}

	}

	public static void saveCsv(IFile csvFile, List<PartitionTableBean> beansToSave)
	{
		File newFile = new File(csvFile.getLocationURI());
		if (!csvFile.exists())
		{
			try
			{
				newFile.createNewFile();
			}
			catch (IOException e)
			{
				Logger.log(e);
			}
		}
		try (Writer writer = new FileWriter(newFile))
		{

			StatefulBeanToCsv<PartitionTableBean> sbc = new StatefulBeanToCsvBuilder<PartitionTableBean>(writer)
					.withQuotechar(Character.MIN_VALUE).withSeparator(CSVWriter.DEFAULT_SEPARATOR).build();

			writer.write("# ESP-IDF Partition Table\n"); //$NON-NLS-1$
			writer.write("# Name,   Type, SubType, Offset,  Size, Flags\n"); //$NON-NLS-1$
			if (beansToSave.isEmpty())
			{
				writeDefaultValues(writer);
			}
			else
			{
				sbc.write(beansToSave);
			}

		}
		catch (IOException e)
		{
			Logger.log(e);
		}
		catch (CsvDataTypeMismatchException e)
		{
			Logger.log(e);
		}
		catch (CsvRequiredFieldEmptyException e)
		{
			Logger.log(e);
		}
	}

	private static void writeDefaultValues(Writer writer) throws IOException
	{
		writer.write("nvs,      data, nvs,     0x9000,  0x6000,\n"); //$NON-NLS-1$
		writer.write("phy_init, data, phy,     0xf000,  0x1000,\n"); //$NON-NLS-1$
		writer.write("factory,  app,  factory, 0x10000, 1M,\n"); //$NON-NLS-1$
	}

	public static String[] getTypeValues()
	{
		String[] typeValues = { "app", "data" }; //$NON-NLS-1$ //$NON-NLS-2$
		return typeValues;
	}

	@SuppressWarnings("nls")
	public static String[] getSubTypeValues(String type)
	{
		String[] matchedSubTypes = null;
		String[] dataSubTypeValues = { "fat", "ota", "phy", "nvs", "nvs_keys", "spiffs" };
		String[] appSubTypeValues = { "factory", "ota_0", "ota_1", "ota_2", "ota_3", "ota_4", "ota_5", "ota_6", "ota_7",
				"ota_8", "ota_9", "ota_10", "ota_11", "ota_12", "ota_13", "ota_14", "ota_15", "test" };
		matchedSubTypes = type.contentEquals("0x00") || type.contentEquals("app") ? appSubTypeValues : matchedSubTypes;
		matchedSubTypes = type.contentEquals("0x01") || type.contentEquals("data") ? dataSubTypeValues
				: matchedSubTypes;
		matchedSubTypes = matchedSubTypes == null ? supportedSubTypeHexRange() : matchedSubTypes;
		return matchedSubTypes;
	}

	private static String[] supportedSubTypeHexRange()
	{
		String[] hexRange = new String[255];
		String incHex = Integer.toHexString(0);
		for (int i = 0; i < hexRange.length; i++)
		{
			incHex = incHex.length() == 1 ? "0" + incHex : incHex; //$NON-NLS-1$
			hexRange[i] = "0x" + incHex.toUpperCase(); //$NON-NLS-1$
			int value = Integer.parseInt(incHex, 16);
			value++;
			incHex = Integer.toHexString(value);
		}
		return hexRange;

	}
}
