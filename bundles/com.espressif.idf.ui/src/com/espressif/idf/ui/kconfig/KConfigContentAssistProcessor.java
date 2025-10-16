/*******************************************************************************
 * Copyright 2024 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui.kconfig;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * @author Kondal Kolipaka <kondal.kolipaka@espressif.com>
 */
public class KConfigContentAssistProcessor implements IContentAssistProcessor
{
    private static final String[] MAIN_KEYWORDS = {
        "config", "menuconfig", "choice", "endchoice", "menu", "endmenu",
        "if", "endif", "source", "comment", "mainmenu"
    };
    
    private static final String[] TYPES = {
        "bool", "tristate", "int", "hex", "string"
    };
    
    private static final String[] PROPERTIES = {
        "default", "depends", "select", "imply", "range", "help", "prompt",
        "visible", "optional"
    };
    
    private static final String[] VALUES = {
        "y", "n", "m"
    };

    @Override
    public ICompletionProposal[] computeCompletionProposals(org.eclipse.jface.text.ITextViewer viewer, int offset)
    {
        List<ICompletionProposal> proposals = new ArrayList<>();
        
        String text = viewer.getDocument().get();
        int lineStart = text.lastIndexOf('\n', offset - 1) + 1;
        String lineText = text.substring(lineStart, offset).trim();
        
        for (String keyword : MAIN_KEYWORDS) {
            if (keyword.toLowerCase().startsWith(lineText.toLowerCase())) {
                proposals.add(createProposal(keyword, offset - lineText.length(), lineText.length(), keyword));
            }
        }
        
        for (String type : TYPES) {
            if (type.toLowerCase().startsWith(lineText.toLowerCase())) {
                proposals.add(createProposal(type, offset - lineText.length(), lineText.length(), type));
            }
        }
        
        for (String property : PROPERTIES) {
            if (property.toLowerCase().startsWith(lineText.toLowerCase())) {
                proposals.add(createProposal(property, offset - lineText.length(), lineText.length(), property));
            }
        }
        
        for (String value : VALUES) {
            if (value.toLowerCase().startsWith(lineText.toLowerCase())) {
                proposals.add(createProposal(value, offset - lineText.length(), lineText.length(), value));
            }
        }
        
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }
    
    private ICompletionProposal createProposal(String text, int offset, int length, String displayText) {
        return new CompletionProposal(text, offset, length, text.length(), null, displayText, null, null);
    }

    @Override
    public IContextInformation[] computeContextInformation(org.eclipse.jface.text.ITextViewer viewer, int offset)
    {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return new char[] { ' ', '\t', '\n' };
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    @Override
    public String getErrorMessage()
    {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }
}
