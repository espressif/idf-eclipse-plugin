package com.espressif.idf.sdk.config.core.server;

public interface IMessagesHandlerNotifier
{
	public void addListener(IMessageHandlerListener listener);

	public void removeListener(IMessageHandlerListener listener);

	public void notifyHandler(String message);
}