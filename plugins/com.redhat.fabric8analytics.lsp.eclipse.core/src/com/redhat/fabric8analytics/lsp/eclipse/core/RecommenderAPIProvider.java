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
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.core.resources.IFile;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides access to Recommender API server
 * 
 * @author ljelinko
 *
 */
public class RecommenderAPIProvider {
	
	public  static String recommender_api_base_url;
	
	private static final String RECOMMENDER_API_URL_POSTFIX = "/api/v1";
	
	private static String userScaleKey;
	
	private static final String RECOMMENDER_API_URL_STACK_ANALYSES_POSTFIX = RECOMMENDER_API_URL_POSTFIX + "/stack-analyses/";

	private static final String ANALYSES_REPORT_URL =  "https://stack-analytics-report.prod-preview.openshift.io/#/analyze/";
	
	private static final String POST_ANALYSES_REPORT_URL	= "?api_data={\"access_token\":\"%s\",\"route_config\":{\"api_url\":\"%s\", \"user_key\":\"%s\"},\"show_modal\":false}";
	
	private String url;

	private static final String SERVICE_ID = "2555417754822";
	
	
	private static String token;
	
	public RecommenderAPIProvider(String url, String userScaleKey, String token) {
		checkConstructorArguments(url, userScaleKey, token);
		this.recommender_api_base_url = url;
		this.token = token;
		this.userScaleKey = userScaleKey;
	}
	
	/**
	 * Request analysis from the recommender API server. 
	 * 
	 * @param pomFiles
	 * @return jobID
	 */
	public static String requestAnalyses(Map<String, File> files) throws RecommenderAPIException {
		
		checkFiles(files);// check if this is none
		HttpPost post = new HttpPost("https://recommender.api.openshift.io/api/v1/analyse" + String.format("?user_key=%s",userScaleKey));
//		HttpPost post = new HttpPost(recommender_api_base_url + RECOMMENDER_API_URL_STACK_ANALYSES_POSTFIX + String.format("?user_key=%s",userScaleKey));
		post.addHeader("Authorization" , token);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		
		for (Map.Entry<String, File> fileObject : files.entrySet())
		{
			builder.addPart("manifest[]", new FileBody(fileObject.getValue()))
			.addTextBody("filePath[]", fileObject.getKey());
		}

		HttpEntity multipart = builder.build();
		post.setEntity(multipart);

		CloseableHttpClient client = createClient();
		try {
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
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}
	}

	public boolean analysesFinished(String jobId) throws RecommenderAPIException {
		System.out.println("analysesFinished started");
		checkJobID(jobId);
		String RECOMMENDER_API_TOKEN = "Bearer ";
		if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
		}
		HttpGet get = new HttpGet("https://recommender.api.openshift.io/api/v1/stack-analyses/" + jobId );
//		HttpGet get = new HttpGet(recommender_api_base_url + RECOMMENDER_API_URL_STACK_ANALYSES_POSTFIX + jobId +  String.format("?user_key=%s", userScaleKey));
		get.addHeader("Authorization" , RECOMMENDER_API_TOKEN);

		CloseableHttpClient client = createClient();
		
		try {
			HttpResponse response = client.execute(get);
			int responseCode = response.getStatusLine().getStatusCode();

			//TODO - for debug purposes - should be removed later
			Fabric8AnalysisLSCoreActivator.getDefault().logInfo("F8 server response code: " + responseCode);
			
			if (responseCode == HttpStatus.SC_OK) {
				return true;
			} else if (responseCode == HttpStatus.SC_ACCEPTED) {
				return false;
			} else {
				throw new RecommenderAPIException("The recommender server returned unexpected return code: " + responseCode);
			}
		} catch (Exception e) {
			throw new RecommenderAPIException(e);
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// do nothing
			}
		}
		
	}
	
	public String getAnalysesURL(String jobID) {
//		to be used once user key is enabled in analyses url
//		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token, SERVER_URL, USER_KEY);
		String temp_server_url = "https://recommender.api.openshift.io/";
		
//		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token, recommender_api_base_url, userScaleKey);
		String postURLFormat = String.format(POST_ANALYSES_REPORT_URL, token, temp_server_url, userScaleKey);

		String url = ANALYSES_REPORT_URL + jobID + postURLFormat; 
		return url;
	}
	
	protected static CloseableHttpClient createClient() {
		return HttpClients.createDefault();
	}
	
	private void checkConstructorArguments(String url2, String userKey2, String token2) {
		if (url2 == null) {
			throw new IllegalArgumentException("The URL was null");
		}
		
		if ("".equals(url2)) {
			throw new IllegalArgumentException("The URL was empty");
		}
		
		if (userKey2 == null) {
			throw new IllegalArgumentException("The user key was null");
		}
		
		if (token2 == null) {
			throw new IllegalArgumentException("The token was null");
		}
	}
	
	private static void checkFiles(Map<String, File> files) {
		if (files == null) {
			throw new IllegalArgumentException("Files for analyses were null");
		}
		
		if (files.size() == 0) {
			throw new IllegalArgumentException("Files for analyses were empty");
		}
	}
	
	public void setUserKey(String userKey) {
		userScaleKey = userKey;
	}
	
	private void checkJobID(String jobId) {
		if (jobId == null) {
			throw new IllegalArgumentException("Job ID was null");
		}
		
		if ("".equals(jobId)) {
			throw new IllegalArgumentException("Job ID was empty string");
		}		
	}
}
