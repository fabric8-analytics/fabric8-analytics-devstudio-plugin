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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.redhat.fabric8analytics.eclipse.lsp.core.Fabric8AnalysisLSPPreferences;
import com.redhat.fabric8analytics.eclipse.ui.OSIOLoginListener;

public class LoginListener implements OSIOLoginListener {

	@Override
	public void loggedIn(boolean successfull) {
		Fabric8AnalysisLSPPreferences.getInstance().setLSPServerEnabled(successfull);
		if (successfull) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					PreferencesUtil
							.createPreferenceDialogOn(shell, "com.redhat.fabric8analytics.eclipse.ui.preferences",
									new String[] { "com.redhat.fabric8analytics.eclipse.ui.preferences" }, null)
							.open();

				}
			});
		}
	}

}
