/*******************************************************************************
 * Copyright 2018-2019 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.wizard;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.wizard.messages"; //$NON-NLS-1$
	public static String ImportIDFProjectWizard_0;
	public static String ImportIDFProjectWizardPage_0;
	public static String ImportIDFProjectWizardPage_1;
	public static String ImportIDFProjectWizardPage_2;
	public static String ImportIDFProjectWizardPage_3;
	public static String ImportIDFProjectWizardPage_4;
	public static String ImportIDFProjectWizardPage_5;
	public static String ImportIDFProjectWizardPage_6;
	public static String ImportIDFProjectWizardPage_7;
	public static String ImportIDFProjectWizardPage_8;
	public static String ImportIDFProjectWizardPage_9;
	public static String ImportIDFProjectWizardPage_CopyIntoWorkspace;
	public static String NewIDFProjectWizard_NewIDFProject;
	public static String NewIDFProjectWizard_Project_Title;
	public static String NewIDFProjectWizard_ProjectDesc;
	public static String NewIDFProjectWizard_TemplatesHeader;
	public static String WizardNewProjectCreationPage_NameCantIncludeSpaceErr;
	public static String WizardNewProjectCreationPage_WorkspaceLocCantIncludeSpaceErr;
	public static String NewIdfComponentWizard_NewIdfComponent;
	public static String NewIdfComponentWizard_Component_Title;
	public static String NewIdfComponentWizard_Component_name;
	public static String NewIdfComponentWizard_CompDesc;
	public static String NewIdfComponentWizard_Page;
	public static String NewIdfComponentWizard_NameCantIncludeSpaceErr;
	public static String NewIdfComponentWizard_NameCantBeEmptyErr;
	public static String NewIdfComponentWizard_NameAlreadyExistsErr;
	public static String NewComponentWizardPage_CantCreateCompErr;
	public static String NewComponentWizardPage_ProjectDoesntExistErr;
	public static String NewComponentWizardPage_ProjectNameLbl;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
