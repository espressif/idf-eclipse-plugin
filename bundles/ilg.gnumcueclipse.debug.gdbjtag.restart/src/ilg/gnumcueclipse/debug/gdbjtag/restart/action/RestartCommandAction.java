/*******************************************************************************
 * Copyright (c) 2014 Liviu Ionescu.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Ningareddy Modase - initial implementation.
 *     Liviu Ionescu - ARM version.
 *******************************************************************************/

package ilg.gnumcueclipse.debug.gdbjtag.restart.action;

import org.eclipse.debug.core.commands.IRestartHandler;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import ilg.gnumcueclipse.debug.gdbjtag.restart.Activator;

public class RestartCommandAction extends DebugCommandAction {

	// ------------------------------------------------------------------------

	public RestartCommandAction() {
		setActionDefinitionId(Activator.PLUGIN_ID + ".commands.Restart");
	}

	@Override
	public String getText() {
		return "Restart";
	}

	@Override
	public String getHelpContextId() {
		return "org.eclipse.debug.ui.disconnect_action_context";
	}

	@Override
	public String getId() {
		return "org.eclipse.debug.ui.debugview.toolbar.disconnect";
	}

	@Override
	public String getToolTipText() {
		return "Reset target and restart debugging";
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/dlcl16/restart_co.gif");
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/elcl16/restart_co.gif");
	}

	@Override
	protected Class<IRestartHandler> getCommandType() {
		return IRestartHandler.class;
	}

	// ------------------------------------------------------------------------
}