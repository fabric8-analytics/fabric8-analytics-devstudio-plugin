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

package com.redhat.fabric8analytics.lsp.eclipse.ui.internal;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.EditorComposite;
import com.redhat.fabric8analytics.lsp.eclipse.ui.ExitHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.StackAnalysesView;

public class AnalysesJobHandler extends Job{

	private static final int   TIMER_INTERVAL = 10000;

	private static String jobId = null;

	private String token;
	
	private Boolean editorCheck;
	
	private Browser editorBrowser;

	public AnalysesJobHandler(String name, String token, Boolean editorCheck) {
		super(name);
		this.token = token;
		this.editorCheck = editorCheck;
		
	}

	protected IStatus run(IProgressMonitor monitor) {
		URL url;
		try {
			url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);
			IViewPart mainView = ExitHandler.getView();
			
			if(!editorCheck) {
				jobId = ExitHandler.getJobId();
				((StackAnalysesView) mainView).updatebrowserUrl(url.toString());
				
			}
			jobId = EditorComposite.jobID;
			EditorComposite.updateBrowser(url.toString());
			setTimerAnalyses();
			syncWithUi(mainView);
		} catch (IOException e) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
		}
		return Status.OK_STATUS;
	}

	private void setTimerAnalyses() {
		try {
			while(!RecommenderAPIProvider.getInstance().analysesFinished(jobId, token)){
				Thread.sleep(TIMER_INTERVAL);
			}
		} catch (RecommenderAPIException | InterruptedException e) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
		}
	}

	private void syncWithUi(IViewPart mainView) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(editorCheck) {
					EditorComposite.updateBrowser(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, token));
					return;
				}
				
				((StackAnalysesView) mainView).updatebrowserUrl(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, token));
			}
		});

	}
}
