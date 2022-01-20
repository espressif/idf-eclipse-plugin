package com.espressif.idf.core.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;

public class IDFVersionReaderTest
{

	private IDFVersionsReader reader;

	@Before
	public void setup()
	{
		reader = new IDFVersionsReader();
	}

	@After
	public void teardown()
	{
		reader = null;
	}

	@Test
	public void testCanGetVersionsFromDLEspressif()
	{

		List<String> versions = reader.getVersions();
		assertTrue(versions.size() > 0);
	}

	@Test
	public void testCanGetVersionsMap()
	{
		Map<String, IDFVersion> versionsMap = reader.getVersionsMap();
		assertTrue(versionsMap.size() > 0);
	}

	@Test
	public void testIsVersionsFilterWorkingCorrectly()
	{
		List<String> versions = getVersions();

		// should return versions > 4.0 and higher
		List<String> filterList = reader.applyPluginFilter(versions);
		assertTrue(filterList.size() == 2); // master and v4.3.2
	}

	@Test
	public void testCanItDiscardversion335()
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

}
