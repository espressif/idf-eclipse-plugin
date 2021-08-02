/*******************************************************************************
 * Copyright 2021 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.ui.tracing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.nebula.widgets.xviewer.customize.XViewerCustomMenu;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.logging.Logger;

/**
 * Custom context menu class for the details view to show the callers view option
 * 
 * @author Ali Azam Rana
 *
 */
public class TracingViewerCustomMenu extends XViewerCustomMenu
{
	private Action showCallStackAction;
	private TracingJsonParser tracingJsonParser;

	public TracingViewerCustomMenu(TracingJsonParser tracingJsonParser)
	{
		this.tracingJsonParser = tracingJsonParser;
	}

	@Override
	protected void setupActions()
	{
		super.setupActions();
		showCallStackAction = new ShowCallStackAction();
	}

	@Override
	public void setupMenuForTable(MenuManager menuManager)
	{
		menuManager.add(showCallStackAction);
		menuManager.add(new Separator());
		super.setupMenuForTable(menuManager);
	}

	private class ShowCallStackAction extends Action
	{

		private ShowCallStackAction()
		{
			super(Messages.TracingAnalysisEditor_DetailsContextMenuShowCallers);
		}

		@Override
		public void run()
		{
			TreeItem[] treeItems = xViewer.getTree().getSelection();
			DetailsVO detailsVO = (DetailsVO) treeItems[0].getData();
			List<AddressInfoVO> addressInfoVOs = new ArrayList<AddressInfoVO>();
			for (String callerAdderess : detailsVO.getEventsVO().getCallersAddressList())
			{
				AddressInfoVO addressInfoVO = tracingJsonParser.getCallersAddressMap().get(callerAdderess);
				if (addressInfoVO != null)
				{
					addressInfoVOs.add(addressInfoVO);
				}
			}

			try
			{
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TracingCallersView.ID);
				TracingCallersView tracingCallersView = (TracingCallersView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().findView(TracingCallersView.ID);
				tracingCallersView.setAddressInfoVOs(addressInfoVOs);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(TracingCallersView.ID);
			}
			catch (Exception e)
			{
				Logger.log(e);
			}
		}

		@Override
		public String getToolTipText()
		{
			return Messages.TracingAnalysisEditor_DetailsContextMenuShowCallersTooltip;
		}
	}
}
