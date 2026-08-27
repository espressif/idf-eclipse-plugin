/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.bug.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.espressif.idf.core.bug.BugReportGenerator;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class BugReportGeneratorTest
{

	@ParameterizedTest(name = "''{0}'' is collected as ''{1}''")
	@CsvSource({ ".log, ide_error_log.log", ".bak_0.log, ide_error_log.bak_0.log",
			".bak_1.log, ide_error_log.bak_1.log" })
	void test_hidden_ide_log_gets_a_visible_name(String metadataFileName, String expectedReportFileName)
	{
		String reportFileName = BugReportGenerator.getReportLogFileName(metadataFileName);

		Assertions.assertEquals(expectedReportFileName, reportFileName);
	}

	@ParameterizedTest(name = "''{0}'' is collected unchanged")
	@CsvSource({ "version.ini", "workspace.log" })
	void test_already_visible_file_name_is_kept(String metadataFileName)
	{
		String reportFileName = BugReportGenerator.getReportLogFileName(metadataFileName);

		Assertions.assertEquals(metadataFileName, reportFileName);
	}

	@Test
	void test_no_collected_file_stays_hidden()
	{
		Assertions.assertFalse(BugReportGenerator.getReportLogFileName(".log").startsWith("."));
	}
}
