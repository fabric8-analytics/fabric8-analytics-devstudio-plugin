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

package com.redhat.fabric8analytics.eclipse.ui.internal.auth;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.redhat.fabric8analytics.eclipse.ui.Fabric8AnalysisUIActivator;
import com.redhat.fabric8analytics.eclipse.ui.OSIOLoginListener;
import com.redhat.fabric8analytics.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.job.LoginToOSIOJob;

/**
 * Handler for menu item "Enable Fabric8 Analyses".
 *
 * @author Geetika Batra
 *
 */
public class AuthorizeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LoginToOSIOJob authJob = new LoginToOSIOJob();

		authJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void done(IJobChangeEvent event) {
				AnalyticsAuthData analyticsAuthData = authJob.getAnalyticsAuthData();
				if (analyticsAuthData == null) {
					List<OSIOLoginListener> listeners = Fabric8AnalysisUIActivator.getDefault().getOSIOLoginListeners();
					if (listeners != null) {
						listeners.stream().forEach(l -> l.loggedIn(false));
					}
					MessageDialogUtils.displayInfoMessage("Authorization to Openshift.io was not successful");
				} else {
					List<OSIOLoginListener> listeners = Fabric8AnalysisUIActivator.getDefault().getOSIOLoginListeners();
					if (listeners != null) {
						listeners.stream().forEach(l -> l.loggedIn(true));
					}
				}
			}
		});

		authJob.schedule();
		return null;
	}
}
