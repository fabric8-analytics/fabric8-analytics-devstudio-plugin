package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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
//import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;

import com.redhat.fabric8analytics.lsp.eclipse.ui.TokenCheck;

public class Fabric8AnalyticsStreamConnectionProvider extends ProcessStreamConnectionProvider
implements StreamConnectionProvider {

	static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";
	static String token;

	public Fabric8AnalyticsStreamConnectionProvider() {
		super();
		File nodeJsLocation = getNodeJsLocation();
		
		if (nodeJsLocation == null) {
			return;
		}
		
		try {
			setCommands(Arrays.asList(new String[] {
					nodeJsLocation.getAbsolutePath(),
					Paths.get(FileLocator.toFileURL(Fabric8AnalyticsStreamConnectionProvider.class.getResource("/server/fabric8-analytics-lsp-server-test-devstudio/output/server.js")).toURI()).toString(),
					"--stdio"
			}));
		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		setWorkingDirectory(System.getProperty("user.dir"));
		checkPreferences();
	}

	private void checkPreferences() {
		IPreferenceStore preferenceStore = Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (RECOMMENDER_API_TOKEN.equals(event.getProperty())) {
					stop();
				}
			}
		});
		token = TokenCheck.get().getToken();
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

		if (location != null && Files.exists(Paths.get(location))) {
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
