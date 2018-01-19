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
import java.util.Map;
import java.util.HashMap ;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.equinox.security.storage.StorageException;
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

	private RecommenderAPIProvider provider;

	private String serverUrl;

	private Set<IFile> pomFiles;

	private String userKey;

	private String baseTempDir;

	private EditorComposite editorComposite;


	public AnalysesJobHandler(String name, RecommenderAPIProvider provider, Set<IFile> pomFiles, EditorComposite editorComposite) {
		super(name);
		this.provider = provider;
		this.pomFiles = pomFiles;
		this.editorComposite = editorComposite;
	}

	protected IStatus run(IProgressMonitor monitor) {
		URL url;
		try {

			url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);

			IViewPart mainView = ExitHandler.getView();
			if(editorComposite == null) {
				((StackAnalysesView) mainView).updatebrowserUrl(url.toString());
			} else {
				editorComposite.updateBrowser(url.toString());
			}
			Map<String, File> EffectivePomFiles = new HashMap<String,File>();


			for(IFile pomFile: pomFiles) {
				IMavenProjectRegistry  registry = MavenPlugin.getMavenProjectRegistry();
				IMavenProjectFacade facade = registry.create(pomFile, true, monitor);
				MavenProject mavenProject = facade.getMavenProject(monitor);
				StringWriter sw = new StringWriter();
				new MavenXpp3Writer().write(sw, mavenProject.getModel());

				String effectivePom = sw.toString();
				IProject currentProject = pomFile.getProject();
				baseTempDir = currentProject.getLocation() + "/target/fabrci8Temp/";
				String tempDir = baseTempDir + UUID.randomUUID().toString();
				Boolean tempFileExists = new File(tempDir.toString()).mkdirs();
				if(tempFileExists)
				{
					File effectivePomFile = new File(tempDir + "/pom.xml");
					FileWriter fw=new FileWriter(effectivePomFile);
					fw.write(effectivePom);
					fw.close();
					EffectivePomFiles.put(pomFile.getFullPath().toString(), effectivePomFile);
				}
			} 
			jobId = RecommenderAPIProvider.requestAnalyses(EffectivePomFiles);
			if(setTimerAnalyses(mainView)) {
				syncWithUi(mainView);
			}
			deleteTempDir(baseTempDir);
		}catch (IOException | CoreException | RecommenderAPIException | StorageException  e) {
			deleteTempDir(baseTempDir);
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
		}
		return Status.OK_STATUS;
	}

	private Boolean setTimerAnalyses(IViewPart mainView) throws StorageException {
		try {
			int countTimer = 0;
			while(!provider.analysesFinished(jobId))
			{
				if(countTimer==10) {
					disposeBrowsers(mainView);
					MessageDialogUtils.displayErrorMessage("Analyses Timeout");
					return false;
				}
				Thread.sleep(TIMER_INTERVAL);
				countTimer++;
			}
		} catch (InterruptedException | RecommenderAPIException e) {
			MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
		}
		return true;
	}

	private void deleteTempDir(String baseDir){
		File index = new File(baseDir);

		if(index.isDirectory()) {
			String[]entries = index.list();
			for(String s: entries){

				deleteTempDir(index.getAbsolutePath() + "/" + s);

			}
		}

		index.delete();
		return;
	}

	private void syncWithUi(IViewPart mainView) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				if(editorComposite == null) {
					((StackAnalysesView) mainView).updatebrowserUrl(provider.getAnalysesURL(jobId));
				} else {
					editorComposite.updateBrowser(provider.getAnalysesURL(jobId));
				}
			}
		});

	}

	private void disposeBrowsers(IViewPart mainView) {
		if(editorComposite == null) {
			((StackAnalysesView) mainView).disposebrowserUrl();
		} else {
			editorComposite.disposeBrowser();
		}

	}
}
