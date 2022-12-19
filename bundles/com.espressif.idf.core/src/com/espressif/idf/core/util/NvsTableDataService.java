package com.espressif.idf.core.util;

import java.io.IOException;
import java.io.Writer;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.logging.Logger;

public class NvsTableDataService extends AbstractTableDataService<NvsTableBean>
{

	public NvsTableDataService()
	{
		initGenericTypeClass(NvsTableBean.class);
	}

	@Override
	protected void writeDefaultCsvHeader(Writer writer)
	{
		try
		{
			writer.write(new StringBuilder().append("key,type,encoding,value") //$NON-NLS-1$
					.append(StringUtil.LINE_SEPARATOR).toString());
		}
		catch (IOException e)
		{
			Logger.log(e);
		}
	}

	@Override
	protected void writeDefaultValues(Writer writer)
	{
		try
		{
			writer.write(
					new StringBuilder().append("defaultKey,namespace,,").append(StringUtil.LINE_SEPARATOR).toString()); //$NON-NLS-1$
		}
		catch (IOException e)
		{
			Logger.log(e);
		}

	}

	@SuppressWarnings("nls")
	public static String[] getEncodings(String type)
	{
		if (type.contentEquals("namespace") || type.isBlank())
		{
			return new String[] { StringUtil.EMPTY };
		}

		if (type.contentEquals("file"))
		{
			return new String[] { "hex2bin", "base64", "string", "binary" };
		}

		return new String[] { "u8", "i8", "u16", "i16", "u32", "i32", "u64", "i64", "string", "hex2bin", "base64",
				"binary" };
	}

	@SuppressWarnings("nls")
	public static String[] getTypes()
	{
		return new String[] { "file", "data", "namespace" };
	}

	@Override
	protected int getLinesToSkip()
	{
		return 1;
	}

}
