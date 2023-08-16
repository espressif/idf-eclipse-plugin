package com.espressif.idf.core.db;

import java.util.List;

public class GptResponseVO
{
	private String id;
	private String object;
	private long created;
	private String model;
	private List<Choice> choices;
	private Usage usage;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getObject()
	{
		return object;
	}

	public void setObject(String object)
	{
		this.object = object;
	}

	public long getCreated()
	{
		return created;
	}

	public void setCreated(long created)
	{
		this.created = created;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel(String model)
	{
		this.model = model;
	}

	public List<Choice> getChoices()
	{
		return choices;
	}

	public void setChoices(List<Choice> choices)
	{
		this.choices = choices;
	}

	public Usage getUsage()
	{
		return usage;
	}

	public void setUsage(Usage usage)
	{
		this.usage = usage;
	}

}

class Choice
{
	private int index;
	private Message message;
	private String finish_reason;

	public int getIndex()
	{
		return index;
	}

	public void setIndex(int index)
	{
		this.index = index;
	}

	public Message getMessage()
	{
		return message;
	}

	public void setMessage(Message message)
	{
		this.message = message;
	}

	public String getFinish_reason()
	{
		return finish_reason;
	}

	public void setFinish_reason(String finish_reason)
	{
		this.finish_reason = finish_reason;
	}
}

class Message
{
	private String role;
	private String content;

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}
}

class Usage
{
	private int prompt_tokens;
	private int completion_tokens;
	private int total_tokens;

	public int getPrompt_tokens()
	{
		return prompt_tokens;
	}

	public void setPrompt_tokens(int prompt_tokens)
	{
		this.prompt_tokens = prompt_tokens;
	}

	public int getCompletion_tokens()
	{
		return completion_tokens;
	}

	public void setCompletion_tokens(int completion_tokens)
	{
		this.completion_tokens = completion_tokens;
	}

	public int getTotal_tokens()
	{
		return total_tokens;
	}

	public void setTotal_tokens(int total_tokens)
	{
		this.total_tokens = total_tokens;
	}
}
