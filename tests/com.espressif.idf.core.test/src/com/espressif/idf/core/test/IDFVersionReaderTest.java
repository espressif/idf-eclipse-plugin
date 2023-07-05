/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import com.espressif.idf.core.IDFVersion;
import com.espressif.idf.core.IDFVersionsReader;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
@TestInstance(Lifecycle.PER_CLASS)
public class IDFVersionReaderTest
{

	private IDFVersionsReader reader;

	@BeforeAll
	public void setup()
	{
		reader = new IDFVersionsReader();
	}

	@AfterAll
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
