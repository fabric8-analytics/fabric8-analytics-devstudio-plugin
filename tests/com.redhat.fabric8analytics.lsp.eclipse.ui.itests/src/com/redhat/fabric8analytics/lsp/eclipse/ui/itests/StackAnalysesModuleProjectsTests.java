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

import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.ImportProjectsRequirements.ImportProjects;
import com.redhat.fabric8analytics.lsp.eclipse.ui.itests.requirements.OSIOLoginRequirement.OSIOLogin;

@RunWith(RedDeerSuite.class)
@OSIOLogin
@ImportProjects(projectsNames={"maven-project-test-modules"})
public class StackAnalysesModuleProjectsTests extends StackAnalysesTestProjectBase {

	@Test
	public void runAnalysesForMainProjectTest() {
		log.info("Validate that project with modules is imported, stack analyses is running and has propper results");
		runStackAnalyses("maven-project-test-modules", "/");
		validateResults("/maven-project-test-modules/pom.xml", "/maven-project-test-modules/module-1/pom.xml",
				"/maven-project-test-modules/module-2/pom.xml");
	}

	@Test
	public void runAnalysesForModule1Test() {
		log.info(
				"Validate that project with modules is imported, stack analyses is running and has propper results for module-1");
		// project module name is 'maven-project-test-modules-module-1'
		runStackAnalyses("maven-project-test-modules-module-1", "/");
		validateResults("/maven-project-test-modules-module-1/pom.xml");
	}

	@Test
	public void runAnalysesForModule2Test() {
		log.info(
				"Validate that project with modules is imported, stack analyses is running and has propper results for module-2");
		// project module name is 'maven-project-test-modules-module-2'
		runStackAnalyses("maven-project-test-modules-module-2", "/");
		validateResults("/maven-project-test-modules-module-2/pom.xml");
	}
	
}