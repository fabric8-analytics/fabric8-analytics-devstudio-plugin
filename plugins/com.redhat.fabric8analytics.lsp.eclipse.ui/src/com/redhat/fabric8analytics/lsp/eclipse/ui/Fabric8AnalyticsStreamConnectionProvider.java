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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
		File serverLocation = getServerLocation();
		if (serverLocation == null) {
			return;
		}

		setCommands(Arrays.asList(new String[] {
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
			res.environment().put(RECOMMENDER_API_TOKEN, token);
			res.environment().put(RECOMMENDER_API_URL, serverUrl);
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
}
