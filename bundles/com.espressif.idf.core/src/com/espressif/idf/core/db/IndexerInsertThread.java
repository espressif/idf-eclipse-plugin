package com.espressif.idf.core.db;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.espressif.idf.core.logging.Logger;

public class IndexerInsertThread extends Thread
{
	private ConcurrentLinkedQueue<IndexerVO> insertQueue;
	private IndexerDbOps indexerDbOps;
	private boolean stop = false;
	
	public IndexerInsertThread()
	{
		insertQueue = new ConcurrentLinkedQueue<>();
		indexerDbOps = IndexerDbOps.getIndexerDbOps();
	}
	
	@Override
	public void run()
	{
		while(!insertQueue.isEmpty() || !stop)
		{
			try
			{
				Thread.sleep(500);
			}
			catch (InterruptedException e)
			{
				Logger.log(e);
			}
			
			IndexerVO indexerVO = insertQueue.poll();
			if (indexerVO == null)
				continue;
			
			try
			{
				indexerDbOps.insertFunctionDetail(indexerVO);
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}
	}

	public ConcurrentLinkedQueue<IndexerVO> getInsertQueue()
	{
		return insertQueue;
	}

	public boolean isStop()
	{
		return stop;
	}

	public void setStop(boolean stop)
	{
		this.stop = stop;
	}

}
