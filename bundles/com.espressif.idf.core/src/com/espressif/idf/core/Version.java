package com.espressif.idf.core;

public class Version implements Comparable<Version>
{

	private String version;

	public final String get()
	{
		return this.version;
	}

	public Version(String version)
	{
		if (version == null)
			throw new IllegalArgumentException(Messages.Version_VersionNotNull);
		if (!version.matches("[0-9]+(\\.[0-9]+)*")) //$NON-NLS-1$
			throw new IllegalArgumentException(Messages.Version_InvalidVersion);
		this.version = version;
	}

	@Override
	public int compareTo(Version that)
	{
		if (that == null)
			return 1;
		String[] thisParts = this.get().split("\\."); //$NON-NLS-1$
		String[] thatParts = that.get().split("\\."); //$NON-NLS-1$
		int length = Math.max(thisParts.length, thatParts.length);
		for (int i = 0; i < length; i++)
		{
			int thisPart = i < thisParts.length ? Integer.parseInt(thisParts[i]) : 0;
			int thatPart = i < thatParts.length ? Integer.parseInt(thatParts[i]) : 0;
			if (thisPart < thatPart)
				return -1;
			if (thisPart > thatPart)
				return 1;
		}
		return 0;
	}

	@Override
	public boolean equals(Object that)
	{
		if (this == that)
			return true;
		if (that == null)
			return false;
		if (this.getClass() != that.getClass())
			return false;
		return this.compareTo((Version) that) == 0;
	}

}
