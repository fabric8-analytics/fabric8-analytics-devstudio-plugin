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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
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

	private String serverUrl;

	private Set<IFile> pomFiles;

	private String userKey;

	public AnalysesJobHandler(String name, String token, Boolean editorCheck, Set<IFile> pomFiles, String serverUrl, String userKey ) {
		super(name);
		this.token = token;
		this.editorCheck = editorCheck;
		this.pomFiles = pomFiles;
		this.serverUrl = serverUrl;
		this.userKey = userKey;

	}

	protected IStatus run(IProgressMonitor monitor) {
		URL url;
		try {

			url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);
			IViewPart mainView = ExitHandler.getView();

			if(!editorCheck) {
				((StackAnalysesView) mainView).updatebrowserUrl(url.toString());
			}		

			else {
				EditorComposite.updateBrowser(url.toString());
				//				jobId = RecommenderAPIProvider.getInstance().requestAnalyses(token, EffectivePomFiles, serverUrl, userKey);
				//				jobId = EditorComposite.jobID;
				//				
			}
			Set<File> EffectivePomFiles = new HashSet<File>();
			for(IFile pomFile: pomFiles) {
				IMavenProjectRegistry  registry = MavenPlugin.getMavenProjectRegistry();
				IMavenProjectFacade facade = registry.create(pomFile, true, monitor);
				
				MavenProject mavenProject = facade.getMavenProject(monitor);
				Model temp = mavenProject.getModel();
				System.out.println(temp.getPomFile());
				StringWriter sw = new StringWriter();
				new MavenXpp3Writer().write(sw, mavenProject.getModel());

				String effectivePom = sw.toString();
				File file = new File(pomFile.getLocation().toString());
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(effectivePom);
				fileWriter.flush();
				fileWriter.close();
				EffectivePomFiles.add(file);
				jobId = RecommenderAPIProvider.getInstance().requestAnalyses(token, EffectivePomFiles, serverUrl, userKey);
				if(!editorCheck) {
					ExitHandler.setJobId(jobId);
				}

				//				System.out.println(jobId);
				//				jobId = ExitHandler.getJobId();
				setTimerAnalyses();
				syncWithUi(mainView);

			} 
		}catch (IOException | RecommenderAPIException | CoreException e) {
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
