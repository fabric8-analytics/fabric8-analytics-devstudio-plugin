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

package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.JSONException;
import org.json.JSONObject;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.ThreeScale;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.WorkspaceFilesFinder;



public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN = "";
	private String RECOMMENDER_3SCALE_TOKEN = "";
	static  String jobId;
	static IViewPart mainView = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Set<IFile> pomFiles = WorkspaceFilesFinder.getInstance().findPOMs();
		if (pomFiles.isEmpty()) {
			MessageDialogUtils.displayInfoMessage("No POM files found in the selection");
			return null;
		}

		String token = TokenCheck.getInstance().getToken();
		if (token == null) {
			MessageDialogUtils.displayInfoMessage("Cannot run analyses because login into OpenShift.io failed");
			return null;
		}
		
		if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
			RECOMMENDER_3SCALE_TOKEN = token;
		}
		
//		ThreeScale.getInstance().setToken(token);
		try {
			JSONObject postRegisterData = RecommenderAPIProvider.getInstance().register3Scale(RECOMMENDER_3SCALE_TOKEN);
			setThreeScalePreferences(postRegisterData);
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StackAnalysesView.NAME);
			setView(view);
		} catch (PartInitException | RecommenderAPIException | JSONException | IOException e1) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e1);
			return null;
		} 

		try {
			
			String serverURL = Fabric8AnalysisPreferences.getInstance().getProdURL() + "/api/v1/stack-analyses";
			String userKey = Fabric8AnalysisPreferences.getInstance().getUserKey();
			String jobID = RecommenderAPIProvider.getInstance().requestAnalyses(RECOMMENDER_API_TOKEN, pomFiles, serverURL, userKey);
			setJobId(jobID);
			new AnalysesJobHandler("Analyses check Job", token).schedule();
		} catch (RecommenderAPIException e) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
		}
		return null;
	}

	public static  void setJobId(String jobId) {
		ExitHandler.jobId = jobId;
	}

	public static String getJobId() {
		return ExitHandler.jobId;
	}


	public static void setView(IViewPart mainView) {
		ExitHandler.mainView = mainView;
	}

	public static IViewPart getView() {
		return mainView;
	}
	
	public static void setThreeScalePreferences(JSONObject response) throws JSONException, IOException{
		JSONObject endpoints = new JSONObject(response.getString("endpoints"));
		if(endpoints!=null) {
			String prodURL = endpoints.getString("prod");
			String stageURL = endpoints.getString("stage");
			String userKey = response.getString("user_key");
			Fabric8AnalysisPreferences.getInstance().setProdURL(prodURL);
//			Fabric8AnalysisPreferences.getInstance().setProdURL(stageURL);
			Fabric8AnalysisPreferences.getInstance().setUserKey(userKey);
//			if (Fabric8AnalysisPreferences.getInstance().isProdURLSet()) {
//				throw new IOException("Prod URL is not set");
//			}
			
			
		}
	}

}
