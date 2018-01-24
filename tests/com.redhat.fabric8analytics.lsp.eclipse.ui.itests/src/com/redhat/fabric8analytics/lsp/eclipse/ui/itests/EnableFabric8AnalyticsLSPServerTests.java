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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests;

import static org.junit.Assert.assertTrue;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.dialogs.OpenshiftServicesPreferenceDialog;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.pages.OpenshiftServicesPreferencePage;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.OSIOLoginRequirement;

@RunWith(RedDeerSuite.class)
public class EnableFabric8AnalyticsLSPServerTests {

	private static final Logger log = Logger.getLogger(EnableFabric8AnalyticsLSPServerTests.class);

	@Test
	public void enableFabric8AnalyticsLSPServerTest() {
		log.info("Check if '" + OpenshiftServicesPreferenceDialog.CHECKBOX_LABEL + "' is checkable and proper action follows (osio login dialog shows up)");
		OSIOLoginRequirement.removeAccountFromOpenShiftIOPreferencePage();
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		OpenshiftServicesPreferencePage osServicesPreferences = new OpenshiftServicesPreferencePage(preferences);
		preferences.select(osServicesPreferences);
		
		OpenshiftServicesPreferenceDialog osServices = osServicesPreferences.getOpenshiftServicesPreferenceDialog();
		if(osServices.isFabric8AnalyticsLSPServerEnabled())
			osServices.disableFabric8AnalyticsLSPServer();
		osServices.enableFabric8AnalyticsLSPServer();

		// open preferences anew 
		preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		osServicesPreferences = new OpenshiftServicesPreferencePage(preferences);
		preferences.select(osServicesPreferences);

		osServices = osServicesPreferences.getOpenshiftServicesPreferenceDialog();
		assertTrue("Fabric8Analytics LSP server chekbox in Openshift services preferences page should be checked but it is not",osServices.isFabric8AnalyticsLSPServerEnabled());
		
		osServices.disableFabric8AnalyticsLSPServer();
		osServices.ok();
		//TODO add check that LSP server is disabled
		OSIOLoginRequirement.removeAccountFromOpenShiftIOPreferencePage();
	}

}