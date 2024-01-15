package com.espressif.idf.core.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.espressif.idf.core.build.IDFLaunchConstants;
import com.espressif.idf.core.util.LaunchTargetHelper;
import com.espressif.idf.core.util.StringUtil;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class LaunchBarNameUtilTest
{
	private static final String ACTIVE_LAUNCH_TARGET = "ACTIVE_LAUNCH_TARGET";
	private static final String EXPECTED_TARGET_NAME = "TestTarget";
	private static final String NOT_EXISTING_TARGET_NAME = "NotExisting";
	private static final String SERIAL_PORT = "COM1";
	private static final String FAKE_SERIAL_PORT = "FAKE_PORT";
	private ILaunchTargetManager launchTargetManager;
	private ILaunchBarManager launchBarManager;

	@BeforeEach
	void setUp()
	{
		launchTargetManager = Mockito.mock(ILaunchTargetManager.class);
		launchBarManager = Mockito.mock(ILaunchBarManager.class);
	}

	@Test
	void saveTargetName_should_save_provided_target()
	{
		LaunchTargetHelper.saveTargetName(EXPECTED_TARGET_NAME);

		Optional<String> lastTargetName = LaunchTargetHelper.getLastTargetName();

		assertTrue(lastTargetName.isPresent());
		assertEquals(EXPECTED_TARGET_NAME, lastTargetName.get());
	}

	@Test
	void findLaunchTargetByName_should_return_correct_launch_target()
	{
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		Mockito.when(launchTargetManager.getLaunchTargetsOfType(Mockito.anyString()))
				.thenReturn(new ILaunchTarget[] { target });
		Mockito.when(target.getAttribute(Mockito.anyString(), Mockito.anyString())).thenReturn(EXPECTED_TARGET_NAME);

		ILaunchTarget result = LaunchTargetHelper.findLaunchTargetByName(launchTargetManager, EXPECTED_TARGET_NAME);

		assertNotNull(result);
		assertEquals(EXPECTED_TARGET_NAME, result.getAttribute(Mockito.anyString(), Mockito.anyString()));
	}

	@Test
	void findSuitableTargetForSelectedItem_should_return_correct_launch_target() throws CoreException
	{
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		Mockito.when(launchTargetManager.getLaunchTargetsOfType(Mockito.anyString()))
				.thenReturn(new ILaunchTarget[] { target });
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		Mockito.when(launchBarManager.getActiveLaunchTarget()).thenReturn(target);
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY))
				.thenReturn(SERIAL_PORT);

		Optional<ILaunchTarget> result = LaunchTargetHelper.findSuitableTargetForSelectedItem(launchTargetManager,
				launchBarManager, EXPECTED_TARGET_NAME);

		assertTrue(result.isPresent());
		assertEquals(EXPECTED_TARGET_NAME,
				result.get().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY));
		assertEquals(SERIAL_PORT, result.get().getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY));
	}

	@Test
	void findSuitableTargetForSelectedItem_should_return_launch_target_with_correct_port_when_multiple_launch_target_with_same_name_exist()
			throws CoreException
	{
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		ILaunchTarget target2 = Mockito.mock(ILaunchTarget.class);
		Mockito.when(launchTargetManager.getLaunchTargetsOfType(Mockito.anyString()))
				.thenReturn(new ILaunchTarget[] { target, target2 });
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		Mockito.when(target2.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		Mockito.when(launchBarManager.getActiveLaunchTarget()).thenReturn(target);
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY))
				.thenReturn(SERIAL_PORT);
		Mockito.when(target2.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY))
				.thenReturn(FAKE_SERIAL_PORT);

		Optional<ILaunchTarget> result = LaunchTargetHelper.findSuitableTargetForSelectedItem(launchTargetManager,
				launchBarManager, EXPECTED_TARGET_NAME);

		assertTrue(result.isPresent());
		assertEquals(EXPECTED_TARGET_NAME,
				result.get().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY));
		assertEquals(SERIAL_PORT, result.get().getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY));
	}

	@Test
	void findSuitableTargetForSelectedItem_should_return_launch_target_even_if_target_has_incorrect_port()
			throws CoreException
	{
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		ILaunchTarget activeTarget = Mockito.mock(ILaunchTarget.class);

		Mockito.when(launchTargetManager.getLaunchTargetsOfType(Mockito.anyString()))
				.thenReturn(new ILaunchTarget[] { target });
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		Mockito.when(activeTarget.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(ACTIVE_LAUNCH_TARGET);
		Mockito.when(launchBarManager.getActiveLaunchTarget()).thenReturn(activeTarget);
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY))
				.thenReturn(FAKE_SERIAL_PORT);
		Mockito.when(activeTarget.getAttribute(IDFLaunchConstants.ATTR_SERIAL_PORT, StringUtil.EMPTY))
				.thenReturn(SERIAL_PORT);

		Optional<ILaunchTarget> result = LaunchTargetHelper.findSuitableTargetForSelectedItem(launchTargetManager,
				launchBarManager, EXPECTED_TARGET_NAME);

		assertTrue(result.isPresent());
		assertEquals(EXPECTED_TARGET_NAME,
				result.get().getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY));
	}

	@Test
	void findSuitableTargetForSelectedItem_should_return_empty_optional() throws CoreException
	{
		ILaunchTarget target = Mockito.mock(ILaunchTarget.class);
		Mockito.when(launchTargetManager.getLaunchTargetsOfType(Mockito.anyString()))
				.thenReturn(new ILaunchTarget[] { target });
		Mockito.when(target.getAttribute(IDFLaunchConstants.ATTR_IDF_TARGET, StringUtil.EMPTY))
				.thenReturn(EXPECTED_TARGET_NAME);
		Mockito.when(launchBarManager.getActiveLaunchTarget()).thenReturn(target);

		Optional<ILaunchTarget> result = LaunchTargetHelper.findSuitableTargetForSelectedItem(launchTargetManager,
				launchBarManager, NOT_EXISTING_TARGET_NAME);

		assertFalse(result.isPresent());
	}

}
