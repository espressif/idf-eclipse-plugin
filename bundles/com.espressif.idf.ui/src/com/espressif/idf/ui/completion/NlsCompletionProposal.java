package com.espressif.idf.ui.completion;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.BoldStylerProvider;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension7;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class NlsCompletionProposal implements ICompletionProposal, ICompletionProposalExtension3
{
	/** The string to be displayed in the completion proposal popup. */
	private String fDisplayString;
	/** The replacement string. */
	private String fReplacementString;
	/** The replacement offset. */
	private int fReplacementOffset;
	/** The replacement length. */
	private int fReplacementLength;
	/** The cursor position after this proposal has been applied. */
	private int fCursorPosition;
	/** The image to be displayed in the completion proposal popup. */
	private Image fImage;
	/** The context information of this proposal. */
	private IContextInformation fContextInformation;
	/** The additional info of this proposal. */
	private String fAdditionalProposalInfo;
	private List<String> headerLinksList;
	private String defLinkString;
	private String functionBody;

	private float score;

	/**
	 * Creates a new completion proposal based on the provided information. The replacement string is considered being
	 * the display string too. All remaining fields are set to <code>null</code>.
	 *
	 * @param replacementString the actual string to be inserted into the document
	 * @param replacementOffset the offset of the text to be replaced
	 * @param replacementLength the length of the text to be replaced
	 * @param cursorPosition    the position of the cursor following the insert relative to replacementOffset
	 */
	public NlsCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition)
	{
		this(replacementString, replacementOffset, replacementLength, cursorPosition, null, null, null, null);
	}

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 *
	 * @param replacementString      the actual string to be inserted into the document
	 * @param replacementOffset      the offset of the text to be replaced
	 * @param replacementLength      the length of the text to be replaced
	 * @param cursorPosition         the position of the cursor following the insert relative to replacementOffset
	 * @param image                  the image to display for this proposal
	 * @param displayString          the string to be displayed for the proposal
	 * @param contextInformation     the context information associated with this proposal
	 * @param additionalProposalInfo the additional information associated with this proposal
	 */
	public NlsCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo)
	{
		Assert.isNotNull(replacementString);
		Assert.isTrue(replacementOffset >= 0);
		Assert.isTrue(replacementLength >= 0);
		Assert.isTrue(cursorPosition >= 0);

		fReplacementString = replacementString;
		fReplacementOffset = replacementOffset;
		fReplacementLength = replacementLength;
		fCursorPosition = cursorPosition;
		fImage = image;
		fDisplayString = displayString;
		fContextInformation = contextInformation;
		fAdditionalProposalInfo = additionalProposalInfo;
	}

	private void addIncludesToDoc(IDocument document, String includeToAdd) throws BadLocationException
	{
		int lastOffset = -1;
		for (int i = 0; i < document.getNumberOfLines(); i++)
		{
			IRegion region = document.getLineInformation(i);
			String lineContent = document.get(region.getOffset(), region.getLength());
			if (lineContent.contains(includeToAdd))
				return;
			
			if (lineContent.trim().startsWith("#include"))
			{
				lastOffset = document.getLineOffset(i) + lineContent.length() + 1; // +1 for newline character
			}
			else if (lastOffset != -1) // If we've seen includes before, but this line is not an include
			{
				break; // Stop, since we found the last include
			}
		}
		if (lastOffset == -1)
		{
			document.replace(0, 0, includeToAdd);
		}
		document.replace(lastOffset, 0, includeToAdd);
	}

	@Override
	public void apply(IDocument document)
	{
		try
		{
			// 1. Retrieve indentation of the current line
	        int lineNum = document.getLineOfOffset(fReplacementOffset);
	        String currentLine = document.get(document.getLineOffset(lineNum), document.getLineLength(lineNum));
	        Matcher matcher = Pattern.compile("^\\s*").matcher(currentLine); // regex to get leading whitespace
	        String indentation = "";
	        if (matcher.find())
	        {
	            indentation = matcher.group();
	        }
	        
	        // Add the primary replacement with respect to the indentation
	        String formattedReplacement = System.lineSeparator() + indentation + fReplacementString.trim();
	        document.replace(fReplacementOffset, fReplacementLength, formattedReplacement);
	        int insertedLine = document.getLineOfOffset(fReplacementOffset) + 1;
	        fReplacementOffset = document.getLineOffset(insertedLine);
			// Add the include directives
			if (headerLinksList != null && !headerLinksList.isEmpty())
			{
				StringBuilder includeDirectives = new StringBuilder();
				String headerLink = headerLinksList.get(0);
				String[] pathSplit = headerLink.split("/");
				headerLink = pathSplit[pathSplit.length - 1];
				includeDirectives.append("#include \"").append(headerLink).append("\"")
						.append(System.lineSeparator());
				addIncludesToDoc(document, includeDirectives.toString());
			}
		}
		catch (BadLocationException x)
		{
			// ignore
		}
	}

	@Override
	public Point getSelection(IDocument document)
	{
		return new Point(fReplacementOffset + 1 + fReplacementString.length(), 0);
	}

	@Override
	public IContextInformation getContextInformation()
	{
		return fContextInformation;
	}

	@Override
	public Image getImage()
	{
		return fImage;
	}

	@Override
	public String getDisplayString()
	{
		if (fDisplayString != null)
			return fDisplayString;
		return fReplacementString;
	}

	@Override
	public String getAdditionalProposalInfo()
	{
		return fAdditionalProposalInfo;
	}

	public void setHeaderLinks(List<String> headerLinksList)
	{
		this.headerLinksList = headerLinksList;
	}

	public void setDefLink(String defLink)
	{
		this.defLinkString = defLink;
	}

	@Override
	public IInformationControlCreator getInformationControlCreator()
	{
		IInformationControlCreator controlCreator = new IInformationControlCreator()
		{
			@Override
			public IInformationControl createInformationControl(Shell parent)
			{
				NlsInformationControl nlsInformationControl = new NlsInformationControl(parent);
				nlsInformationControl.setInput(getAdditionalProposalInfo(), functionBody);
				return nlsInformationControl;
			}
		};
		return controlCreator;
	}

	@Override
	public CharSequence getPrefixCompletionText(IDocument document, int completionOffset)
	{
		return null;
	}

	@Override
	public int getPrefixCompletionStart(IDocument document, int completionOffset)
	{
		return 0;
	}

	public float getScore()
	{
		return score;
	}

	public void setScore(float score)
	{
		this.score = score;
	}

	public String getFunctionBody()
	{
		return functionBody;
	}

	public void setFunctionBody(String functionBody)
	{
		this.functionBody = functionBody;
	}
}
