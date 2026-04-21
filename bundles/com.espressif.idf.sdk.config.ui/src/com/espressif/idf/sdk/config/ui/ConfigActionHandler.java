package com.espressif.idf.sdk.config.ui;

import org.json.simple.JSONObject;

import com.espressif.idf.sdk.config.core.KConfigMenuItem;

public interface ConfigActionHandler
{
	void onCommandExecuted(JSONObject jsonMap);

	void onTextModified(String key, Object value);

	void onResetRequested(String key);

	void onMenuResetRequested(KConfigMenuItem menu);
}
