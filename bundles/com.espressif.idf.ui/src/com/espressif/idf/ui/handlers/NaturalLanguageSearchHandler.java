package com.espressif.idf.ui.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.espressif.idf.core.db.IndexerDbOps;
import com.espressif.idf.core.db.IndexerVO;
import com.espressif.idf.core.gpt.GptApi;
import com.espressif.idf.core.gpt.GptQueryManager;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.pinecone.PineconeOps;
import com.espressif.idf.core.util.StringUtil;

@SuppressWarnings("restriction")
public class NaturalLanguageSearchHandler extends AbstractHandler
{

	private GptQueryManager gptQueryManager;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		gptQueryManager = new GptQueryManager();
		Job indexingMainJob = new Job("Indexing Main Job")
		{
			
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
//					functionNamesAndDescriptions();
					List<IndexerVO> indexerVOs = IndexerDbOps.getIndexerDbOps().fetchIndexerVOs();
					IndexerDbOps.getIndexerDbOps().insertGptFuncDesc(indexerVOs);
					for (IndexerVO indexerVO : indexerVOs)
					{
						List<Float> vector = GptApi.queryGPTForEmbeddings(indexerVO.getGptDescription());
						PineconeOps.pineConeUpsert(indexerVO.getId(), vector);	
					}
					
					gptQueryManager.shutdown();
				}
				catch (Exception e)
				{
					Logger.log(e);
				}
				return Status.OK_STATUS;
			}
		};
		indexingMainJob.schedule();
		return null;
	}

	private void functionNamesAndDescriptions() throws Exception
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		for (IProject project : projects)
		{
			ICProject cProject = CoreModel.getDefault().create(project);
			if (cProject == null)
				continue;

			for (ICElement cElement : cProject.getChildren())
			{
				{
					SourceRoot sourceRoot = (SourceRoot) cElement;
					for (ICElement cElementDirs : sourceRoot.getChildren())
					{
						if (cElementDirs.getElementType() == ICElement.C_CCONTAINER)
						{
							processContainer((ICContainer) cElementDirs);
						}
					}
				}
			}
		}
	}

	private void processContainer(ICContainer container) throws Exception
	{
		for (ICElement cElement : container.getChildren())
		{
			if (cElement.getElementType() == ICElement.C_CCONTAINER)
			{
				processContainer((ICContainer) cElement);
			}
			else if (cElement.getElementType() == ICElement.C_UNIT)
			{
				processTranslationUnit((ITranslationUnit) cElement);
			}
		}
	}

	private void processTranslationUnit(ITranslationUnit tu) throws Exception
	{
		IASTTranslationUnit ast = tu.getAST();
		if (ast != null)
		{
			ast.accept(new ASTVisitor()
			{
				{
					shouldVisitDeclarations = true;
					shouldVisitDeclarators = true;
				}

				public int visit(IASTDeclaration declaration)
				{
					if (declaration instanceof IASTFunctionDefinition)
					{
						IASTFunctionDefinition functionDef = (IASTFunctionDefinition) declaration;
						IASTFunctionDeclarator declarator = functionDef.getDeclarator();
						String functionName = declarator.getName().toString();

						Logger.log(functionDef.getFileLocation().getFileName());
						Logger.log(functionDef.getRawSignature());
						Logger.log(functionName);
						String fileHeaders = StringUtil.EMPTY;
						IBinding binding = functionDef.getDeclarator().getName().resolveBinding();
						try
						{
							if (binding instanceof IFunction)
							{
								try
								{
									IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
//									index.acquireReadLock();
									IIndexName[] indexNames = index.findNames(binding, IIndex.FIND_DECLARATIONS);
									List<URI> headerUris = new ArrayList<>();
									for (IIndexName indexName : indexNames)
									{
										headerUris.add(indexName.getFile().getLocation().getURI());
									}
									index.releaseReadLock();
									fileHeaders = headerUris.isEmpty() ? StringUtil.EMPTY : headerUris.toString();
								}
								catch (NullPointerException e)
								{
									Logger.log("No Header Reference found");
									fileHeaders = StringUtil.EMPTY;
								}
							}
							if (!StringUtil.isEmpty(fileHeaders))
							{
								IndexerVO indexerVO = new IndexerVO(functionName, functionDef.getRawSignature(), functionDef.getFileLocation().getFileName(), fileHeaders, null);
								if (!IndexerDbOps.getIndexerDbOps().recordExists(indexerVO))
								{
									gptQueryManager.addToQueue(indexerVO);								
								}
							}
						}
						catch (Exception e)
						{
							Logger.log(e);
						}
					}
					return PROCESS_CONTINUE;
				}
			});
		}
	}

}
