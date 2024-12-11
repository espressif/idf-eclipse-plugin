/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.templates;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.templates.messages"; //$NON-NLS-1$
	public static String BaseWizardSelectionPage_No_Desc;
	public static String TemplateListSelectionPage_SelectTemplate_Desc;
	public static String TemplateListSelectionPage_Template_Wizard_Desc;
	public static String NewProjectWizardPage_Header;
	public static String NewProjectWizardPage_DescriptionString;
	public static String TemplateSelectionToolTip;
	public static String NewProjectWizardPage_NoTemplateFoundMessage;
	public static String TemplateGroupHeader;
	public static String NewProjectTargetSelection_Tooltip;
	public static String NewProjectTargetSelection_Label;
	public static String RunIdfCommandButtonTxt;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
