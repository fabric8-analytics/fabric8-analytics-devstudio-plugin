package com.redhat.bayesian.lsp.eclipse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
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
	


}
