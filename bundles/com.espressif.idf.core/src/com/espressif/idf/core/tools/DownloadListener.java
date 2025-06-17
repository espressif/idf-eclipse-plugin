/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools;

/**
 * Interface to use for the download listening this can be used in your own classes. 
 * Added specifically for {@link EimLoader} 
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
public interface DownloadListener
{
	public void onProgress(int percent);
	public void onCompleted(String filePath);
	public void onError(String message, Exception e);

}
