package com.espressif.idf.core.db;

public class IndexerVO
{
	private int id;
	private String functionName;
	private String body;
	private String fileDefinition;
	private String fileHeaders;
	private String description;
	
	private String gptDescription;
	private GptResponseVO gptResponseVO;
	
	public IndexerVO()
	{
	}
	
	public IndexerVO(String functionName, String body, String fileDefinition, String fileHeaders, String description)
	{
		this.functionName = functionName;
		this.body = body;
		this.fileDefinition = fileDefinition;
		this.fileHeaders = fileHeaders;
		this.description = description;
	}


	public String getFunctionName()
	{
		return functionName;
	}


	public void setFunctionName(String functionName)
	{
		this.functionName = functionName;
	}


	public String getBody()
	{
		return body;
	}


	public void setBody(String body)
	{
		this.body = body;
	}


	public String getFileDefinition()
	{
		return fileDefinition;
	}


	public void setFileDefinition(String fileDefinition)
	{
		this.fileDefinition = fileDefinition;
	}


	public String getFileHeaders()
	{
		return fileHeaders;
	}


	public void setFileHeaders(String fileHeaders)
	{
		this.fileHeaders = fileHeaders;
	}


	public String getDescription()
	{
		return description;
	}


	public void setDescription(String description)
	{
		this.description = description;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getGptDescription()
	{
		return gptDescription;
	}

	public void setGptDescription(String gptDescription)
	{
		this.gptDescription = gptDescription;
	}

	public GptResponseVO getGptResponseVO()
	{
		return gptResponseVO;
	}

	public void setGptResponseVO(GptResponseVO gptResponseVO)
	{
		this.gptResponseVO = gptResponseVO;
	}
}
