package com.espressif.idf.sdk.config.core.server;

public interface IMessageHandlerListener
{
	public void notifyRequestServed(String message);
}