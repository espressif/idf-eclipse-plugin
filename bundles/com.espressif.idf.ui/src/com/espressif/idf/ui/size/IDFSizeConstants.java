/*******************************************************************************
 * Copyright 2020 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.size;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 *
 */
public interface IDFSizeConstants
{
	String DATA = "data"; // DRAM .data //$NON-NLS-1$
	String BSS = "bss"; // DRAM .bss //$NON-NLS-1$
	String IRAM = "iram"; //$NON-NLS-1$
	String DIRAM = "diram"; //$NON-NLS-1$
	String FLASH_TEXT = "flash_text"; //$NON-NLS-1$
	String FLASH_RODATA = "flash_rodata"; //$NON-NLS-1$
	String OTHER = "other"; //$NON-NLS-1$
	String TOTAL = "total"; //$NON-NLS-1$

	//idf_size.py overview constants
	String DRAM_DATA = "dram_data"; //$NON-NLS-1$
	String DRAM_BSS = "dram_bss"; //$NON-NLS-1$
	String FLASH_CODE = "flash_code"; //$NON-NLS-1$
	String TOTAL_SIZE = "total_size"; //$NON-NLS-1$
	String USED_IRAM = "used_iram"; //$NON-NLS-1$
	String AVAILABLE_IRAM = "available_iram"; //$NON-NLS-1$
	String USED_IRAM_RATIO = "used_iram_ratio"; //$NON-NLS-1$
	String USED_DRAM = "used_dram"; //$NON-NLS-1$
	String AVAILABLE_DRAM = "available_dram"; //$NON-NLS-1$
	String USED_DRAM_RATIO = "used_dram_ratio"; //$NON-NLS-1$
	
	//esp32-s2 specific
	String USED_DIRAM = "used_diram"; //$NON-NLS-1$
	String AVAILABLE_DIRAM = "available_diram"; //$NON-NLS-1$
	String USED_DIRAM_RATIO = "used_diram_ratio"; //$NON-NLS-1$
	

}
