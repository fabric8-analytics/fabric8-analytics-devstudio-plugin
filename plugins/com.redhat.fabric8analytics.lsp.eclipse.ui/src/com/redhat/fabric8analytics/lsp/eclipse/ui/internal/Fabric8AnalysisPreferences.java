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

public class Fabric8AnalysisPreferences {

	public static final String LSP_SERVER_ENABLED = "Fabric8AnalysisPreferences.LSP_SERVER_ENABLED";
	
	public static final String PROD_URL = "Fabric8AnalysisPreferences.PROD_URL";
	
	public static final String STAGE_URL = "Fabric8AnalysisPreferences.STAGE_URL";
	
	public static final String USER_KEY = "Fabric8AnalysisPreferences.USER_KEY";

	private static final Fabric8AnalysisPreferences INSTANCE = new Fabric8AnalysisPreferences();

	private Fabric8AnalysisPreferences() {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_ENABLED, true);
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(PROD_URL, "");
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
	public boolean isProdURLSet() {
		return (!(Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getString(PROD_URL)==null));
	}
	
	public void setProdURL(String prodURL) {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setValue(PROD_URL, prodURL);
	}
	
	public String getProdURL() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getString(PROD_URL);
	}
	public void setUserKey(String userKey) {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setValue(USER_KEY, userKey);
	}
	
	public String getUserKey() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getString(USER_KEY);
	}
}
