/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.espressif.idf.core.build.CsvBean;

public interface CsvTableDataService<T extends CsvBean>
{

	public List<T> parseCsv(Path csvFile) throws IOException;

	public void saveCsv(IFile csvFile, List<T> beansToSave);
}
