/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing.images;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Image fetcher for the tracing view
 * @author Ali Azam Rana
 *
 */
public class TracingImagesCache
{

	static Map<String, Image> imageCache = new HashMap<>();
	static Map<String, ImageDescriptor> imageDescriptorCache = new HashMap<>();

	public static Image getImage(String imageName)
	{
		if (!imageCache.containsKey(imageName))
		{
			imageCache.put(imageName, getImageDescriptor(imageName).createImage());
		}
		return imageCache.get(imageName);
	}

	public static ImageDescriptor getImageDescriptor(String imageName)
	{
		if (!imageDescriptorCache.containsKey(imageName))
		{
			URL url = TracingImagesCache.class.getResource(imageName);
			imageDescriptorCache.put(imageName, ImageDescriptor.createFromURL(url));
		}
		return imageDescriptorCache.get(imageName);
	}

}