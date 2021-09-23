/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import org.eclipse.core.resources.IFile;

/**
 * Callers address info vo
 * 
 * @author Ali Azam Rana
 *
 */
public class AddressInfoVO
{
	private IFile file;
	private int lineNumber;
	private String functionName;
	private String address;
	private String fullFilePath;

	public AddressInfoVO(IFile file, int lineNumber, String funcitonName, String address, String fullFilePath)
	{
		this.file = file;
		this.lineNumber = lineNumber;
		this.functionName = funcitonName;
		this.address = address;
		this.fullFilePath = fullFilePath;
	}

	public int getLineNumber()
	{
		return lineNumber;
	}

	public void setLineNumber(int lineNumber)
	{
		this.lineNumber = lineNumber;
	}

	public String getFunctionName()
	{
		return functionName;
	}

	public void setFunctionName(String functionName)
	{
		this.functionName = functionName;
	}

	public IFile getFile()
	{
		return file;
	}

	public void setFile(IFile file)
	{
		this.file = file;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(file.getName());
		sb.append(":");
		sb.append(functionName);
		sb.append(":");
		sb.append(lineNumber);
		return sb.toString();
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getFullFilePath()
	{
		return fullFilePath;
	}

	public void setFullFilePath(String fullFilePath)
	{
		this.fullFilePath = fullFilePath;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof AddressInfoVO))
		{
			return false;
		}

		AddressInfoVO addressInfoVO = (AddressInfoVO) obj;
		if (this.address.equals(addressInfoVO.address))
		{
			return true;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return address.hashCode();
	}
}
