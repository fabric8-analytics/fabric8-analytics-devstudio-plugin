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


		//token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwbEwwdlhzOVlSVnFaTW93eXc4dU5MUl95cjBpRmFvemRRazlyenEyT1ZVIn0.eyJqdGkiOiI5YmI1YzAyZi02Yzk2LTQyNGQtOTkyYi1lMzU1N2IxMDYwYTMiLCJleHAiOjE1MzAzNDc2ODIsIm5iZiI6MCwiaWF0IjoxNDk4ODExNjgyLCJpc3MiOiJodHRwczovL3Nzby5vcGVuc2hpZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1ZCI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3ViIjoiNGU3YzY5MjEtZDJjYi00MjMyLWJjYjQtMjQzZGFiNjNiZDJkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI3ZWQ3NzFhZS1iOGU5LTQxZjgtOWUzNi02MDNiYjRjZjFkYmEiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJicm9rZXIiOnsicm9sZXMiOlsicmVhZC10b2tlbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IiIsInByZWZlcnJlZF91c2VybmFtZSI6ImJheWVzaWFuLWFwaSJ9.HGn9MJcBv0xlkoCSdg3zATswQZuHPGs2ur4f4k93m23BGjQqteuEvvy8AuxL4utTWehO5phzYzGxb2l4jtV_prMkdGh8K3Lrld6KIlTfG9lfaBZQk8SdrLo27WhJOK1Wf72hLbyGzez5FiWZkY1sbDuAzryBE6igDQq3daHK3BAn8jZluQYFi3_T8CoJP1BUaxytINKikQKthPjc9U1WmVR5GglnKxMzAq6Ig6JzU51VtngoVcCOoP-jfz-VcGoOuXzzZ-qqoWbqu9X42awyJvLJN5fSCDnVDn1Ept2JW_U7ros_2WzJRLvOMuLM1B8zr1gNeJloUXPySNWDHLgZgg";
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
