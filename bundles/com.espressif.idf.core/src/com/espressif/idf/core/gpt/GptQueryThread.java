package com.espressif.idf.core.gpt;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.espressif.idf.core.db.IndexerVO;
import com.espressif.idf.core.logging.Logger;
import com.google.gson.JsonObject;

public class GptQueryThread implements Callable<Void>
{
	private volatile boolean terminate;
	private ConcurrentLinkedQueue<IndexerVO> gptQueryQueue;
	private ConcurrentLinkedQueue<IndexerVO> insertQueue;
	
	public GptQueryThread(ConcurrentLinkedQueue<IndexerVO> gptQueryQueue, ConcurrentLinkedQueue<IndexerVO> insertQueue)
	{
		this.gptQueryQueue = gptQueryQueue;
		this.insertQueue = insertQueue;
	}
	
	@Override
	public Void call() throws Exception
	{
		while (!terminate || !gptQueryQueue.isEmpty())
		{
			IndexerVO indexerVO = gptQueryQueue.poll();
			if (indexerVO == null)
			{
				Thread.yield();
				continue;
			}
			JsonObject response = GptApi.queryGPTForFunctions(indexerVO.getFunctionName(), indexerVO.getBody(), indexerVO.getFileDefinition());
			Logger.log(response.toString());
			indexerVO.setDescription(response.toString());
			insertQueue.add(indexerVO);
			Logger.log("GPT Query Queue Size Remaining: " + gptQueryQueue.size()); //$NON-NLS-1$
		}
		return null;
	}

	public boolean isTerminate()
	{
		return terminate;
	}

	public void setTerminate(boolean terminate)
	{
		this.terminate = terminate;
	}
}
