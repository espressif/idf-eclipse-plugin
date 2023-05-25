package com.espressif.idf.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.ProcessBuilderFactory;

class ProcessBuilderFactoryTest
{
	private final static List<String> cmd = Arrays.asList(new String[] { "cmd", "/c" });
	private final static List<String> bash = Arrays.asList(new String[] { "bash", "-c" });
	private static List<String> dummyCommand;

	private static boolean isWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}

	@BeforeAll
	static void setUp()
	{
		dummyCommand = isWindows() ? cmd : bash;
	}

	@Test
	void testRunInBackgroundShoudRetutnOkStatusWithOnlyFakeExecutable() throws IOException
	{
		IStatus status = new ProcessBuilderFactory().runInBackground(dummyCommand, null, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunInBackgroundShouldReturnOkStatusWithAllCorrectArguments() throws IOException
	{
		IStatus status = new ProcessBuilderFactory().runInBackground(dummyCommand, Path.ROOT, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunShouldThrowExceptionWithIncorrectWorkingDir() throws IOException
	{
		Map<String, String> emptyEnvironment = new HashMap<>();

		assertThrows(IOException.class, () -> {
			new ProcessBuilderFactory().run(dummyCommand, Path.EMPTY, emptyEnvironment);
		});
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithEmptyEnviromentMap() throws IOException
	{
		Map<String, String> emptyEnvironment = new HashMap<>();

		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, emptyEnvironment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithNullEnviromentMap() throws IOException
	{
		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, null);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableWithNullWorkingDir() throws IOException
	{
		Map<String, String> environment = new HashMap<>(System.getenv());

		Process process = new ProcessBuilderFactory().run(dummyCommand, null, environment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithFakeExecutableAndWithAllArguments() throws IOException, InterruptedException
	{
		Map<String, String> environment = new HashMap<>(System.getenv());

		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, environment);

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
