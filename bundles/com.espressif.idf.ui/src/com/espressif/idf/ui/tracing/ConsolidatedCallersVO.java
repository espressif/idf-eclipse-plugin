/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

/**
 * Collective callers vo for the consolidated view
 * 
 * @author Ali Azam Rana
 *
 */
public class ConsolidatedCallersVO
{
	private AddressInfoVO addressInfoVO;

	private double sizeUsed;

	private int hitCount;

	public double getSizeUsed()
	{
		return sizeUsed;
	}

	public void setSizeUsed(double sizeUsed)
	{
		this.sizeUsed = sizeUsed;
	}

	public AddressInfoVO getAddressInfoVO()
	{
		return addressInfoVO;
	}

	public void setAddressInfoVO(AddressInfoVO addressInfoVO)
	{
		this.addressInfoVO = addressInfoVO;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ConsolidatedCallersVO))
		{
			return false;
		}

		ConsolidatedCallersVO consolidatedCallersVO = (ConsolidatedCallersVO) obj;
		if (this.getAddressInfoVO().getAddress().equals(consolidatedCallersVO.getAddressInfoVO().getAddress()))
		{
			return true;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		return this.getAddressInfoVO().hashCode();
	}

	public int getHitCount()
	{
		return hitCount;
	}

	public void setHitCount(int hitCount)
	{
		this.hitCount = hitCount;
	}
}
