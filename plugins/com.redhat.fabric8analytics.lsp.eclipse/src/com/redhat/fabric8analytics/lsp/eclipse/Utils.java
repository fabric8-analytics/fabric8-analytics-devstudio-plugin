package com.redhat.fabric8analytics.lsp.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.ui.PartInitException;
import org.json.JSONObject;

public class Utils {


	public static JSONObject jsonObj(HttpResponse response) throws UnsupportedOperationException, IOException {
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

	public static int checkStackProgress(String jobId) throws ClientProtocolException, IOException, PartInitException{
		CloseableHttpClient client = HttpClients.createDefault();
		String recommendUrl = "https://recommender.api.openshift.io/api/v1/stack-analyses-v2/";
//		int getResponseCode ;
		String RECOMMENDER_API_TOKEN = "Bearer ";
		String token = TokenCheck.getToken();
		while(token==null || token.isEmpty()) {					
			TokenCheck.checkToken();	
			token = TokenCheck.getToken();
		}
		if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
		}
		HttpGet get = new HttpGet(recommendUrl + jobId);
		get.addHeader("Authorization" , RECOMMENDER_API_TOKEN);
		HttpResponse getResponse = client.execute(get);
		//		 String viewId = "de.vogella.rcp.commands.first.commands.Exit";
		return getResponse.getStatusLine().getStatusCode();
		//		IViewPart temp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId); 
		//			Browser browser =  ((CustomView) temp).getBrowser();
		//		browser = ((CustomView) temp).getBrowser();
		//		if(getResponseStatus==200) {
		//			browser.setUrl("http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/" + jobId);
		//			return true;
		//		}
		//		else if(getResponseStatus==202) {
		//			//				browser.setUrl("file:///Users/gbatra/fabric8/fabric8-analytics-devstudio-plugin/plugins/com.redhat.fabric8analytics.lsp.eclipse/templates/index.html");
		//			browser.setUrl("http://google.com");
		//			return false;
		//		}
		//		throw new ClientProtocolException();
	}



}
