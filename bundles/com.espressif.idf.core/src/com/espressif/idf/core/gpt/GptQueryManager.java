package com.espressif.idf.core.gpt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.espressif.idf.core.db.IndexerInsertThread;
import com.espressif.idf.core.db.IndexerVO;

public class GptQueryManager
{
	private static final int THREAD_COUNT = 10;

	private ExecutorService executorService;
	private IndexerInsertThread indexerInsertThread;
	private ConcurrentLinkedQueue<IndexerVO> gptQueryQueue = new ConcurrentLinkedQueue<>();
	private List<GptQueryThread> submittedGptQueryThreads = new ArrayList<>();

	public GptQueryManager()
	{
		this.executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		indexerInsertThread = new IndexerInsertThread();
		for (int i = 0; i <THREAD_COUNT; i++)
		{
			GptQueryThread gptQueryThread = new GptQueryThread(gptQueryQueue, indexerInsertThread.getInsertQueue());
			submittedGptQueryThreads.add(gptQueryThread);
			executorService.submit(gptQueryThread);
		}
		indexerInsertThread.start();
	}

	public void addToQueue(IndexerVO indexerVO) throws Exception
	{
		gptQueryQueue.add(indexerVO);
	}

	public void shutdown()
	{
		for (GptQueryThread gptQueryThread : submittedGptQueryThreads)
		{
			gptQueryThread.setTerminate(true);
		}
		indexerInsertThread.setStop(true);
		executorService.shutdown();
	}
}
