package com.espressif.idf.sdk.config.core;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.core.runtime.content.IContentDescription;

public class SDKConfigContentDescriber implements IContentDescriber
{

	@Override
	public int describe(InputStream contents, IContentDescription description) throws IOException
	{
		return VALID;
	}

	@Override
	public QualifiedName[] getSupportedOptions()
	{
		return new QualifiedName[0];
	}

}
