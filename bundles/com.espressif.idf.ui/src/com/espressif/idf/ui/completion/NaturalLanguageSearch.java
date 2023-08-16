package com.espressif.idf.ui.completion;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

import com.espressif.idf.core.db.IndexerDbOps;
import com.espressif.idf.core.db.IndexerVO;
import com.espressif.idf.core.gpt.GptApi;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.pinecone.PineconeOps;
import com.espressif.idf.core.util.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@SuppressWarnings("restriction")
public class NaturalLanguageSearch implements ICompletionProposalComputer
{

	private static final String PREFIX_STRING = "// ->";
	IndexerDbOps indexerDbOps;

	public NaturalLanguageSearch()
	{
		indexerDbOps = IndexerDbOps.getIndexerDbOps();
		
	}

	@Override
	public void sessionStarted()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public String getErrorMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context,
			IProgressMonitor monitor)
	{
		try
		{

			List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
			
			IDocument document = context.getDocument();
			int offset = context.getInvocationOffset();
			int lineNum = document.getLineOfOffset(offset);
			int lineStartOffset = document.getLineInformation(lineNum).getOffset();

			String prefix = document.get(lineStartOffset, offset - lineStartOffset).strip();
			if (prefix.startsWith(PREFIX_STRING))
			{
				String query = prefix.substring(PREFIX_STRING.length()).strip();
				List<Float> queryVector = GptApi.queryGPTForEmbeddings(query);
				JsonObject jsonObject = PineconeOps.pineConeQuery(queryVector);
				JsonArray jsonArray = jsonObject.get("matches").getAsJsonArray();
				Logger.log(jsonArray.toString());
				for (JsonElement jsonElement : jsonArray)
				{
					Logger.log(String.valueOf(jsonElement.getAsJsonObject().get("score").getAsFloat()));
					
					String id = jsonElement.getAsJsonObject().get("id").getAsString();
					IndexerVO indexerVo = indexerDbOps.fetchIndexerVo(Integer.parseInt(id));
					Logger.log(indexerVo.getGptDescription());
					StringBuilder displayString = new StringBuilder();
					String [] bodySplit = indexerVo.getBody().split(System.lineSeparator());
					String firstLine = bodySplit[0];
					int funcNameIndex = firstLine.indexOf(indexerVo.getFunctionName());
					String returnType = StringUtil.EMPTY;
					if (funcNameIndex != 0)
					{
						returnType = firstLine.substring(0, funcNameIndex);
					}
					
					displayString.append(firstLine.substring(funcNameIndex));
					displayString.append(" : ");
					displayString.append(returnType);
					
					NlsCompletionProposal completionProposal = new NlsCompletionProposal(
							System.lineSeparator() + indexerVo.getFunctionName(), offset, 0,
							indexerVo.getFunctionName().length() + 1,
							MakeUIImages.getImage(MakeUIImages.IMG_OBJS_FUNCTION), displayString.toString(), null,
							indexerVo.getGptDescription());
					completionProposal.setHeaderLinks(parseToList(indexerVo.getFileHeaders()));
					completionProposal.setDefLink(indexerVo.getFileDefinition());
					completionProposal.setScore(jsonElement.getAsJsonObject().get("score").getAsFloat());
					completionProposal.setFunctionBody(indexerVo.getBody());
					proposals.add(completionProposal);
				}
			}
			return proposals;
		}
		catch (Exception e)
		{
			Logger.log(e);
		}
		return null;
	}

	private List<String> parseToList(String input)
	{
		List<String> result = new ArrayList<>();

		// Regular expression to extract file paths
		Pattern pattern = Pattern.compile("file:([^,\\[\\]]+)");
		Matcher matcher = pattern.matcher(input);
		while (matcher.find())
		{
			String fullPath = matcher.group(1).trim();
            String fileName = fullPath.substring(fullPath.lastIndexOf('/') + 1);
            result.add(fileName);
		}

		return result;
	}

	@Override
	public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context,
			IProgressMonitor monitor)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
