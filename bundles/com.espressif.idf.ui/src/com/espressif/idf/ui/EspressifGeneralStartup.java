/*******************************************************************************
 * Copyright 2025 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.ui;

import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.espressif.idf.core.build.Messages;
import com.espressif.idf.core.build.ReHintPair;
import com.espressif.idf.core.logging.Logger;
import com.espressif.idf.core.resources.OpenDialogListenerSupport;
import com.espressif.idf.core.resources.PopupDialog;
import com.espressif.idf.core.resources.ResourceChangeListener;
import com.espressif.idf.ui.dialogs.BuildView;
import com.espressif.idf.ui.dialogs.MessageLinkDialog;

/**
 * General Startup class for handling 
 * ui elements and registering any listeners
 * @author Ali Azam Rana <ali.azamrana@espressif.com>
 *
 */
@SuppressWarnings("restriction")
public class EspressifGeneralStartup implements IStartup
{
    private static final String BUILDHINTS_ID = "com.espressif.idf.ui.views.buildhints";

    private LaunchBarListener launchBarListener;

    @Override
    public void earlyStartup()
    {
        hookDialogListeners();
        hookLaunchBarListeners();
    }

    private void hookDialogListeners()
    {
        OpenDialogListenerSupport.getSupport().addPropertyChangeListener(evt -> {
            PopupDialog popupDialog = PopupDialog.valueOf(evt.getPropertyName());
            switch (popupDialog)
            {
                case LOW_PARTITION_SIZE:
                    openLowPartitionSizeDialog(evt);
                    break;
                case AVAILABLE_HINTS:
                    openAvailableHintsDialog(evt);
                    break;
                case DISABLE_LAUNCHABAR_EVENTS:
                    disableLaunchBarEvents();
                    break;
                case ENABLE_LAUNCHBAR_EVENTS:
                    enableLaunchBarEvents();
                    break;
                default:
                    break;
            }
        });
    }

    private void hookLaunchBarListeners()
    {
        launchBarListener = new LaunchBarListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(launchBarListener));

        ILaunchBarManager launchBarManager = Activator.getService(ILaunchBarManager.class);
        launchBarManager.addListener(launchBarListener);
    }

    @SuppressWarnings("static-access")
	private void disableLaunchBarEvents()
    {
        launchBarListener.setIgnoreTargetChange(true);
    }

    @SuppressWarnings("static-access")
	private void enableLaunchBarEvents()
    {
        launchBarListener.setIgnoreTargetChange(false);
    }

    @SuppressWarnings("unchecked")
    private void openAvailableHintsDialog(PropertyChangeEvent evt)
    {
        Display.getDefault().asyncExec(() -> {
            List<ReHintPair> errorHintPairs = (List<ReHintPair>) evt.getNewValue();

            if (errorHintPairs.isEmpty())
            {
                updateBuildView(errorHintPairs);
                return;
            }

            try
            {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(BUILDHINTS_ID);
            }
            catch (PartInitException e)
            {
                Logger.log(e);
            }

            updateBuildView(errorHintPairs);
        });
    }

    private void updateBuildView(List<ReHintPair> errorHintPairs)
    {
        BuildView view = (BuildView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .findView(BUILDHINTS_ID);
        if (view != null)
        {
            view.updateReHintsPairs(errorHintPairs);
        }
    }

    private void openLowPartitionSizeDialog(PropertyChangeEvent evt)
    {
        Display.getDefault().asyncExec(() -> {
            Shell shell = Display.getDefault().getActiveShell();
            MessageLinkDialog.openWarning(shell,
                    Messages.IncreasePartitionSizeTitle,
                    MessageFormat.format(Messages.IncreasePartitionSizeMessage,
                            evt.getNewValue(),
                            evt.getOldValue(),
                            "https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html?highlight=partitions%20csv#creating-custom-tables"));
        });
    }
}
