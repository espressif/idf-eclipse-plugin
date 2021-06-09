package com.espressif.idf.util.nls;

import java.io.File;

public class Translator
{
	public static void translator(String dirPath, String native2asciiExePath) throws Exception
	{
		File dir = new File(dirPath);
		File[] listFiles = dir.listFiles();
		for (File file : listFiles)
		{
			if (file.isDirectory())
			{
				translator(file.getAbsolutePath(), native2asciiExePath);
			}
			else if (file.isFile() && file.getName().endsWith(".properties"))
			{
				System.out.println(file.getAbsolutePath());

				// cmd
				String[] cmdArray = new String[3];
				cmdArray[0] = native2asciiExePath;

				// source properties file - original
				cmdArray[1] = file.getAbsolutePath();

				// destination properties - converted
				cmdArray[2] = file.getAbsolutePath();
				System.out.println(cmdArray);

				// run cmd
				Process process = Runtime.getRuntime().exec(cmdArray);
				process.waitFor();
			}

		}
	}

	// Test
	public static void main(String[] args)
	{
		String folder = "/Users/kondal/Downloads/20210511/IEP-EN:CN/";
		String native2asciiExePath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_201.jdk/Contents/Home/bin/native2ascii";
		try
		{
			translator(folder, native2asciiExePath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
