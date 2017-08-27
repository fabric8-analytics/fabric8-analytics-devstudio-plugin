package com.redhat.fabric8analytics.lsp.eclipse;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;

public class TokenCheck {

	static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	public static String getToken()
	{
		String token = "";
		token = Fabric8AnalysisLSActivator.getDefault().getPreferenceStore().getString(RECOMMENDER_API_TOKEN);
		return token;
	}


	public static void checkToken() {

		new UIJob(PlatformUI.getWorkbench().getDisplay(), "Incorrect RECOMMENDER_API_TOKEN") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog.openError(getDisplay().getActiveShell(), "Incorrect RECOMMENDER_API_TOKEN",
						"You need to set the RECOMMENDER_API_TOKEN to the API token of your OpenShift.io account for Fabric8 analysis to work.\n" +
						"You're going to be redirected to the Preference page for that.");
				PreferencesUtil.createPreferenceDialogOn(getDisplay().getActiveShell(),
						Fabric8AnalysisPreferencePage.PREFERENCE_PAGE_ID,
						null, null).open();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

}
