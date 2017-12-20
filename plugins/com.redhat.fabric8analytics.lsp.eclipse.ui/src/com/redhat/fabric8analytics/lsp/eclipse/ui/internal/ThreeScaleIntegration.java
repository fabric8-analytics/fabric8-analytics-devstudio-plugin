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

import java.io.UnsupportedEncodingException;

import org.eclipse.equinox.security.storage.StorageException;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

/**
 * Helper class for 3scale Integration.
 *
 * @author Geetika Batra
 *
 */
public class ThreeScaleIntegration {

	private static final ThreeScaleIntegration INSTANCE = new ThreeScaleIntegration();

	public static ThreeScaleIntegration getInstance() {
		return INSTANCE;
	}

	public void set3ScalePreferences(String token) throws UnsupportedEncodingException, RecommenderAPIException, JSONException, StorageException {
		JSONObject urlObject = RecommenderAPIProvider.getInstance().register3Scale(token);
		JSONObject endpoints = new JSONObject(urlObject.getString("endpoints"));
		if(endpoints!=null) {
			try {	
				Fabric8AnalysisPreferences.getInstance().setProdURL(endpoints.getString("prod"));
				Fabric8AnalysisPreferences.getInstance().setStageURL(endpoints.getString("stage"));
				Fabric8AnalysisPreferences.getInstance().setUserKey(urlObject.getString("user_key"));
				System.out.println(Fabric8AnalysisPreferences.getInstance().getUserKey());

			} catch (JSONException e1) {
				e1.printStackTrace();
			}


		}

		
	}
}

