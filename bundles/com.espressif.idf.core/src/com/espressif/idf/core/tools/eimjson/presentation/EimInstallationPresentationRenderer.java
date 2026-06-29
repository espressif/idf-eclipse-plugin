/*******************************************************************************
 * Copyright 2026 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.tools.eimjson.presentation;

import com.espressif.idf.core.tools.Messages;
import com.espressif.idf.core.tools.eimjson.InstallationStatus;
import com.espressif.idf.core.tools.eimjson.model.EimInstallationModel;

/**
 * Builds {@link EimInstallationPresentation} for a table row from the installation's
 * {@link InstallationStatus}. Schema-neutral: older files report
 * {@link InstallationStatus#FINISHED} and newer files add lifecycle statuses, both mapped here.
 */
public final class EimInstallationPresentationRenderer
{
	private EimInstallationPresentationRenderer()
	{
	}

	public static EimInstallationPresentation render(EimInstallationModel installation, boolean isActive,
			boolean isSettingUp)
	{
		if (isSettingUp)
		{
			return new EimInstallationPresentation(EimInstallationPresentation.StatusKind.SETTING_UP,
					Messages.EimInstallationStatusSettingUp, false, false);
		}

		return switch (installation.getStatus())
		{
			case FINISHED -> isActive
					? new EimInstallationPresentation(EimInstallationPresentation.StatusKind.ACTIVE,
							Messages.EimInstallationStatusActive, false, true)
					: new EimInstallationPresentation(EimInstallationPresentation.StatusKind.INACTIVE, "", true, false); //$NON-NLS-1$
			case IN_PROGRESS -> new EimInstallationPresentation(EimInstallationPresentation.StatusKind.IN_PROGRESS,
					Messages.EimInstallationStatusInProgress, false, false);
			case FAILED -> new EimInstallationPresentation(EimInstallationPresentation.StatusKind.FAILED,
					Messages.EimInstallationStatusFailed, false, false);
			case BEING_REPAIRED -> new EimInstallationPresentation(EimInstallationPresentation.StatusKind.BEING_REPAIRED,
					Messages.EimInstallationStatusBeingRepaired, false, false);
			case BROKEN -> new EimInstallationPresentation(EimInstallationPresentation.StatusKind.BROKEN,
					Messages.EimInstallationStatusBroken, false, false);
		};
	}
}
