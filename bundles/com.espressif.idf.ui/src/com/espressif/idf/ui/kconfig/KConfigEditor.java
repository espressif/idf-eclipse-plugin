/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.kconfig;

import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
@SuppressWarnings("restriction")
public class KConfigEditor extends ExtensionBasedTextEditor
{
    public static final String KCONFIG_EDITOR_ID = "com.espressif.idf.ui.kconfig.KConfigEditor";
    public static final String KCONFIG_CONTENT_TYPE = "com.espressif.idf.ui.kconfig.contentType";
    private static final String CONTEXT_ID = "com.espressif.idf.ui.kconfig.editorContext";
    
    public KConfigEditor()
    {
        super();
    }
    
    @Override
    protected void initializeKeyBindingScopes() {
        setKeyBindingScopes(new String[] { CONTEXT_ID });
    }
}
