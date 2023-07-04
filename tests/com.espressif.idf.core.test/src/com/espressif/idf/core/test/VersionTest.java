package com.espressif.idf.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.espressif.idf.core.Version;

public class VersionTest
{

	@Test
	@DisplayName("Get version should return the correct version string")
	void get_version_returns_correct_version_string()
	{
		String versionString = "1.2.3";
		Version version = new Version(versionString);
		Assertions.assertEquals(versionString, version.get());
	}

	@Test
	@DisplayName("Creating version with null value should throw an IllegalArgumentException")
	void create_version_with_null_throws_IllegalArgumentException()
	{
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(null));
	}

	@Test
	@DisplayName("Creating version with an invalid format should throw an IllegalArgumentException")
	void create_version_with_invalid_format_throws_IllegalArgumentException()
	{
		String invalidVersion = "1.2.a";
		Assertions.assertThrows(IllegalArgumentException.class, () -> new Version(invalidVersion));
	}

	@ParameterizedTest
	@DisplayName("Comparing version 1.2.3 to higher versions should return negative")
	@ValueSource(strings = { "1.2.4", "1.2.4", "1.3", "1.2.3.4" })
	void comparing_to_higher_versions_returns_negative(String version)
	{
		Version versionFromSource = new Version(version);
		Version comparedVersion = new Version("1.2.3");

		Assertions.assertTrue(comparedVersion.compareTo(versionFromSource) < 0);
	}

	@ParameterizedTest
	@DisplayName("Comparing version 1.2.3 to lower versions should return positive")
	@ValueSource(strings = { "1.2.2", "1.2.1", "1.2", "1.2.1.1", "1.1" })
	void comparing_to_lower_versions_returns_positive(String version)
	{
		Version versionFromSource = new Version(version);
		Version comparedVersion = new Version("1.2.3");

		Assertions.assertTrue(comparedVersion.compareTo(versionFromSource) > 0);
	}

	@Test
	@DisplayName("Comparing same versions should return 0")
	void comparing_same_versions_ruturns_zero()
	{
		Version version = new Version("1.2.3");
		Version comparedVersion = new Version("1.2.3");

		Assertions.assertTrue(comparedVersion.compareTo(version) == 0);
	}

	@Test
	@DisplayName("Comparing same versions for equality should return true")
	void comparing_same_version_with_equals_returns_true()
	{
		Version version = new Version("1.2.3");
		Version comparedVersion = new Version("1.2.3");

		Assertions.assertTrue(comparedVersion.equals(version));
	}

	@Test
	@DisplayName("Comparing different versions for equality should return false")
	void comparing_different_version_with_equals_returns_false()
	{
		Version version = new Version("1.2.3");
		Version comparedVersion = new Version("1.2.4");

		Assertions.assertFalse(comparedVersion.equals(version));
	}

	@Test
	@DisplayName("Generating hash code for versions should return the same hash code for equal versions")
	void generate_hashCode_returns_same_hashCode_for_equal_versions()
	{
		Version version1 = new Version("1.2.3");
		Version version2 = new Version("1.2.3");

		Assertions.assertEquals(version1.hashCode(), version2.hashCode());
	}

	@ParameterizedTest
	@DisplayName("Comparing version 1.2 to higher version with different numbers of segments ruturns negative")
	@ValueSource(strings = { "1.2.3", "1.2.3.4", "1.5.1" })
	void compare_to_higher_versions_with_different_numbers_of_segments_returns_negative(String version)
	{
		Version versionFromSource = new Version(version);
		Version comparedVersion = new Version("1.2");

		Assertions.assertTrue(comparedVersion.compareTo(versionFromSource) < 0);
	}

	@ParameterizedTest
	@DisplayName("Comparing version 1.2 to lower version with different numbers of segments ruturns positive")
	@ValueSource(strings = { "1.1.4", "1.1", "1.0.5" })
	void compare_to_lower_versions_with_different_numbers_of_segments_returns_positive(String version)
	{
		Version versionFromSource = new Version(version);
		Version comparedVersion = new Version("1.2");

		Assertions.assertTrue(comparedVersion.compareTo(versionFromSource) > 0);
	}

	@Test
	@DisplayName("Comparing null version should return a positive result")
	void compare_to_null_returns_positive()
	{
		Version version1 = new Version("1.2.3");
		Version nullVersion = null;

		Assertions.assertTrue(version1.compareTo(nullVersion) > 0);
	}

	@Test
	@DisplayName("Comparing version with itself should return zero")
	void compare_to_same_version_returns_zero()
	{
		Version version = new Version("1.2.3");

		Assertions.assertTrue(version.compareTo(version) == 0);
	}
	
	@Test
    @DisplayName("Equals method should return false when comparing with null")
	void equals_compared_with_null_returns_false()
	{
		Version version = new Version("1.2.3");

		Assertions.assertFalse(version.equals(null));
    }

	@Test
	@DisplayName("Equals method should return false when comparing with a different object type")
	void equals_coompared_with_different_type_returns_false()
	{
		Version version = new Version("1.2.3");
		String differentType = "1.2.3";

		Assertions.assertFalse(version.equals(differentType));
	}

	@Test
	@DisplayName("Equals method should return true when comparing object to itself")
	void equals_compared_with_itself_returns_true()
	{
		Version version = new Version("1.2.3");

		Assertions.assertTrue(version.equals(version));
	}

	@Test
	@DisplayName("Hash code should be consistent for the same version object")
	void hashCode_same_version_returns_consistent_hash_code()
	{
		Version version = new Version("1.2.3");

		Assertions.assertEquals(version.hashCode(), version.hashCode());
	}

	@Test
	@DisplayName("Hash code should be different for different version objects")
	void hashCode_different_versions_returns_different_hash_code()
	{
		Version version1 = new Version("1.2.3");
		Version version2 = new Version("1.2.4");

		Assertions.assertNotEquals(version1.hashCode(), version2.hashCode());
	}

}
