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

import java.util.concurrent.TimeUnit;

import com.redhat.fabric8analytics.lsp.eclipse.ui.Fabric8AnalysisLSUIActivator;

public class Fabric8AnalysisPreferences {

	public static final String LSP_SERVER_ENABLED = "Fabric8AnalysisPreferences.LSP_SERVER_ENABLED";

	// the value is stored in minutes
	public static final String LSP_SERVER_TOKEN_CHECK_INTERVAL = "Fabric8AnalysisPreferences.LSP_SERVER_TOKEN_CHECK_INTERVAL";
	
	// set interval to 1 hour
	private static final int MINUTES_INTERVAL = 60;

	private static final long CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(MINUTES_INTERVAL);

	private static final Fabric8AnalysisPreferences INSTANCE = new Fabric8AnalysisPreferences();

	private Fabric8AnalysisPreferences() {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_ENABLED, true);
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_TOKEN_CHECK_INTERVAL, CHECK_INTERVAL);
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
	
	public long getTokenCheckInterval() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getLong(LSP_SERVER_TOKEN_CHECK_INTERVAL);
	}
	
	public long getTokenCheckIntervalMiliseconds() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getLong(LSP_SERVER_TOKEN_CHECK_INTERVAL) * 60 * 1000;
	}
}
