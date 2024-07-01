/*******************************************************************************
 * Copyright (c) 2017 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial version
 *     Espressif Systems Ltd — Kondal Kolipaka <kondal.kolipaka@espressif.com>

 *******************************************************************************/
package com.espressif.idf.launch.serial.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.launch.serial.ui.internal.messages"; //$NON-NLS-1$
	public static String NewSerialFlashTargetWizard_Title;
	public static String NewSerialFlashTargetWizardPage_Description;
	public static String NewSerialFlashTargetWizardPage_Fetching;
	public static String NewSerialFlashTargetWizardPage_IDFTarget;
	public static String NewSerialFlashTargetWizardPage_IDFTargetToolTipMsg;
	public static String NewSerialFlashTargetWizardPage_Name;
	public static String NewSerialFlashTargetWizardPage_SerialPort;
	public static String NewSerialFlashTargetWizardPage_Title;
	public static String CMakeMainTab2_FlashComboLbl;
	public static String CMakeMainTab2_Arguments;
	public static String CMakeMainTab2_NoDfuTargetSelectedError;
	public static String CMakeMainTab2_Note;
	public static String CMakeMainTab2_TargetsComboLbl;
	public static String CMakeMainTab2_Variables;
	public static String CMakeMainTab2_WarningDfuMsg;
	public static String flashVoltageLabel;
	public static String flashVoltageToolTip;
	public static String configTargetLabel;
	public static String configTargetToolTip;
	public static String configBoardLabel;
	public static String configBoardTooTip;
	public static String CMakeMainTab2_OpeonOcdSetupGroupTitle;
	public static String CMakeMainTab2_JtagFlashingNotSupportedMsg;
	public static String CMakeMainTab2_SettingTargetJob;
	public static String IDFLaunchTargetNotFoundMsg1;
	public static String IDFLaunchTargetNotFoundMsg2;
	public static String IDFLaunchTargetNotFoundIDFLaunchTargetNotFoundTitle;
	public static String IDFLaunchTargetNotFoundMsg3;
	public static String CMakeMainTab2_SerialMonitorBtn;

	public static String TargetPortUpdatingMessage;
	public static String TargetPortInformationMessage;
	public static String TargetPortFoundMessage;
	public static String TargetPortNotFoundMessage;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
