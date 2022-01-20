package com.espressif.idf.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;

public class IDFVersionReaderTest
{

	private IDFVersionsReader reader;

	public IDFVersionReaderTest()
	{
		reader = new IDFVersionsReader();
	}

	@Test
	public void canGetVersionsFromDLEspressif()
	{

		List<String> versions = reader.getVersions();
		assertTrue(versions.size() > 0);
	}

	@Test
	public void canItFilterVersionsCorrectly()
	{
		List<String> versions = getVersions();

		// should return versions > 4.0 and higher
		List<String> filterList = reader.applyPluginFilter(versions);
		assertTrue(filterList.size() == 2); // master and v4.3.2
	}

	@Test
	public void canItDiscardversion335()
	{
		List<String> versions = getVersions();

		// should return versions > 4.0 and above
		List<String> filterList = reader.applyPluginFilter(versions);
		assertFalse(filterList.contains("v3.3.5"));
	}

	private List<String> getVersions()
	{
		List<String> versions = new ArrayList<>();
		versions.add("v4.3.2");
		versions.add("v3.3.5");
		versions.add("master");
		return versions;
	}

	@Test
	public void canGetVersionsMap()
	{
		Map<String, IDFVersion> versionsMap = reader.getVersionsMap();
		assertTrue(versionsMap.size() > 0);
	}

}
