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

import java.util.Arrays;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.eclipse.reddeer.swt.impl.menu.ContextMenu;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.ImportProjectsRequirements.ImportProjects;

@RunWith(RedDeerSuite.class)
@ImportProjects
public class StackAnalysesContextMenuItemTests extends StackAnalysesTestProjectBase {

	private static final Logger log = Logger.getLogger(StackAnalysesContextMenuItemTests.class);

	@Test
	public void existsForMavenProjectPomTest() {
		log.info("Validating that " + CONTEXT_MENU_ITEM_TEXT + " is present for project '" + getProjectName() + "'");
		ContextMenu contextMenu = getContextMenuFor("/");
		assertTrue("ContextMenu item '" + CONTEXT_MENU_ITEM_TEXT + "' is missing", contextMenu.getItems().stream()
				.filter(p -> p.getText().matches(CONTEXT_MENU_ITEM_TEXT)).findAny().isPresent());
		log.info("Validating that " + CONTEXT_MENU_ITEM_TEXT + " is enabled for project '" + getProjectName() + "'");
		assertTrue("ContextMenu item '" + CONTEXT_MENU_ITEM_TEXT + "' is missing",
				contextMenu.getItem(CONTEXT_MENU_ITEM_TEXT).isEnabled());

		log.info("Validating that " + CONTEXT_MENU_ITEM_TEXT + " is present for root pom.xml file in project '"
				+ getProjectName() + "'");
		contextMenu = getContextMenuFor("pom.xml");
		assertTrue("ContextMenu item '" + CONTEXT_MENU_ITEM_TEXT + "' is missing", contextMenu.getItems().stream()
				.filter(p -> p.getText().matches(CONTEXT_MENU_ITEM_TEXT)).findAny().isPresent());
		log.info("Validating that " + CONTEXT_MENU_ITEM_TEXT + " is enabled for root pom.xml file in project '"
				+ getProjectName() + "'");
		assertTrue("ContextMenu item '" + CONTEXT_MENU_ITEM_TEXT + "' is missing",
				contextMenu.getItem(CONTEXT_MENU_ITEM_TEXT).isEnabled());
	}

	@Test
	public void notExistsForMavenProjectSrcTest() {
		String[] paths = { "src" };
		log.info("Validating that " + CONTEXT_MENU_ITEM_TEXT + " is not present for " + paths.toString()
				+ " file in project '" + getProjectName() + "'");
		Arrays.stream(paths).forEach(path -> {
			assertTrue("ContextMenu item '" + CONTEXT_MENU_ITEM_TEXT + "' is missing",
					!getContextMenuFor(path).getItems().stream()
							.filter(p -> p.getText().matches(CONTEXT_MENU_ITEM_TEXT)).findAny().isPresent());
		});
	}

}