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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests.pages;

import static org.junit.Assert.assertTrue;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.reference.ReferencedComposite;
import org.eclipse.reddeer.jface.preference.PreferencePage;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.openshift.reddeer.preference.page.OpenShifIOPreferencePage;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.dialogs.OSIOLoginDialog;

public class OpenshiftServicesPreferencePage extends PreferencePage {
	
	private static final Logger log = Logger.getLogger(OpenshiftServicesPreferencePage.class);

	public static String CHECKBOX_LABEL = "Fabric8 Analytics LSP Server";
	public RegexMatcher CHECKBOX_LABEL_REGEX = new RegexMatcher(".*" + CHECKBOX_LABEL + ".*");

	private OSIOLoginDialog loginDialog = null;
	
	public OpenshiftServicesPreferencePage(ReferencedComposite composite) {
		super(composite, "Openshift Services");
	}
	
	public OSIOLoginDialog enableFabric8AnalyticsLSPServer() {
		CheckBox enablef8analytics = new CheckBox(CHECKBOX_LABEL);
		enablef8analytics.click();
		if (loginDialog == null || !loginDialog.isOpen())
			loginDialog = new OSIOLoginDialog();
		try {
			loginDialog.login();
		} catch (Exception e) {
			// retry
			e.printStackTrace();

			if (!loginDialog.catch404()) {
				log.info("Something went wrong but it was not '404 page not found'");
			}

			try {
				new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
				WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
				preferences.open();
				OpenShifIOPreferencePage page = new OpenShifIOPreferencePage(preferences);
				preferences.select(page);
				if (page.existsOpenShiftIOAccount()) {
					return loginDialog;
				}
			} catch (Exception ee) {
				log.info("Something went wrong when checking that account already exists");
			}

			assertTrue("Maximum number of OS Login attempts occured, failing - enableFabric8AnalyticsLSPServer",
					loginDialog.getAttempts() != OSIOLoginDialog.MAX_ATTEMPTS);

			WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
			preferences.open();
			OpenshiftServicesPreferencePage osServicesPreferences = new OpenshiftServicesPreferencePage(preferences);
			preferences.select(osServicesPreferences);
			
			if (isFabric8AnalyticsLSPServerEnabled())
				disableFabric8AnalyticsLSPServer();
			return enableFabric8AnalyticsLSPServer();
		}
		return loginDialog;
	}

	public void disableFabric8AnalyticsLSPServer() {
		CheckBox enablef8analytics = new CheckBox(CHECKBOX_LABEL);
		enablef8analytics.click();
	}

	public boolean isFabric8AnalyticsLSPServerEnabled() {
		return new CheckBox(CHECKBOX_LABEL).isChecked();
	}

	

}
