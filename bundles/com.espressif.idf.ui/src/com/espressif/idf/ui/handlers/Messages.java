package com.espressif.idf.ui.handlers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.espressif.idf.ui.handlers.messages"; //$NON-NLS-1$
	public static String NewProjectHandler_CouldntFindPath;
	public static String NewProjectHandler_CouldntFindIdfPath;
	public static String NewProjectHandler_CouldntFindTools;
	public static String NewProjectHandler_MandatoryMsg;
	public static String NewProjectHandler_NavigateToHelpMenu;
	public static String NewProjectHandler_PathErrorTitle;
	public static String LanguageChange_ErrorTitle;
	public static String LanguageChange_ErrorMessage;
	public static String ProjectCleanCommandHandler_RunningProjectCleanJobName;
	public static String ProjectFullCleanCommandHandler_RunningFullcleanJobName;
	public static String PythonCleanCommandHandler_RunningPythonCleanJobName;
	public static String StopLaunchBuildHandler_0;
	public static String UpdateEspIdfCommand_JobMsg;
	public static String UpdateEspIdfCommand_InstallToolsJobMsg;
	public static String UpdateEspIdfCommand_SuggestToOpenInstallToolsWizard;
	public static String MissingDebugConfigurationTitle;
	public static String DebugConfigurationNotFoundMsg;

	public static String RunActionHandler_NoProjectQuestionText;
	public static String RunActionHandler_NoProjectQuestionTitle;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
