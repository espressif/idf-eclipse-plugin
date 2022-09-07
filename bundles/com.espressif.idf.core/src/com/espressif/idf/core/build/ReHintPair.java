/*******************************************************************************
 * Copyright 2022-2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.build;

/**
 * Represents a parsed re and hint entries of a hints.yml file.
 * 
 * @author Denys Almazov
 */
public class ReHintPair
{
	private String re;
	private String hint;

	public ReHintPair(String re, String hint)
	{
		this.re = re;
		this.hint = hint;
	}

	public String getRe()
	{
		return re;
	}

	public String getHint()
	{
		return hint;
	}

}
