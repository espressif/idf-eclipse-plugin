/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.espressif.idf.core.logging.Logger;

/**
 * Represents a parsed re and hint entries of a hints.yml file.
 * 
 * @author Denys Almazov
 */
public class ReHintPair
{
	private Pattern re;
	private String hint;
	private String ref; // Optional reference

	public ReHintPair(String re, String hint)
	{
		this(re, hint, null);
	}

	public ReHintPair(String re, String hint, String ref)
	{
		try
		{
			this.re = Pattern.compile(re);
		}
		catch (PatternSyntaxException e)
		{
			Logger.log(e);
		}
		this.hint = hint;
		this.ref = ref;
	}

	public Optional<Pattern> getRe()
	{
		return Optional.ofNullable(re);
	}

	public String getHint()
	{
		return hint;
	}

	public Optional<String> getRef()
	{
		return Optional.ofNullable(ref);
	}
}
