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
package com.redhat.fabric8analytics.eclipse.core.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.StorageException;

import com.redhat.fabric8analytics.eclipse.core.Fabric8AnalysisCoreActivator;
import com.redhat.fabric8analytics.eclipse.core.data.AnalyticsAuthData;
import com.redhat.fabric8analytics.eclipse.core.internal.AnalyticsAuthService;

public class LoginToOSIOJob extends Job {

		private AnalyticsAuthData analyticsAuthData;

		public LoginToOSIOJob() {
			super("Login to Openshift.io");
		}

		@Override
		protected IStatus run(IProgressMonitor progressMonitor) {
			try {
				analyticsAuthData = AnalyticsAuthService.getInstance().login(progressMonitor);
			} catch (StorageException e) {
				return new Status(IStatus.ERROR, Fabric8AnalysisCoreActivator.PLUGIN_ID, "Error occured while storing Fabric8Analytics data", e);
			}
			return Status.OK_STATUS;
		}

		public AnalyticsAuthData getAnalyticsAuthData() {
			return analyticsAuthData;
		}

	}