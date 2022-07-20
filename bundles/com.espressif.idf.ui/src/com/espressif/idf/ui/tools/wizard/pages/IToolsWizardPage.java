/*******************************************************************************
 * Copyright 2022 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.tools.wizard.pages;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface to put some commomn methods in the wizard pages
 * 
 * @author Ali Azam Rana
 *
 */
public interface IToolsWizardPage
{
	public void cancel();
	
	public Composite getPageComposite();
}
