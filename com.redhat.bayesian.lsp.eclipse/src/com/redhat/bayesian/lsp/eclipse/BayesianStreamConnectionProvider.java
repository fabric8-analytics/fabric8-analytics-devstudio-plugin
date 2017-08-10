package com.redhat.bayesian.lsp.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;

public class BayesianStreamConnectionProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {

	static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";
	static String token;

	public BayesianStreamConnectionProvider() {
		super();
		try {
			setCommands(Arrays.asList(new String[] {
					getNodeJsLocation().getAbsolutePath(),
					FileLocator.toFileURL(BayesianStreamConnectionProvider.class.getResource("/fabric8-analytics-lsp-server-test-devstudio/output/server.js")).getPath(),
					"--stdio"
			}));
		} catch (IOException e) {
			e.printStackTrace();
		}
		setWorkingDirectory(System.getProperty("user.dir"));
		checkPreferences();
	}

	private void checkPreferences() {
		// eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwbEwwdlhzOVlSVnFaTW93eXc4dU5MUl95cjBpRmFvemRRazlyenEyT1ZVIn0.eyJqdGkiOiI2ODBkZDJiYi05YTI0LTRmYWMtODg3MS0wZjk1MTRhYThhYjUiLCJleHAiOjE0OTU3NTQ0NTEsIm5iZiI6MCwiaWF0IjoxNDkzMTYyNDUxLCJpc3MiOiJodHRwczovL3Nzby5vcGVuc2hpZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1ZCI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3ViIjoiYmRlNGRjZGQtOTMzYS00NjFmLTk0NGMtYTM0OGY2MmY4ZDRiIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX3RpbWUiOjE0OTMxNjIzNzYsInNlc3Npb25fc3RhdGUiOiJjZjU0MjFlYi1hZGY2LTRkYmQtYTg0NC1jZTgxYTJiNDc5YmQiLCJuYW1lIjoia3Jpc2huYSBwYXBhcmFqdSIsImdpdmVuX25hbWUiOiJrcmlzaG5hIiwiZmFtaWx5X25hbWUiOiJwYXBhcmFqdSIsInByZWZlcnJlZF91c2VybmFtZSI6InNwYXBhcmFqIiwiZW1haWwiOiJzcGFwYXJhakByZWRoYXQuY29tIiwiYWNyIjoiMSIsImNsaWVudF9zZXNzaW9uIjoiYzAwNTBkZTItM2ZhOS00N2M0LWIxZDAtMWRjZmUyNGQwMmM0IiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYnJva2VyIjp7InJvbGVzIjpbInJlYWQtdG9rZW4iXX0sImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sImF1dGhvcml6YXRpb24iOnsicGVybWlzc2lvbnMiOlt7InNjb3BlcyI6WyJyZWFkOnNwYWNlIiwiYWRtaW46c3BhY2UiXSwicmVzb3VyY2Vfc2V0X2lkIjoiOGI0NzM2ODMtYmRiMi00NmUyLWFkODQtYTliOGZkZDIzNGVmIiwicmVzb3VyY2Vfc2V0X25hbWUiOiI0YWE5ZjU0OS01YzU4LTRhMzQtYTRjMy0xYTdhOGJjMTkwODcifSx7InNjb3BlcyI6WyJyZWFkOnNwYWNlIiwiYWRtaW46c3BhY2UiXSwicmVzb3VyY2Vfc2V0X2lkIjoiZTM3NTRhNDgtOGI2MC00N2NhLTg1MGItYWNiMDRmOTM4YzQ0IiwicmVzb3VyY2Vfc2V0X25hbWUiOiI4YjAzZGI4ZC03ZDVjLTQ3ZTAtYWY3Mi05YzM4NjdlODI5ZWIifSx7InNjb3BlcyI6WyJyZWFkOnNwYWNlIiwiYWRtaW46c3BhY2UiXSwicmVzb3VyY2Vfc2V0X2lkIjoiNGMwODc2Y2EtM2ZlZC00ZmQ1LWIyZTUtNGRkOWZkNjkyMWNjIiwicmVzb3VyY2Vfc2V0X25hbWUiOiIyMmFiOGU2My1kMWIwLTRhZTctOTMxZS1iNjhmNzg3ZDQ1YTcifV19LCJjb21wYW55IjoiUmVkaGF0In0.gQ1YgPw3rVoGhPYWwCJDJFswzwZak8kYQArs1fb7k5zTAqjPnNS1LF0DPzmHnLd9533lKX4XpOgGaM4xmQc4CyrFMpQb8aNfO3xTqDsAZ74O51FKsnXEgctnmZIqRo8j1MlYjrLODQ4CMWqbGBIl3D9tXnR_IyyOA4hY6osFy-hFnyC68xcaYRPRH1v4L9aV6JaXHK1lnE9XGCzbb3a5wwUlRJWno3NxDBiAuKbs3uagpw_Gln8LIcDiefCueFdSVIeKIvqfgGpsYdz_SKojZxjFTZNkcjGRl5Sk-EihEM2LfOiJeLi4WmjNFmL8jx4NZphq7Wfkwfl3ue3YwmMgtw
		IPreferenceStore preferenceStore = Fabric8AnalysisLSActivator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (RECOMMENDER_API_TOKEN.equals(event.getProperty())) {
					token = (String)event.getNewValue();
					stop();
				}
			}
		});
		token = Fabric8AnalysisLSActivator.getDefault().getPreferenceStore().getString(RECOMMENDER_API_TOKEN);
		//token = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwbEwwdlhzOVlSVnFaTW93eXc4dU5MUl95cjBpRmFvemRRazlyenEyT1ZVIn0.eyJqdGkiOiI5YmI1YzAyZi02Yzk2LTQyNGQtOTkyYi1lMzU1N2IxMDYwYTMiLCJleHAiOjE1MzAzNDc2ODIsIm5iZiI6MCwiaWF0IjoxNDk4ODExNjgyLCJpc3MiOiJodHRwczovL3Nzby5vcGVuc2hpZnQuaW8vYXV0aC9yZWFsbXMvZmFicmljOCIsImF1ZCI6ImZhYnJpYzgtb25saW5lLXBsYXRmb3JtIiwic3ViIjoiNGU3YzY5MjEtZDJjYi00MjMyLWJjYjQtMjQzZGFiNjNiZDJkIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiZmFicmljOC1vbmxpbmUtcGxhdGZvcm0iLCJhdXRoX3RpbWUiOjAsInNlc3Npb25fc3RhdGUiOiI3ZWQ3NzFhZS1iOGU5LTQxZjgtOWUzNi02MDNiYjRjZjFkYmEiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbIioiXSwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJicm9rZXIiOnsicm9sZXMiOlsicmVhZC10b2tlbiJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwibmFtZSI6IiIsInByZWZlcnJlZF91c2VybmFtZSI6ImJheWVzaWFuLWFwaSJ9.HGn9MJcBv0xlkoCSdg3zATswQZuHPGs2ur4f4k93m23BGjQqteuEvvy8AuxL4utTWehO5phzYzGxb2l4jtV_prMkdGh8K3Lrld6KIlTfG9lfaBZQk8SdrLo27WhJOK1Wf72hLbyGzez5FiWZkY1sbDuAzryBE6igDQq3daHK3BAn8jZluQYFi3_T8CoJP1BUaxytINKikQKthPjc9U1WmVR5GglnKxMzAq6Ig6JzU51VtngoVcCOoP-jfz-VcGoOuXzzZ-qqoWbqu9X42awyJvLJN5fSCDnVDn1Ept2JW_U7ros_2WzJRLvOMuLM1B8zr1gNeJloUXPySNWDHLgZgg";
		if (token == null || token.isEmpty()) {
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

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder res = super.createProcessBuilder();
		res.environment().put(RECOMMENDER_API_TOKEN, token);
		res.environment().put("RECOMMENDER_API_URL", "https://recommender.api.openshift.io/api/v1");
		return res;
	}

	private static File getNodeJsLocation() {
		String location = null;
		String[] command = new String[] {"/bin/bash", "-c", "which node"};
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] {"cmd", "/c", "where node"};
		}
		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			location = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}

		// Try default install path as last resort
		if (location == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			location = "/usr/local/bin/node";
		}

		if (Files.exists(Paths.get(location))) {
			return new File(location);
		}
		new UIJob(PlatformUI.getWorkbench().getDisplay(), "Missing `node` in PATH") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				MessageDialog.openError(getDisplay().getActiveShell(), "Missing Node.js",
						"`node` is missing in your PATH, C# editor won't work fully.\n" +
						"Please install `node` and make it available in your PATH");
				return Status.OK_STATUS;
			}
		}.schedule();
		return null;
	}

}
