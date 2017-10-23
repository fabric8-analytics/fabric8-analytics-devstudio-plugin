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

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * Checks if the token has changed since the server started
 * 
 * @author ljelinko
 *
 */
public class CheckTokenJob extends Job {

	private Fabric8AnalyticsStreamConnectionProvider provider;
	
	public CheckTokenJob(Fabric8AnalyticsStreamConnectionProvider provider, String token) {
		super("Check OSIO token");
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
			displayInfoMessage("The OSIO token has changed since the Fabric8 LSP server has started. The server is now stopped. You can enable it again via Preferences");	
			return Status.OK_STATUS;
		}
		
		if (!token.equals(tmpToken)) {
			try {
				provider.stop();
				provider.start();
			} catch (IOException e) {
				// TODO
			}
			return Status.OK_STATUS;			
		}
		
		schedule(Fabric8AnalysisPreferences.getInstance().getTokenCheckIntervalMiliseconds());
		return Status.OK_STATUS;
	}
	
	private void displayInfoMessage(String message) {
		Fabric8AnalysisLSUIActivator.getDefault().logInfo(message);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "INFO", message);
			}
		});
	}
}
