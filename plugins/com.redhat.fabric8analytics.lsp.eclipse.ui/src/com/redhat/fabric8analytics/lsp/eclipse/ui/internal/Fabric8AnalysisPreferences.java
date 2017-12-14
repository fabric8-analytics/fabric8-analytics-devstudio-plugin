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

package com.redhat.fabric8analytics.lsp.eclipse.ui.internal;

import com.redhat.fabric8analytics.lsp.eclipse.ui.Fabric8AnalysisLSUIActivator;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public class Fabric8AnalysisPreferences {

	public static final String LSP_SERVER_ENABLED = "Fabric8AnalysisPreferences.LSP_SERVER_ENABLED";

	public static final String PROD_URL = "Fabric8AnalysisPreferences.PROD_URL";

	public static final String STAGE_URL = "Fabric8AnalysisPreferences.STAGE_URL";

	public static final String USER_KEY = "Fabric8AnalysisPreferences.USER_KEY";

	private static final Fabric8AnalysisPreferences INSTANCE = new Fabric8AnalysisPreferences();

	private static ISecurePreferences preferenceNode;

	private Fabric8AnalysisPreferences() {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_ENABLED, true);
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		preferenceNode = preferences.node("secured routes and keys");
	}

	public static Fabric8AnalysisPreferences getInstance() {
		return INSTANCE;
	}

	public boolean isLSPServerEnabled() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getBoolean(LSP_SERVER_ENABLED);
	}

	public void setLSPServerEnabled(boolean enabled) {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setValue(LSP_SERVER_ENABLED, enabled);
	}

	public void setProdURL(String prodURL) throws StorageException {

		preferenceNode.put(PROD_URL, prodURL, true);
	}

	public String getProdURL() throws StorageException {
		return preferenceNode.get(PROD_URL, null);
	}
	public void setStageURL(String stageURL) throws StorageException {

		preferenceNode.put(STAGE_URL, stageURL, true);
	}

	public String getStageURL() throws StorageException {
		return preferenceNode.get(STAGE_URL, null);
	}
	public void setUserKey(String userKey) throws StorageException {

		preferenceNode.put(USER_KEY, userKey, true);
	}
	public String getUserKey() throws StorageException {
		return preferenceNode.get(USER_KEY, null);
	}
}
