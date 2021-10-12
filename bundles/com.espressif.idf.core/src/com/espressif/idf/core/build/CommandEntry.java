package com.espressif.idf.core.build;

/**
 * Represents a parsed command entry of a compile_commands.json file.
 * 
 * @author weber
 */
class CommandEntry
{
	private String directory;
	private String command;
	private String file;

	/**
	 * Gets the build directory as a String.
	 */
	public String getDirectory()
	{
		return directory;
	}

	/**
	 * Gets the command-line to compile the source file.
	 */
	public String getCommand()
	{
		return command;
	}

	/**
	 * Gets the source file path as a String.
	 */
	public String getFile()
	{
		return file;
	}

	public void setCommand(String command)
	{
		this.command = command;
	}

	public void setFile(String file)
	{
		this.file = file;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}
}