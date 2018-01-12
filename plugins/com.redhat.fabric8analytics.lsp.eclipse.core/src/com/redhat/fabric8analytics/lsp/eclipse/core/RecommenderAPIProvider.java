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

package com.redhat.fabric8analytics.lsp.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IFile;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.lsp.eclipse.core.internal.Utils;

/**
 * Provides access to Recommender API server
 * 
 * @author ljelinko
 *
 */
public class RecommenderAPIProvider {

	public static String SERVER_URL;
	
	private static String SERVER_ANALYZER_URL;
	
	private static String userScaleKey;
	
	private static final String ANALYSES_REPORT_URL =  "https://stack-analytics-report.prod-preview.openshift.io/#/analyze/";
	
	private static final String POST_ANALYSES_REPORT_URL	= "?api_data={\"access_token\":\"%s\",\"route_config\":{\"api_url\":\"%s\", \"user_key\":\"%s\"},\"show_modal\":false}";
	
	private static final String THREE_SCALE_URL = "https://3scale-connect.api.prod-preview.openshift.io/get-route";

	private static final String SERVICE_ID = "2555417754822";
	
	private static final RecommenderAPIProvider INSTANCE = new RecommenderAPIProvider();

	public static RecommenderAPIProvider getInstance() {
		return INSTANCE;
	}

	/**
	 * Request analysis from the recommender API server. 
	 * 
	 * @param pomFiles
	 * @return jobID
	 */
	public String requestAnalyses(String token, Set<File> files, String serverURL, String userKey) throws RecommenderAPIException {
		
		setServerURL(serverURL);
		setUserKey(userKey);
		
//		HttpPost post = new HttpPost(SERVER_ANALYZER_URL + String.format("?user_key=%s",userKey));
		HttpPost post = new HttpPost("https://recommender.api.openshift.io/api/v1/analyse" + String.format("?user_key=%s",userKey));
		post.addHeader("Authorization" , token);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		System.out.println(files);
		
		for (File file : files)
		{
			builder.addPart("manifest[]", new FileBody(file))
			.addTextBody("filePath[]", file.getAbsolutePath());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);
		System.out.println(post);

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(post);
			int responseCode = response.getStatusLine().getStatusCode();
			System.out.println(responseCode);
			if (responseCode==HttpStatus.SC_OK) {
				JSONObject jsonObj = Utils.jsonObj(response);
				System.out.println(jsonObj.getString("id"));
				return jsonObj.getString("id");
			} else {
				throw new RecommenderAPIException("The recommender server returned unexpected return code: " + responseCode);				
			}
		} catch (IOException | JSONException e) {
			throw new RecommenderAPIException(e);				
		}
	}

	public boolean analysesFinished(String jobId, String token) throws RecommenderAPIException {
		String RECOMMENDER_API_TOKEN = "Bearer ";
		if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
		}
		
		String url = SERVER_ANALYZER_URL + jobId +  String.format("?user_key=%s", userScaleKey);
		HttpGet get = new HttpGet(url);
		get.addHeader("Authorization" , RECOMMENDER_API_TOKEN);

		//TODO - for debug purposes - should be removed later
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("Polling url address to get analyses results: " + url);
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();

			//TODO - for debug purposes - should be removed later
			Fabric8AnalysisLSCoreActivator.getDefault().logInfo("Response code: " + responseCode);
			
			if (responseCode == HttpStatus.SC_OK) {
				return true;
			} else if (responseCode == HttpStatus.SC_ACCEPTED) {
				return false;
			} else {
				throw new RecommenderAPIException("The recommender server returned unexpected return code: " + responseCode);
			}
		} catch (Exception e) {
			throw new RecommenderAPIException(e);
		}
	}
	
	public String getAnalysesURL(String jobID, String token) {
//		to be used once user key is enabled in analyses url
//		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token, SERVER_URL, USER_KEY);
		String temp_server_url = "https://recommender.api.openshift.io/";
		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token, temp_server_url, userScaleKey);
		String url = ANALYSES_REPORT_URL + jobID + postURLFormat; 
		//TODO - for debug purposes - should be removed later
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("Analyses URL: " + url);
		return url;
	}
	
	/**
	 * Registers to 3scale. 
	 * 
	 * @author Geetika Batra
	 * @param token
	 * @return 
	 * @throws UnsupportedEncodingException 
	 * @throws JSONException 
	 * @throws RecommenderAPIException
	 */
	public JSONObject register3Scale(String token) throws RecommenderAPIException, UnsupportedEncodingException, JSONException {
		JSONObject urlObject = new JSONObject();
        urlObject.put("auth_token", token);
        urlObject.put("service_id" , SERVICE_ID);
		HttpPost post = new HttpPost(THREE_SCALE_URL);
		StringEntity se = new StringEntity(urlObject.toString());
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        post.setEntity(se);
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(post);
			
			int responseCode = response.getStatusLine().getStatusCode();
			
			if (responseCode==HttpStatus.SC_OK) {
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
				return jsonObj;
			} else {
				throw new RecommenderAPIException("The 3scale server returned unexpected return code: " + responseCode);				
			}
		} catch (IOException | JSONException e) {
			throw new RecommenderAPIException(e);				
		}
	}
	
	/**
	 * Set Server URL. 
	 * 
	 * @author Geetika Batra
	 * @param serverUrl 
	 */
	public void setServerURL(String serverUrl) {
		SERVER_URL = serverUrl;
		SERVER_ANALYZER_URL = SERVER_URL + "/api/v1/stack-analyses/";
	}
	public void setUserKey(String userKey) {
		userScaleKey = userKey;
	}
}
