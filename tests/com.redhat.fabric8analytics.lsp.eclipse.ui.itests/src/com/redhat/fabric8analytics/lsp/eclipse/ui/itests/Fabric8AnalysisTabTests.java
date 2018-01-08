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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests;

import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.eclipse.exception.EclipseLayerException;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.OSIOLoginRequirement.OSIOLogin;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.tabs.Fabric8Analysis;

@RunWith(RedDeerSuite.class)
@OSIOLogin
public class Fabric8AnalysisTabTests extends StackAnalysesTestProjectBase {

	@Test
	public void validateFabric8AnalysisTabTest() {
		log.info("Validating that tab can be opened for project " + getProjectName());
		getProject(getProjectName()).getProjectItem("pom.xml").open();
		Fabric8Analysis.openTab();
		getProject(getProjectName()).getProjectItem("/").select();
	}

	@AfterClass
	public static void clean() {
		// new ProjectExplorer().getProject(PROJECT_NAMES[0]).delete(false);
		try {
			new ProjectExplorer().deleteAllProjects(false);
		} catch (EclipseLayerException | CoreLayerException e) {
			// idk why but deletion is successfull
			// e.printStackTrace();
			log.info("Exception occured and ignored");
		}
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
	}

}