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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.redhat.fabric8analytics.lsp.eclipse.ui.Fabric8AnalyticsStreamConnectionProvider;

/**
 * Checks if the token has changed since the server started
 * 
 * @author ljelinko
 *
 */
public class CheckTokenJob extends Job {

	private Fabric8AnalyticsStreamConnectionProvider provider;
	
	public CheckTokenJob(Fabric8AnalyticsStreamConnectionProvider provider, String token) {
		super("Check OpenShift.io token");
		if (provider == null || token == null) {
			throw new IllegalArgumentException("Token or provider was null");
		}
		this.provider = provider;
	}

	@Override
	protected IStatus run(IProgressMonitor arg0) {
		String token = provider.getToken();
		if (token == null) {
			return Status.OK_STATUS;
		}

		String tmpToken = TokenCheck.getInstance().getToken();
		
		if (tmpToken == null) {
			provider.stop();
			Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
			MessageDialogUtils.displayInfoMessage("The OpenShift.io token has changed since the Fabric8 analyses server had started. The server is now stopped. You can enable it again via Preferences");
			return Status.OK_STATUS;
		}
		
		if (!token.equals(tmpToken)) {
			try {
				provider.stop();
				provider.start();
			} catch (IOException e) {
				Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
				MessageDialogUtils.displayInfoMessage("The OpenShift.io token has changed since the Fabric8 analyses server had started. The server is now stopped. You can enable it again via Preferences");
			}
			return Status.OK_STATUS;			
		}
		
		schedule(Fabric8AnalysisPreferences.getInstance().getTokenCheckIntervalMiliseconds());
		return Status.OK_STATUS;
	}
}
