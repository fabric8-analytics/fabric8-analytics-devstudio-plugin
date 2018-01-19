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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.json.JSONException;

import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.ThreeScaleIntegration;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;

/**
 * Handler for menu item "Enable Fabric8 Analyses".
 *
 * @author Geetika Batra
 *
 */
public class AuthorizeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String id = Fabric8AnalysisPreferencePage.PREFERENCE_PAGE_ID;
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String token = TokenCheck.getInstance().getToken();
		if (token == null) {
			Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
			MessageDialogUtils.displayInfoMessage("Authorization to Openshift.io was not successful");
			return null;
		}
		try {
			Fabric8AnalysisPreferences.getInstance().setToken(token);
			ThreeScaleIntegration.getInstance().set3ScalePreferences(token);
		} catch (ThreeScaleAPIException | JSONException | StorageException e) {
			e.printStackTrace();
		}
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
		PreferencesUtil.createPreferenceDialogOn(shell,
				"com.redhat.fabric8analytics.lsp.eclipse.preferences", new String[] { "com.redhat.fabric8analytics.lsp.eclipse.preferences" }, null).open();
		return null;
	}
}
