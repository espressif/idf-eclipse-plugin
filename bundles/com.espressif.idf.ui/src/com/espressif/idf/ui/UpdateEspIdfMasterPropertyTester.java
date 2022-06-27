package com.espressif.idf.ui;

import org.eclipse.core.expressions.PropertyTester;

import com.espressif.idf.core.util.IDFUtil;

public class UpdateEspIdfMasterPropertyTester extends PropertyTester
{

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		System.out.println("PROPERTY TESTER CONSTRUCTOR");
		return IDFUtil.getEspIdfVersion().contains("dev");
	}

}
