package com.espressif.idf.core.tools;

public interface DownloadListener
{
	public void onProgress(int percent);
	public void onCompleted(String filePath);
	public void onError(String message, Exception e);

}
