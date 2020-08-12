/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
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

package ilg.gnumcueclipse.debug.gdbjtag.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

import ilg.gnumcueclipse.debug.gdbjtag.Activator;

public class Messages extends NLS {

	private static final String MESSAGES = Activator.PLUGIN_ID + ".ui.messages"; //$NON-NLS-1$

	public static String PeripheralsView_NameColumn_text;
	public static String PeripheralsView_AddressColumn_text;
	public static String PeripheralsView_DescriptionColumn_text;

	public static String PeripheralRegister_Msg_Unknown_expression;
	public static String PeripheralRegister_Msg_Not_a_number;

	public static String PeripheralsPreferencePage_description;
	public static String PeripheralsPreferencePage_readOnlyColor_label;
	public static String PeripheralsPreferencePage_writeOnlyColor_label;
	public static String PeripheralsPreferencePage_changedSaturateColor_label;
	public static String PeripheralsPreferencePage_changedMediumColor_label;
	public static String PeripheralsPreferencePage_changedLightColor_label;

	public static String PeripheralsPreferencePage_useFadingBackground_label;

	public static String AddMemoryBlockAction_title;
	public static String AddMemoryBlockAction_noMemoryBlock;
	public static String AddMemoryBlockAction_failed;
	public static String AddMemoryBlockAction_input_invalid;

	public static String SvdPathProperties_intro_label;
	public static String SvdPathProperties_file_label;
	public static String SvdPathProperties_file_button;
	public static String SvdPathProperties_file_tooltip;
	public static String SvdPathProperties_file_dialog;

	public static String TabSvd_group_text;
	public static String TabSvd_label_text;
	public static String TabSvd_label_tooltip;
	public static String TabSvd_button_Browse_text;
	public static String TabSvd_button_Variables_text;

	static {
		// initialise above static strings
		NLS.initializeMessages(MESSAGES, Messages.class);
	}

	private static ResourceBundle RESOURCE_BUNDLE;
	static {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(MESSAGES);
		} catch (MissingResourceException e) {
			Activator.log(e);
		}
	}

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	public static ResourceBundle getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

}
