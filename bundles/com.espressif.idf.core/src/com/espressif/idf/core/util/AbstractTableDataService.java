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

import com.espressif.idf.core.build.CsvBean;
import com.espressif.idf.core.logging.Logger;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

public abstract class AbstractTableDataService<T extends CsvBean> implements CsvTableDataService<T>
{
	private Class<T> genericTypeClass;

	protected void initGenericTypeClass(Class<T> typeClass)
	{
		genericTypeClass = typeClass;
	}

	@Override
	public List<T> parseCsv(Path csvFile) throws IOException
	{
		try (Reader reader = Files.newBufferedReader(csvFile))
		{
			CsvToBean<T> csvToBean = null;
			csvToBean = new CsvToBeanBuilder<T>(reader).withSkipLines(getLinesToSkip())
					.withType(genericTypeClass).build();
			return csvToBean.parse();
		}
	}
	
	protected abstract int getLinesToSkip();

	@Override
	public void saveCsv(IFile csvFile, List<T> beansToSave)
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

			StatefulBeanToCsv<T> sbc = new StatefulBeanToCsvBuilder<T>(writer).withQuotechar(Character.MIN_VALUE)
					.withSeparator(ICSVWriter.DEFAULT_SEPARATOR).build();

			writeDefaultCsvHeader(writer);
			if (beansToSave.isEmpty())
			{
				writeDefaultValues(writer);
			}
			else
			{
				sbc.write(beansToSave);
			}

		}
		catch (
				IOException
				| CsvDataTypeMismatchException
				| CsvRequiredFieldEmptyException e)
		{
			Logger.log(e);
		}

	}

	protected abstract void writeDefaultCsvHeader(Writer writer);

	protected abstract void writeDefaultValues(Writer writer);
	
}
