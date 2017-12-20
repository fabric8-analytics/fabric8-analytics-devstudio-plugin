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
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.osgi.framework.Bundle;

import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;

public class Fabric8AnalyticsStreamConnectionProvider extends ProcessStreamConnectionProvider
implements StreamConnectionProvider {

	public static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	private static final String RECOMMENDER_API_URL = "RECOMMENDER_API_URL";

	private String token;

	private String serverUrl;

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
			stop();
			return;
		}
		super.start();
		// if super.start() does not throw exception, we're started
		Fabric8AnalysisLSUIActivator.getDefault().logInfo("The Fabric8 analyses server is started ");
	}

	@Override
	public void stop() {
		super.stop();
		Fabric8AnalysisLSUIActivator.getDefault().logInfo("The Fabric8 analyses server is stopped");
	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder res = super.createProcessBuilder();
		try {
			token = Fabric8AnalysisPreferences.getInstance().getToken();
			serverUrl = Fabric8AnalysisPreferences.getInstance().getProdURL();
			String [] arrOfStr = serverUrl.split("http", 2);
			serverUrl = "https" + arrOfStr[1];
			String temp_server_url = "https://recommender.api.openshift.io/api/v1";
			res.environment().put(RECOMMENDER_API_TOKEN, token);
			//			res.environment().put(RECOMMENDER_API_URL, serverUrl);
			res.environment().put(RECOMMENDER_API_URL, temp_server_url);
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private static File getServerLocation() {
		try {
			Bundle bundle = Platform.getBundle(Fabric8AnalysisLSUIActivator.PLUGIN_ID);
			return new File(FileLocator.getBundleFile(bundle), "ca-lsp-server-0.0.6-SNAPSHOT/server.js");
		} catch (IOException e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Cannot find the Fabric8 analyses server location", e);
			return null;
		}
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
}
