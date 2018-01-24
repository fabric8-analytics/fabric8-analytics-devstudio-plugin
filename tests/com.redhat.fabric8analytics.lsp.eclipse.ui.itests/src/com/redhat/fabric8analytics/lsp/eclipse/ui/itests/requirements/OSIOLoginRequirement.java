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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.junit.requirement.AbstractRequirement;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.jboss.tools.openshift.reddeer.preference.page.OpenShifIOPreferencePage;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.dialogs.OSIOLoginDialog;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.OSIOLoginRequirement.OSIOLogin;

public class OSIOLoginRequirement extends AbstractRequirement<OSIOLogin> {

	/**
	 * Marks test class, which requires clean workspace before test cases are
	 * executed.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@Documented
	public @interface OSIOLogin {

	}

	/**
	 * Save all editors and delete all projects from workspace.
	 */
	@Override
	public void fulfill() {
		// to make sure that there account is not present from previous failed tests 
		removeAccountFromOpenShiftIOPreferencePage();
		OSIOLoginDialog.openAndLogin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.reddeer.junit.requirement.Requirement#cleanUp()
	 */
	@Override
	public void cleanUp() {
		removeAccountFromOpenShiftIOPreferencePage();
		OSIOLoginDialog.closePreferences(TimePeriod.SHORT);
	}

	// https://github.com/jbosstools/jbosstools-openshift/blob/master/itests/org.jboss.tools.openshift.ui.bot.test/src/org/jboss/tools/openshift/ui/bot/test/integration/openshift/io/GetOpenShiftIOTokenTest.java
	public static void removeAccountFromOpenShiftIOPreferencePage() {
		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		WorkbenchPreferenceDialog preferences = new WorkbenchPreferenceDialog();
		preferences.open();
		OpenShifIOPreferencePage page = new OpenShifIOPreferencePage(preferences);
		preferences.select(page);
		if (page.existsOpenShiftIOAccount()) {
			page.remove();
		}
		preferences.ok();
	}
}