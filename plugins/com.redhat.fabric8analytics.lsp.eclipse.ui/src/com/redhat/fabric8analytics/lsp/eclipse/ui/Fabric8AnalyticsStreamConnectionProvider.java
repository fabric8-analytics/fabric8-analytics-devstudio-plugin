package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

public class Fabric8AnalyticsStreamConnectionProvider extends ProcessStreamConnectionProvider
implements StreamConnectionProvider {

	public static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	private static final String RECOMMENDER_API_URL = "RECOMMENDER_API_URL";

	public Fabric8AnalyticsStreamConnectionProvider() {
		super();

		File nodeJsLocation = getNodeJsLocation();
		if (nodeJsLocation == null) {
			return;
		}

		File serverLocation = getServerLocation();
		if (serverLocation == null) {
			return;
		}

		setCommands(Arrays.asList(new String[] {
				nodeJsLocation.getAbsolutePath(),
				serverLocation.getAbsolutePath(),
				"--stdio"
		}));

		setWorkingDirectory(System.getProperty("user.dir"));
	}
	
	@Override
	public void start() throws IOException {
		String token = TokenCheck.getInstance().getToken();
		if (token == null) {
			throw new IOException("Cannot get token from OSIO plugin");
		}
		super.start();
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder res = super.createProcessBuilder();
		res.environment().put(RECOMMENDER_API_TOKEN, TokenCheck.getInstance().getToken());
		res.environment().put(RECOMMENDER_API_URL, RecommenderAPIProvider.SERVER_URL);
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
			// no problem, try default install path
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
		
		String message = "`node` is missing in your PATH, Fabric8 LSP server won't work.\n" +
				"Please install `node` and make it available in your PATH";
		
		Fabric8AnalysisLSUIActivator.getDefault().logError(message);
		MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Missing `node` in PATH", message);
		return null;
	}

	private static File getServerLocation() {
		try {
			Bundle bundle = Platform.getBundle(Fabric8AnalysisLSUIActivator.PLUGIN_ID);
			return new File(FileLocator.getBundleFile(bundle), "/server/fabric8-analytics-lsp-server-master/output/server.js");
		} catch (IOException e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Cannot find the LSP server location", e);
			return null;
		}
	}
}
