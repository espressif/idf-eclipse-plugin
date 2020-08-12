/*******************************************************************************
 * Copyright (c) 2017 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Liviu Ionescu - initial version
 *******************************************************************************/

package ilg.gnumcueclipse.debug.gdbjtag.openocd;

import java.util.regex.Matcher;

import org.eclipse.core.resources.IProject;

import ilg.gnumcueclipse.core.EclipseUtils;
import ilg.gnumcueclipse.debug.gdbjtag.openocd.preferences.PersistentPreferences;

// Resolves variables from persistent preferences.

public class DynamicVariableResolver {

	public static final String VARIABLE_EXECUTABLE = "openocd_executable";
	public static final String VARIABLE_PATH = "openocd_path";

	static String macros[] = { VARIABLE_EXECUTABLE, VARIABLE_PATH };
	static String preferences[] = { PersistentPreferences.EXECUTABLE_NAME, PersistentPreferences.INSTALL_FOLDER };
	static String defaults[] = { "none", "/undefined_path" };

	public static String resolveAll(String input, IProject project) {

		String output = input;
		if (input.indexOf("${") >= 0) {
			for (int i = 0; i < macros.length; i++) {
				if (input.indexOf("${" + macros[i] + "}") >= 0) {
					String tmp = EclipseUtils.getPreferenceValueForId(Activator.PLUGIN_ID, preferences[i], defaults[i],
							project);

					// The replacer gives a special meaning to '\' and '$'; to
					// prevent this
					// the string must be quoted.
					tmp = Matcher.quoteReplacement(tmp);
					output = output.replaceAll("[$][{]" + macros[i] + "[}]", tmp);
				}
			}
		}
		return output;
	}

}
