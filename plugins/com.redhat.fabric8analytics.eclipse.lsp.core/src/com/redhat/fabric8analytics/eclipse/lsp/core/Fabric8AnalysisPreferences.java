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
package com.redhat.fabric8analytics.eclipse.lsp.core;

import org.eclipse.core.runtime.preferences.InstanceScope;

public class Fabric8AnalysisPreferences {

	public static final String LSP_SERVER_ENABLED = "fabric8AnalysisPreferences.LSP_ENABLED";

	private static Fabric8AnalysisPreferences instance;

	private Fabric8AnalysisPreferences() {
	}

	public static Fabric8AnalysisPreferences getInstance() {
		if(instance == null) {
			instance = new Fabric8AnalysisPreferences();
		}
		return instance;
	}

	public boolean isLSPServerEnabled() {
		return InstanceScope.INSTANCE.getNode(Fabric8AnalysisLSCoreActivator.PLUGIN_ID).getBoolean(LSP_SERVER_ENABLED, false);
	}

	public void setLSPServerEnabled(boolean enabled) {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisLSCoreActivator.PLUGIN_ID).putBoolean(LSP_SERVER_ENABLED, enabled);
	}
}