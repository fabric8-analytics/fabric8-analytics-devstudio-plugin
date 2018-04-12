/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package com.redhat.fabric8analytics.eclipse.lsp.ui;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;
import com.redhat.fabric8analytics.eclipse.lsp.core.Fabric8AnalysisLSPPreferences;
import com.redhat.fabric8analytics.eclipse.ui.OSIOService;
import com.redhat.fabric8analytics.eclipse.ui.ServiceEnablementException;

public class LSPService implements OSIOService{

	@Override
	public String getServiceName() {
		return "&Fabric8 Analytics LSP Server";
	}

	@Override
	public void enable(boolean enable) throws ServiceEnablementException{
		if(enable) {
			try {
				AnalyticsAuthService.getInstance().login(new NullProgressMonitor());
			} catch (Exception e) {
				throw new ServiceEnablementException("Error while logging to openshift.io", e);
			}
		}
		Fabric8AnalysisLSPPreferences.getInstance().setLSPServerEnabled(enable);
	}
	
	@Override
	public boolean isEnabled() {
		return Fabric8AnalysisLSPPreferences.getInstance().isLSPServerEnabled();
	}

}
