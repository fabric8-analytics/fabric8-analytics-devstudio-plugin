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

package com.redhat.fabric8analytics.lsp.eclipse.core;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.lsp.eclipse.core.data.ThreeScaleData;

/**
 * Provides access to 3scale service
 * 
 */
public class ThreeScaleAPIProvider {

	public static final String THREE_SCALE_URL = "https://f8a-connect-api-2445582058137.production.gw.apicast.io:443/get-endpoints?user_key=%s";

	public static final String SERVICE_ID = "ad467b765e5c8a8a5ca745a1f32b8487";

	private String token;

	public ThreeScaleAPIProvider(String token) {
		checkConstructorArguments(token);
		this.token = token;
	}

	/**
	 * Registers to 3scale. 
	 * 
	 * @author Geetika Batra
	 * @param token
	 * @return 
	 * @throws ThreeScaleAPIException 
	 */
	public ThreeScaleData register3Scale() throws ThreeScaleAPIException {
		CloseableHttpClient client = createClient();
		try {
			String queryUrl = String.format(THREE_SCALE_URL, SERVICE_ID);
			HttpGet get = new HttpGet(queryUrl);
			get.addHeader("Authorization", token);
			HttpResponse response = client.execute(get);

			int responseCode = response.getStatusLine().getStatusCode();

			if (responseCode==HttpStatus.SC_OK) {
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
				return createThreeScaleData(jsonObj);
			} else {
				throw new ThreeScaleAPIException("The 3scale server returned unexpected return code: " + responseCode);				
			}
		} catch (IOException | JSONException e) {
			throw new ThreeScaleAPIException(e);				
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}
		
	}

	protected CloseableHttpClient createClient() {
		return HttpClients.createDefault();
	}
	
	private void checkConstructorArguments(String token2) {
		if (token2 == null) {
			throw new IllegalArgumentException("The token was null");
		}
	}

	private ThreeScaleData createThreeScaleData(JSONObject jsonObj) throws ThreeScaleAPIException, JSONException {
		String endpointsString = jsonObj.getString("endpoints");
		JSONObject endpoints = new JSONObject(endpointsString);

		ThreeScaleData data = new ThreeScaleData();
		data.setProd(endpoints.getString("prod"));
		data.setStage(endpoints.getString("stage"));
		data.setUserKey(jsonObj.getString("user_key"));
		return data;
	}
}
