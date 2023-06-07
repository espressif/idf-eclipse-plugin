/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.IDFEnvironmentVariables;
import com.espressif.idf.core.variable.OpenocdDynamicVariable;
import com.espressif.idf.core.variable.OpenocdVariableResolver;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OpenocdVariableResolverTest
{
	private static final String NON_EXISTING_VALUE = "nonExistingValue";
	private static final String OPENOCD_SCRIPTS_EXPECTED = "OPENOCD_SCRIPTS";
	private static final String OPENOCD_EXE_EXPECTED = File.separator + "bin" + File.separator + "openocd";
	private static final String OPENOCD_BIN_PATH_EXPECTED = "openocd" + File.separator + "bin";

	private OpenocdVariableResolver variableResolver;

	@BeforeEach
	void setUp()
	{
		new IDFEnvironmentVariables().addEnvVariable(IDFEnvironmentVariables.OPENOCD_SCRIPTS, OPENOCD_SCRIPTS_EXPECTED);
		variableResolver = new TestableOpenocdVariableResolver();
	}

	@Test
	void resolveValue_on_openocd_scripts_dynamic_variable_returns_openocd_scripts()
	{

		IDynamicVariable dynamicVariable = mock(IDynamicVariable.class);

		when(dynamicVariable.getName()).thenReturn(OpenocdDynamicVariable.OPENOCD_SCRIPTS.getValue());
		String actualResult = variableResolver.resolveValue(dynamicVariable, null);

		assertEquals(OPENOCD_SCRIPTS_EXPECTED, actualResult);
	}

	@Test
	void resolveValue_on_openocd_path_dynamic_variable_returns_openocd_path()
	{
		IDynamicVariable dynamicVariable = mock(IDynamicVariable.class);
		String expectedResult = Paths.get(OPENOCD_BIN_PATH_EXPECTED).getParent().toString();
		when(dynamicVariable.getName()).thenReturn(OpenocdDynamicVariable.OPENOCD_PATH.getValue());

		String actualResult = variableResolver.resolveValue(dynamicVariable, null);

		assertEquals(expectedResult, actualResult);
	}

	@Test
	void resolveValue_on_openocd_exe_dynamic_variable_returns_openocd_exe()
	{
		IDynamicVariable dynamicVariable = mock(IDynamicVariable.class);
		when(dynamicVariable.getName()).thenReturn(OpenocdDynamicVariable.OPENOCD_EXE.getValue());

		String actualResult = variableResolver.resolveValue(dynamicVariable, null);

		assertEquals(OPENOCD_EXE_EXPECTED, actualResult);
	}

	@Test
	void resolveValue_on_non_existing_enum_variable_returns_variable_name()
	{
		IDynamicVariable dynamicVariable = mock(IDynamicVariable.class);
		when(dynamicVariable.getName()).thenReturn(NON_EXISTING_VALUE);

		String actualResult = variableResolver.resolveValue(dynamicVariable, null);

		assertEquals(NON_EXISTING_VALUE, actualResult);

	}

	@Test
	void resolveValue_on_null_throws_exception()
	{
		assertThrows(NullPointerException.class, () -> variableResolver.resolveValue(null, null));
	}

	private static class TestableOpenocdVariableResolver extends OpenocdVariableResolver
	{

		@Override
		protected Path getOpenocdBinPath(ILaunchConfiguration configuration)
		{
			return Paths.get(OPENOCD_BIN_PATH_EXPECTED);
		}

	}
}
