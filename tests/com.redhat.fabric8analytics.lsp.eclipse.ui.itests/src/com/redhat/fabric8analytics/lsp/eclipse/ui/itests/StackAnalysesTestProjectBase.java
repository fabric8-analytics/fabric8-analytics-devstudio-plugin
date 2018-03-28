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

import static org.junit.Assert.assertTrue;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.eclipse.core.resources.DefaultProject;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.ImportProjectsRequirements.ImportProjects;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.view.StackAnalysesView;

abstract public class StackAnalysesTestProjectBase {

	// protected final String[] PROJECT_NAMES = { "maven-project-test" };
	protected final String CONTEXT_MENU_ITEM_TEXT = "Stack Analysis";

	protected static final Logger log = Logger.getLogger(StackAnalysesTestProjectBase.class);

	public DefaultProject getProject(String projectName) {
		ProjectExplorer explorer = new ProjectExplorer();
		explorer.open();
		return explorer.getProject(projectName);
	}

	public String getProjectName() {
		// default project
		String[] projectsNames = ((ImportProjects) this.getClass().getAnnotation(ImportProjects.class)).projectsNames();
		return projectsNames[0];
	}

	public DefaultProject getProject() {
		return getProject(getProjectName());
	}

	public ContextMenu getContextMenuFor(String path) {
		return getContextMenuFor(getProjectName(), path);
	}

	public ContextMenu getContextMenuFor(String projectName, String path) {
		if (path.equals("/")) {
			getProject(projectName).select();
		} else {
			getProject(projectName).getProjectItem(path).select();
		}
		return new ContextMenu();
	}

	public void runStackAnalyses(String projectName, String path) {
		ContextMenu contextMenu = getContextMenuFor(projectName, path);
		contextMenu.getItem(CONTEXT_MENU_ITEM_TEXT).select();
	}
	
	public void validateResults(String... texts) {
		StackAnalysesView sav = new StackAnalysesView();
		for (String text : texts) {
			boolean containsText = sav.contains(text);
			assertTrue("Stack analyses View should contain: '" + text + "' but does not", containsText);
		}
	}

}
