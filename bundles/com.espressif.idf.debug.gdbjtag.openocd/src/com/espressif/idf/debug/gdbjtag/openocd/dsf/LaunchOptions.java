package com.espressif.idf.debug.gdbjtag.openocd.dsf;

/**
 * Per-launch options passed from the delegate entry point to {@link Launch}.
 * Stored in a {@link ThreadLocal} for the duration of a single launch call.
 */
public final class LaunchOptions
{
	private final boolean openOcdOnly;

	private LaunchOptions(boolean openOcdOnly)
	{
		this.openOcdOnly = openOcdOnly;
	}

	public static LaunchOptions openOcdOnly()
	{
		return new LaunchOptions(true);
	}

	public boolean isOpenOcdOnly()
	{
		return openOcdOnly;
	}
}
