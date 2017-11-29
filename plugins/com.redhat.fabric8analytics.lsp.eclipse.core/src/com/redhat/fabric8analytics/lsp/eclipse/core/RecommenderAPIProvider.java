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

	public static final String SERVER_URL = "https://recommender.api.openshift.io/api/v1/";
	
	private static final String SERVER_ANALYZER_URL = SERVER_URL + "stack-analyses/";
	
	private static final String ANALYSES_REPORT_URL =  "https://stack-analytics-report.openshift.io/#/analyze/";
	
	private static final String POST_ANALYSES_REPORT_URL	= "?api_data={\"access_token\":\"%s\"}";
	
	private static final String THREE_SCALE_URL = "http://f8a-3scale-admin-gateway-bayesian-preview.b6ff.rh-idev.openshiftapps.com/register";
	
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
	public String requestAnalyses(String token, Set<IFile> files, String ServerURL, String user_key) throws RecommenderAPIException {
		
//		String ServerUrl = Fabric8AnalysisPreferences.getInstance().getProdURL();
//		HttpPost post = new HttpPost(SERVER_ANALYZER_URL);
		String recommenderURL = ServerURL +  String.format("?user_key=%s",user_key);
		HttpPost post = new HttpPost(recommenderURL);
				post.addHeader("Authorization" , token);
//		post.addHeader("Authorization" , user_key);
		System.out.println(post.toString());
		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		for (IFile file : files)
		{
			builder.addPart("manifest[]", new FileBody(new File(file.getLocation().toString())))
			.addTextBody("filePath[]", file.toString());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);
		
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(post);
			int responseCode = response.getStatusLine().getStatusCode();
			
			if (responseCode==HttpStatus.SC_OK) {
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
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
		
		String url = SERVER_ANALYZER_URL + jobId;
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
		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token);
		String url = ANALYSES_REPORT_URL + jobID + postURLFormat; 
		//TODO - for debug purposes - should be removed later
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("Analyses URL: " + url);
		return url;
	}
	
	/**
	 * Registers to 3scale. 
	 * 
	 * @param token
	 * @return 
	 * @throws UnsupportedEncodingException 
	 * @throws JSONException 
	 */
	public JSONObject register3Scale(String token) throws RecommenderAPIException, UnsupportedEncodingException, JSONException {
		JSONObject urlObject = new JSONObject();
        urlObject.put("auth_token", token);
		HttpPost post = new HttpPost(THREE_SCALE_URL);
		StringEntity se = new StringEntity(urlObject.toString());
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        post.setEntity(se);
//		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
//				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//		System.out.println(token);
//		builder.addTextBody("auth_token", token, ContentType.create("application/json", Consts.UTF_8));

//		HttpEntity multipart = builder.build();
//		post.setEntity(multipart);
		
//		System.out.println(post.toString());
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(post);
			
			int responseCode = response.getStatusLine().getStatusCode();
			
			if (responseCode==HttpStatus.SC_OK) {
//				System.out.println(EntityUtils.toString(response.getEntity()));
				JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
				return jsonObj;
			} else {
				throw new RecommenderAPIException("The 3scale server returned unexpected return code: " + responseCode);				
			}
		} catch (IOException | JSONException e) {
			throw new RecommenderAPIException(e);				
		}
	}
}
