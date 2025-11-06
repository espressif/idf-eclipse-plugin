package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.configparser.EspConfigParser;
import com.espressif.idf.core.configparser.vo.Board;

public class EspConfigParserTest
{

	private File tempJsonFile;

	@BeforeEach
	public void setup() throws IOException
	{
		// Copy test JSON resource to a temp file
		InputStream in = getClass().getClassLoader().getResourceAsStream("esp-config.json"); //$NON-NLS-1$
		assertNotNull(in, "test-esp-config.json not found in resources"); //$NON-NLS-1$

		tempJsonFile = File.createTempFile("esp-config", ".json"); //$NON-NLS-1$ //$NON-NLS-2$
		tempJsonFile.deleteOnExit();

		try (OutputStream out = new FileOutputStream(tempJsonFile))
		{
			in.transferTo(out);
		}
	}

	@Test
	public void testGetTargets()
	{
		EspConfigParser parser = new EspConfigParser(tempJsonFile.getAbsolutePath());

		Set<String> targets = parser.getTargets();

		assertNotNull(targets);
		assertTrue(targets.contains("esp32")); //$NON-NLS-1$
		assertTrue(targets.contains("esp32c3")); //$NON-NLS-1$
		assertEquals(9, targets.size());
	}

	@Test
	public void testGetEspFlashVoltages()
	{
		EspConfigParser parser = new EspConfigParser(tempJsonFile.getAbsolutePath());

		List<String> voltages = parser.getEspFlashVoltages();

		assertNotNull(voltages);
		assertEquals(List.of("default", "3.3", "1.8"), voltages); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Test
	public void testGetBoardsConfigsForEsp32s3()
	{
		EspConfigParser parser = new EspConfigParser(tempJsonFile.getAbsolutePath());

		List<Board> boards = parser.getBoardsForTarget("esp32s3"); //$NON-NLS-1$
		assertEquals(3, boards.size());

		Set<String> names = boards.stream().map(Board::name).collect(Collectors.toSet());
		assertTrue(names.contains("ESP32-S3 chip (via builtin USB-JTAG)")); //$NON-NLS-1$
		assertTrue(names.contains("ESP32-S3 chip (via ESP-PROG)")); //$NON-NLS-1$
		assertTrue(names.contains("ESP32-S3 chip (via ESP-PROG-2)")); //$NON-NLS-1$
	}

	@Test
	public void testHasBoardConfigJson()
	{
		EspConfigParser parser = new EspConfigParser(tempJsonFile.getAbsolutePath());

		assertTrue(parser.hasBoardConfigJson());
	}
}
