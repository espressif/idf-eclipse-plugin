package com.espressif.idf.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.ZipUtility;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ZipUtilityTest
{
	private static final String TEST_INPUT = "Test, Input!";
	private static final String DIRECTORY = "directory" + File.separator;
	private static final String FILE_ENTRY = DIRECTORY + "file.txt";
	private static final String NESTED_DIRECTORY = DIRECTORY + DIRECTORY;
	private static final String NESTED_FILE_ENTRY = DIRECTORY + FILE_ENTRY;

	@Test
	void decompress_should_extract_files_from_zip_to_output_directory(@TempDir Path tempDir) throws IOException
	{
		File zipFile = createTemporaryZipFile(tempDir);
		File outputDirectory = new File(tempDir.toFile(), "output");

		ZipUtility zipUtility = new ZipUtility();
		boolean result = zipUtility.decompress(zipFile, outputDirectory);
		assertTrue(result);

		File extractedFile = new File(outputDirectory, DIRECTORY);
		assertTrue(extractedFile.exists());
		assertEquals(TEST_INPUT, readContentFromFile(extractedFile.listFiles()[0]));
	}

	@Test
	void decompress_folder_returns_false(@TempDir Path tempDir)
	{
		File outputDirectory = new File(tempDir.toFile(), "output");
		ZipUtility zipUtility = new ZipUtility();
		boolean result = zipUtility.decompress(tempDir.toFile(), outputDirectory);
		assertFalse(result);
	}

	@Test
	void decompress_non_existent_zip_file_returns_false(@TempDir Path tempDir)
	{
		File zipFile = new File(tempDir.toFile(), "nonexistent.zip");
		File outputDirectory = new File(tempDir.toFile(), "output");

		ZipUtility zipUtility = new ZipUtility();

		assertFalse(zipUtility.decompress(zipFile, outputDirectory));
	}

	@Test
	void decompress_non_zip_file_returns_true(@TempDir Path tempDir) throws IOException
	{
		File txtFile = new File(tempDir.toFile(), "test.txt");
		txtFile.createNewFile();
		File outputDirectory = new File(tempDir.toFile(), "output");
		ZipUtility zipUtility = new ZipUtility();

		boolean result = zipUtility.decompress(txtFile, outputDirectory);

		assertTrue(result);
	}

	private String readContentFromFile(File file) throws IOException
	{
		return new String(Files.readAllBytes(file.toPath()));
	}

	@Test
	void decompress_nested_directories_should_extract_nested_files(@TempDir Path tempDir)
			throws IOException
	{
		File zipFile = createNestedZipFile(tempDir);
		File outputDirectory = new File(tempDir.toFile(), "output");
		ZipUtility zipUtility = new ZipUtility();

		boolean result = zipUtility.decompress(zipFile, outputDirectory);

		assertTrue(result);

		File extractedFile = new File(outputDirectory, FILE_ENTRY);
		assertTrue(extractedFile.exists());
		assertEquals(TEST_INPUT, readContentFromFile(extractedFile));

		File extractedNestedFile = new File(outputDirectory, NESTED_FILE_ENTRY);
		assertTrue(extractedNestedFile.exists());
		assertEquals(TEST_INPUT, readContentFromFile(extractedNestedFile));
	}

	private File createTemporaryZipFile(Path tempDir) throws IOException
	{
		File zipFile = new File(tempDir.toFile(), "test.zip");

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile)))
		{
			// Create a directory entry
			ZipEntry directoryEntry = new ZipEntry(DIRECTORY);
			zipOutputStream.putNextEntry(directoryEntry);
			zipOutputStream.closeEntry();

			// Create a file entry within the directory
			ZipEntry fileEntry = new ZipEntry(FILE_ENTRY);
			zipOutputStream.putNextEntry(fileEntry);
			zipOutputStream.write(TEST_INPUT.getBytes());
			zipOutputStream.closeEntry();
		}

		return zipFile;
	}

	private File createNestedZipFile(Path tempDir) throws IOException
	{
		File zipFile = new File(tempDir.toFile(), "nested.zip");

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile)))
		{
			// Create a directory entry
			ZipEntry directoryEntry = new ZipEntry(DIRECTORY);
			zipOutputStream.putNextEntry(directoryEntry);
			zipOutputStream.closeEntry();

			// Create a file entry within the directory
			ZipEntry fileEntry = new ZipEntry(FILE_ENTRY);
			zipOutputStream.putNextEntry(fileEntry);
			zipOutputStream.write(TEST_INPUT.getBytes());
			zipOutputStream.closeEntry();

			// Create the nested directory
			ZipEntry nestedDirectory = new ZipEntry(NESTED_DIRECTORY);
			zipOutputStream.putNextEntry(nestedDirectory);
			zipOutputStream.closeEntry();

			// Create the nested file in the nested directory
			ZipEntry nestedFile = new ZipEntry(NESTED_FILE_ENTRY);
			zipOutputStream.putNextEntry(nestedFile);
			zipOutputStream.write(TEST_INPUT.getBytes());
			zipOutputStream.closeEntry();
		}

		return zipFile;
	}
}

