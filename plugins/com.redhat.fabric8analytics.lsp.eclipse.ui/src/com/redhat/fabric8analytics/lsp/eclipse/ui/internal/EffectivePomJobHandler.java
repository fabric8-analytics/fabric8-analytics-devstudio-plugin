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
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.ui.IViewPart;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;


/**
 * Helper class for finding files in the workspace.
 * 
 * @author Geetika Batra
 *
 */
public class EffectivePomJobHandler extends Job{

	private IFile pomFile;


	public EffectivePomJobHandler(String name, IFile pomFile) {
		super(name);
		this.pomFile = pomFile;
		
	}
	
	protected IStatus run(IProgressMonitor monitor) {
		
		try {
			IMavenProjectRegistry  registry = MavenPlugin.getMavenProjectRegistry();
			IMavenProjectFacade facade = registry.create(pomFile, true, monitor);
			MavenProject mavenProject = facade.getMavenProject(monitor);
			StringWriter sw = new StringWriter();
			new MavenXpp3Writer().write(sw, mavenProject.getModel());
			String effectivePom = sw.toString();
			System.out.println(effectivePom);
			
		} catch (IOException | CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}


	private void syncWithUi(IViewPart mainView) {
//		Display.getDefault().asyncExec(new Runnable() {
//			public void run() {
//				if(editorCheck) {
//					EditorComposite.updateBrowser(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, token));
//					return;
//				}
//				
//				((StackAnalysesView) mainView).updatebrowserUrl(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, token));
//			}
//		});

	}
}
