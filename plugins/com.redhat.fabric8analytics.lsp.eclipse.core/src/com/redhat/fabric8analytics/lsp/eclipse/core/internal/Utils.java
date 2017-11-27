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

package com.redhat.fabric8analytics.lsp.eclipse.core.internal;

import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;


public class Utils {


	public static JSONObject jsonObj(HttpResponse response) throws JSONException, UnsupportedOperationException, IOException {
		InputStream responseContent = response.getEntity().getContent();

		StringBuilder sb = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				responseContent));
		String line = "";
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		JSONObject json = new JSONObject(sb.toString());
		return json;
	}
	public static void setThreeScalePreferences(JSONObject response) throws JSONException, IOException{
		String prodURL = response.getString("prod");
		if(prodURL!=null) {
			if (Fabric8AnalysisPreferences.getInstance().isProdURLSet()) {
				throw new IOException("Fabric8 analyses server is not enabled");
			}
		}
	}
	
}
