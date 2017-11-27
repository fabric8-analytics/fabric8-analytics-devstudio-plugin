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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.json.JSONObject;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.Fabric8AnalysisLSUIActivator;
import com.redhat.fabric8analytics.lsp.eclipse.core.internal.Utils;

/**
 * Helper class for registration to 3scale.
 * 
 * @author GeetikaBatra
 *
 */
public class ThreeScale {
	private static final ThreeScale INSTANCE = new ThreeScale();
	private static String RECOMMENDER_API_TOKEN = "";
	
	public static ThreeScale getInstance() {
		return INSTANCE;
	}
	
	private ThreeScale() {
		register3scale();
	}
	
	public static void register3scale() {
		try {
			String token = TokenCheck.getInstance().getToken();
			if (token == null) {
				MessageDialogUtils.displayInfoMessage("Cannot run analyses because login into OpenShift.io failed");
			}
			if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
				RECOMMENDER_API_TOKEN = "Bearer "+ token;
			}
			
			JSONObject postRegisterData = RecommenderAPIProvider.getInstance().register3Scale(RECOMMENDER_API_TOKEN);
			Utils.setThreeScalePreferences(postRegisterData);
			
		} catch (RecommenderAPIException e) {
			Fabric8AnalysisLSUIActivator.getDefault().getLog().log(new Status(IStatus.ERROR, Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName(), "Error while registering to 3scale", e));
		}
	}
	
}
