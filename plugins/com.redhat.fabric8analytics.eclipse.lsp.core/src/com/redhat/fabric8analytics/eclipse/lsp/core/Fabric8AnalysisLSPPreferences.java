/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc.
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

public class Fabric8AnalysisLSPPreferences {
	
	public static final String LSP_SERVER_ENABLED = "fabric8AnalysisPreferences.LSP_ENABLED";
	
	private static Fabric8AnalysisLSPPreferences instance;
	
	private Fabric8AnalysisLSPPreferences() {
	}
	
	public static Fabric8AnalysisLSPPreferences getInstance() {
		if(instance == null) {
			instance = new Fabric8AnalysisLSPPreferences();
		}
		return instance;
	}
	
	public boolean isLSPServerEnabled() {
		return InstanceScope.INSTANCE.getNode(Fabric8AnalysisLSPCoreActivator.PLUGIN_ID).getBoolean(LSP_SERVER_ENABLED, false);
	}
	
	public void setLSPServerEnabled(boolean enabled) {
		InstanceScope.INSTANCE.getNode(Fabric8AnalysisLSPCoreActivator.PLUGIN_ID).putBoolean(LSP_SERVER_ENABLED, enabled);
	}

}
