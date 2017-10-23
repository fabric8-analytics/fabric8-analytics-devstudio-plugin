/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.osgi.framework.Bundle;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

public class Fabric8AnalyticsStreamConnectionProvider extends ProcessStreamConnectionProvider
implements StreamConnectionProvider {

	public static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	private static final String RECOMMENDER_API_URL = "RECOMMENDER_API_URL";
	
	private CheckTokenJob job;
	
	private String token;

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
		addPreferencesListener();
	}

	@Override
	public void start() throws IOException {
		if (!Fabric8AnalysisPreferences.getInstance().isLSPServerEnabled()) {
			throw new IOException("Fabric8 analyses server is not enabled");
		}
		
		token = TokenCheck.getInstance().getToken();
		if (token == null) {
			Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
			MessageDialogUtils.displayInfoMessage("Cannot start Fabric8 analyses server because login into OpenShift.io failed. The analyses is now disabled. You can enable it in Preferences");
			throw new IOException("Cannot get OpenShift.io token");
		}
		
		CheckTokenJob job = new CheckTokenJob(this, token);
		job.schedule();
		super.start();
		Fabric8AnalysisLSUIActivator.getDefault().logInfo("The Fabric8 analyses server is started");
	}
	
	@Override
	public void stop() {
		if (job != null) {
			job.cancel();
		}
		super.stop();
		Fabric8AnalysisLSUIActivator.getDefault().logInfo("The Fabric8 analyses server is stopped");
	}
	
	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder res = super.createProcessBuilder();
		res.environment().put(RECOMMENDER_API_TOKEN, token);
		res.environment().put(RECOMMENDER_API_URL, RecommenderAPIProvider.SERVER_URL);
		return res;
	}
	
	private void addPreferencesListener() {
		IPreferenceStore preferenceStore = Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (Fabric8AnalysisPreferences.LSP_SERVER_ENABLED.equals(event.getProperty())) {
					if (Fabric8AnalysisPreferences.getInstance().isLSPServerEnabled()) {
						try {
							start();
						} catch (IOException e) {
							MessageDialogUtils.displayErrorMessage("Failed to start Fabric8 analyses server", e);
						}
						
					} else {
						stop();	
					}
				}
			}
		});
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
		
		String message = "`node` is missing in your PATH, Fabric8 Fabric8 analyses server won't work.\n" +
				"Please install `node` and make it available in your PATH";
		
		MessageDialogUtils.displayErrorMessage(message);
		return null;
	}

	private static File getServerLocation() {
		try {
			Bundle bundle = Platform.getBundle(Fabric8AnalysisLSUIActivator.PLUGIN_ID);
			return new File(FileLocator.getBundleFile(bundle), "/server/fabric8-analytics-lsp-server-master/output/server.js");
		} catch (IOException e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Cannot find the Fabric8 analyses server location", e);
			return null;
		}
	}
	
	public String getToken() {
		return token;
	}
}
