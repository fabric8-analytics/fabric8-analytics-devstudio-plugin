package com.redhat.fabric8analytics.lsp.eclipse.core;

import java.io.File;
import java.io.IOException;
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

	private static final String SERVER_URL = "https://recommender.api.openshift.io/api/v1/stack-analyses/";
	
	private static final String ANALYSES_REPORT_URL = "http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/";

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
	public String requestAnalyses(String token, Set<IFile> files) throws RecommenderAPIException {
		HttpPost post = new HttpPost(SERVER_URL);
		post.addHeader("Authorization" , token);

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
				JSONObject jsonObj = Utils.jsonObj(response);
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
		HttpGet get = new HttpGet(SERVER_URL + jobId);
		get.addHeader("Authorization" , RECOMMENDER_API_TOKEN);

		//TODO - for debug purposes - should be removed later
		Fabric8AnalysisLSCoreActivator.getDefault().logInfo("Polling url address to get analyses results: " + SERVER_URL + jobId);
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
	
	public String getAnalysesURL(String jobID) {
		return ANALYSES_REPORT_URL + jobID;
	}
}
