package com.espressif.idf.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.ProcessBuilderFactory;

class ProcessBuilderFactoryTest
{
	@TempDir
	private static File tempDir;
	private static File fakeExecutableFile;

	@BeforeAll
	public static void setUp() throws IOException
	{
		fakeExecutableFile = new File(tempDir, "fake");
		fakeExecutableFile.createNewFile();
		fakeExecutableFile.setExecutable(true);
	}

	@Test
	void testRunInBackgroundShoudRetutnOkStatusWithOnlyFakeExecutable() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());

		IStatus status = new ProcessBuilderFactory().runInBackground(command, null, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunInBackgroundShouldReturnOkStatusWithAllCorrectArguments() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		IPath workingDirPath = Path.fromOSString(tempDir.getAbsolutePath());

		IStatus status = new ProcessBuilderFactory().runInBackground(command, workingDirPath, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunShouldThrowExceptionWithIncorrectWorkingDir() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		Map<String, String> emptyEnvironment = new HashMap<>();

		assertThrows(IOException.class, () -> {
			new ProcessBuilderFactory().run(command, Path.EMPTY, emptyEnvironment);
		});
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithEmptyEnviromentMap() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		IPath workingDirPath = Path.fromOSString(tempDir.getAbsolutePath());
		Map<String, String> emptyEnvironment = new HashMap<>();

		Process process = new ProcessBuilderFactory().run(command, workingDirPath, emptyEnvironment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithNullEnviromentMap() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		IPath workingDirPath = Path.fromOSString(tempDir.getAbsolutePath());

		Process process = new ProcessBuilderFactory().run(command, workingDirPath, null);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithNullWorkingDir() throws IOException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		Map<String, String> environment = new HashMap<>(System.getenv());

		Process process = new ProcessBuilderFactory().run(command, null, environment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableAndWithAllArguments() throws IOException, InterruptedException
	{
		List<String> command = new ArrayList<>();
		command.add(fakeExecutableFile.getPath());
		Map<String, String> environment = new HashMap<>(System.getenv());
		IPath workingDirPath = Path.fromOSString(tempDir.getAbsolutePath());


		Process process = new ProcessBuilderFactory().run(command, workingDirPath, environment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldThrowExceptionWithNonExistingFileCommand() throws IOException, InterruptedException
	{
		List<String> command = new ArrayList<>();
		command.add("NotExistingFile");
		Map<String, String> environment = new HashMap<>(System.getenv());

		assertThrows(IOException.class, () -> {
			new ProcessBuilderFactory().run(command, Path.EMPTY, environment);
		});
	}

}
