package com.espressif.idf.core.util;

import java.util.regex.Pattern;

import com.espressif.idf.core.build.PartitionTableBean;

public class PartitionBeanValidator
{
	public String validateBean(PartitionTableBean bean, int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return validateName(bean.getName());
		case 1:
			return validateType(bean.getType());
		case 2:
			return validateSubType(bean.getSubType(), bean.getType());
		case 3:
			return validateOffSet(bean.getOffSet());
		case 4:
			return validateSize(bean.getSize());
		case 5:
			return validateFlags(bean.getFlag());
		default:
			break;
		}
		return StringUtil.EMPTY;
	}

	private String validateFlags(String flag)
	{
		return StringUtil.EMPTY; // Always true, because flag field has only two options
	}

	private String validateSize(String size)
	{
		if (size == null || size.isBlank())
		{
			return Messages.SizeValidationError_1;
		}

		Pattern pattern = Pattern.compile("(^((0x)[0-9a-fA-F]*)$)|^([0-9]*)$|([0-9]*((K|M)$))"); //$NON-NLS-1$
		if (!pattern.matcher(size).matches())
		{
			return Messages.SizeValidationError_2;
		}

		return StringUtil.EMPTY;
	}

	private String validateOffSet(String offSet)
	{
		if (offSet == null || offSet.isBlank())
		{
			return StringUtil.EMPTY;
		}

		Pattern pattern = Pattern.compile("(^((0x)[0-9a-fA-F]*)$)|^([0-9]*)$|([0-9]*((K|M)$))"); //$NON-NLS-1$
		if (!pattern.matcher(offSet).matches())
		{
			return Messages.OffSetValidationError_1;
		}
		return StringUtil.EMPTY;
	}

	private String validateSubType(String subType, String type)
	{
		if (subType == null || subType.isBlank())
		{
			return Messages.SubTypeValidationError_1;
		}

		Pattern pattern = Pattern.compile("^(0x00|app)$"); //$NON-NLS-1$
		if (pattern.matcher(type).matches())
		{
			pattern = Pattern
					.compile("^(factory|test|ota_[0-9]|ota_1[0-5]|test|0x00)$|^(0x)(([1][0-9a-fA-F])|[2][0])$"); //$NON-NLS-1$
			if (!pattern.matcher(subType).matches())
			{
				return Messages.SubTypeValidationError_2;
			}
		}

		pattern = Pattern.compile("^(0x01|data)$"); //$NON-NLS-1$

		if (pattern.matcher(type).matches())
		{
			pattern = Pattern.compile("^(ota|phy|nvs|nvs_keys|spiffs|coredump|fat)$|^(0x)(([0][0-6])|[8][0-2])$"); //$NON-NLS-1$
			if (!pattern.matcher(subType).matches())
			{
				return Messages.SubTypeValidationError_3;
			}
		}

		pattern = Pattern.compile("((0x)[4-9a-fA-F]([0-9a-e]|[A-E]))$"); //$NON-NLS-1$
		if (pattern.matcher(type).matches())
		{
			pattern = Pattern.compile("^((0x)[0-9a-fA-F]([0-9a-e]|[A-E]))$"); //$NON-NLS-1$
			if (!pattern.matcher(subType).matches())
			{
				return Messages.SubTypeValidationError_4;
			}
		}
		return StringUtil.EMPTY;
	}

	private String validateType(String type)
	{
		if (type == null || type.isBlank())
		{
			return Messages.TypeValidationError_1;
		}

		Pattern pattern = Pattern.compile(
				"^(0x00|0x01|app|data)$|^((0x)((([4-9a-e]|[A-E])[0-9a-fA-F])|([fF]([0-9a-e]|[A-E]))))$|^([01][0-9][0-9]|2[0-4][0-9]|25[0-4])$"); //$NON-NLS-1$
		if (!pattern.matcher(type).matches())
		{
			return Messages.TypeValidationError_2;
		}

		return StringUtil.EMPTY;
	}

	private String validateName(String name)
	{
		if (name == null || name.isBlank())
		{
			return Messages.NameValidationError_1;
		}

		if (name.length() > 16)
		{
			return Messages.NameValidationError_2;
		}
		return StringUtil.EMPTY;
	}
}
