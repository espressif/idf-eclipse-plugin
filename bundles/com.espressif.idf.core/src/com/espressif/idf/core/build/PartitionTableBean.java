package com.espressif.idf.core.build;

import com.opencsv.bean.CsvBindByName;

public class PartitionTableBean
{

	@CsvBindByName(column = "# Name")
	private String name;

	@CsvBindByName(column = "Type")
	private String type;

	@CsvBindByName(column = "SubType")
	private String subType;

	@CsvBindByName(column = "Offset")
	private String offset;

	@CsvBindByName(column = "Size")
	private String size;

	@CsvBindByName(column = "Flags")
	private String flags;

}
