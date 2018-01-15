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

import java.io.File;
import java.io.IOException;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.eclipse.core.resources.DefaultProject;
import org.eclipse.reddeer.eclipse.m2e.core.ui.wizard.MavenImportWizard;
import org.eclipse.reddeer.eclipse.m2e.core.ui.wizard.MavenImportWizardPage;
import org.eclipse.reddeer.eclipse.ui.navigator.resources.ProjectExplorer;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.eclipse.reddeer.workbench.handler.WorkbenchShellHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

abstract public class StackAnalysesTestProjectBase {

	protected static final String[] PROJECT_NAMES = { "maven-project-test" };
	protected static final String CONTEXT_MENU_ITEM_TEXT = "Stack Analyses";

	protected static final Logger log = Logger.getLogger(StackAnalysesTestProjectBase.class);

	public static void importProjects(String[] projectNames) throws IOException {
		for (String projectName : projectNames) {
			importProject(projectName);
		}
	}

	public static void importProject(String projectName) throws IOException {
		log.info("Import " + projectName);
		String path = "resources/" + projectName;
		MavenImportWizard importDialog = new MavenImportWizard();
		importDialog.open();
		MavenImportWizardPage importPage = new MavenImportWizardPage(importDialog);
		String canonicalPath = new File(path).getCanonicalPath();
		log.info("Canonical path to resoruce project: " + canonicalPath);
		importPage.setRootDirectory(canonicalPath);
		importDialog.finish();
	}

	@BeforeClass
	public static void prepare() throws IOException {
		importProjects(PROJECT_NAMES);
	}

	@AfterClass
	public static void clean() {
		new ProjectExplorer().deleteAllProjects(false);
		WorkbenchShellHandler.getInstance().closeAllNonWorbenchShells();
	}

	public DefaultProject getProject(String projectName) {
		ProjectExplorer explorer = new ProjectExplorer();
		explorer.open();
		return explorer.getProject(projectName);
	}
	
	public String getProjectName() {
		// default project
		return PROJECT_NAMES[0];
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
		// TODO wait for internal Browser to start and load then wait for status code 200, 
		// TODO check that there is no error dialog
	}

}