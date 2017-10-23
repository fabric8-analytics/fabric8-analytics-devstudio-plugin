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

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.WorkspaceFilesFinder;



public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN = "";
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
		}
		try {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StackAnalysesView.NAME);
			setView(view);
		} catch (PartInitException e1) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e1);
			return null;
		} 

		try {
			String jobID = RecommenderAPIProvider.getInstance().requestAnalyses(RECOMMENDER_API_TOKEN, pomFiles);
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

}
