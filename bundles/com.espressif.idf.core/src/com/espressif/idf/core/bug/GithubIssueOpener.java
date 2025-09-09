/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD.
 * All rights reserved. Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.bug;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Opens the default web browser to create a new issue on the GitHub repository.
 * 
 * @author Ali Azam Rana
 */
public class GithubIssueOpener
{
	private static final String issueUrlBase = "https://github.com/espressif/idf-eclipse-plugin/issues/new"; //$NON-NLS-1$
	
	public static void openNewIssue()
			throws Exception
	{
		String q = "&template=" + enc("bug_report.md"); // e.g. "bug_report.md" //$NON-NLS-1$ //$NON-NLS-2$
		URI uri = new URI(issueUrlBase + "?" + q); //$NON-NLS-1$
		Desktop.getDesktop().browse(uri);
	}

	private static String enc(String s) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
	}
}
