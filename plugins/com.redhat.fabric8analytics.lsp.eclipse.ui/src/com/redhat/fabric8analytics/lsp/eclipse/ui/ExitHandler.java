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

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.json.JSONException;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.ThreeScaleIntegration;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.WorkspaceFilesFinder;
//import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.EffectivePomJobHandler;;

public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN;
	private String RECOMMENDER_3SCALE_TOKEN;
	static  String jobId;
	static IViewPart mainView = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Set<IFile> pomFiles = new HashSet<IFile>();
		try {
			pomFiles = WorkspaceFilesFinder.getInstance().findPOMs();
		} catch (CoreException e1) {
			MessageDialogUtils.displayErrorMessage("Error while searching for POM files", e1);
			return null;
		}
		if (pomFiles.isEmpty()) {
			MessageDialogUtils.displayInfoMessage("No POM files found in the selection");
			return null;
		}
		try {
			if(!Fabric8AnalysisPreferences.getInstance().isLSPServerEnabled()) {
				MessageDialogUtils.displayInfoMessage("Enable Fabric8 Analyses");
				return null;
			}
			String token = Fabric8AnalysisPreferences.getInstance().getToken();
			if (token == null) {
				MessageDialogUtils.displayInfoMessage("Cannot run analyses because login into OpenShift.io failed");
				return null;
			}
			else
			{
				token = TokenCheck.getInstance().getToken();
			}
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
			RECOMMENDER_3SCALE_TOKEN = token;
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StackAnalysesView.NAME);
			setView(view);
			String serverURL = Fabric8AnalysisPreferences.getInstance().getProdURL();
			String userKey = Fabric8AnalysisPreferences.getInstance().getUserKey();

			if(serverURL == null && userKey == null) {
				ThreeScaleIntegration.getInstance().set3ScalePreferences(RECOMMENDER_3SCALE_TOKEN);
				serverURL = Fabric8AnalysisPreferences.getInstance().getProdURL();
				userKey = Fabric8AnalysisPreferences.getInstance().getUserKey();
			}
			
			new AnalysesJobHandler("Analyses check Job", token, false, pomFiles, serverURL, userKey ).schedule();
		} catch (RecommenderAPIException | StorageException | UnsupportedEncodingException | JSONException | PartInitException e) {
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

}
