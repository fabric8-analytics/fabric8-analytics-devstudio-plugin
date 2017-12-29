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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Helper class for finding files in the workspace.
 * 
 * @author ljelinko
 *
 */
public class WorkspaceFilesFinder {

	private static final WorkspaceFilesFinder INSTANCE = new WorkspaceFilesFinder();

	public static WorkspaceFilesFinder getInstance() {
		return INSTANCE;
	}

	/**
	 * Finds the pom.xml files in the root of workspace selected projects + adds selected pom.xml files
	 * 
	 * @return
	 */
	public Set<IFile> findPOMs() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return Collections.emptySet(); 
		}
		//		window.getSelectionService().

		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		return findPOMs(selection);
	}

	/**
	 * Finds the pom.xml files in the root of selected projects + adds selected pom.xml files
	 * @param selection
	 * @return
	 */
	private Set<IFile> findPOMs(IStructuredSelection selection) {
		Set<IFile> files = new HashSet<IFile>();

		for (Object o : selection.toList()) {
			if (o instanceof IAdaptable) {
				IProject project = (IProject) ((IAdaptable) o).getAdapter(IProject.class);
				IFile pom = findPOM(project);
				if (pom != null && pom.isAccessible()) {
					files.add(pom);	
				}
			}
		}
		return files;
	}

	/**
	 * Finds the pom.xml file in the root of the project or adds the directly selected pom.xml file
	 * @param adaptable
	 * @return
	 */
	private IFile findPOM(IProject project1) {
		IProject project = (IProject) (project1.getAdapter(IProject.class));
		

		if (project != null) {
			if (project.isAccessible()) {
				IFile temp = project.getFile("pom.xml");
				return project.getFile("pom.xml");
			} else {
				return null;
			}
		} 
		
		return null;
//		IFile file = (IFile) ((IAdaptable) adaptable).getAdapter(IFile.class);
//		if (file != null && file.getName().equals("pom.xml")) {
//			return file;
//		}
//		return null;
	}

//	public Set<IFile> findPOMActivePage() {
//		Set<IFile> files = new HashSet<IFile>();
//		IProject project = getCurrentProject();
//		for (Object o : selection.toList()) {
//			if (o instanceof IAdaptable) {
//				IFile pom = findPOM((IAdaptable) o);
//				if (pom != null && pom.isAccessible()) {
//					files.add(pom);	
//				}
//			}
//		}
//		return files;
//	}
	public Set<IFile> getCurrentProject()
	{
		Set<IFile> files = new HashSet<IFile>();
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
		IFile file = input.getFile();
		IProject project = file.getProject();
		files.add(findPOM(project));
		return files;
	}
}

