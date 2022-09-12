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
				StringBuilder command = new StringBuilder();
				command.append(native2asciiExePath);
				
				//encoding option
				command.append(" ");
				command.append("-encoding UTF-8");
				
				// source properties file - original
				command.append(" ");
				command.append(file.getAbsolutePath());

				// destination properties - converted
				command.append(" ");
				command.append(file.getAbsolutePath());
				
				System.out.println(command.toString());

				// run cmd
				Process process = Runtime.getRuntime().exec(command.toString());
				process.waitFor();
				process.getOutputStream().close();
			}

		}
	}

	// Test
	public static void main(String[] args)
	{
		String folder = "C:\\Users\\aliaz\\Documents\\idf-eclipse-plugin\\common\\MissingProperties\\zh0907\\idf-eclipse-plugin";
		String native2asciiExePath = "C:\\Program Files\\Java\\jdk1.8.0_291\\bin\\native2ascii.exe";
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
