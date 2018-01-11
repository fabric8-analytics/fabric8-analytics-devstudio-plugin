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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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

	private static final Set<String> EXCLUDED_FOLDERS = Stream.of("bin", "target").collect(Collectors.toSet());
	
	private static final WorkspaceFilesFinder INSTANCE = new WorkspaceFilesFinder();

	public static WorkspaceFilesFinder getInstance() {
		return INSTANCE;
	}

	/**
	 * Finds the pom.xml files in the root of workspace selected projects + adds selected pom.xml files
	 * 
	 * @return
	 * @throws CoreException 
	 */
	public Set<IFile> findPOMs() throws CoreException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return Collections.emptySet(); 
		}

		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		if (selection == null) {
			return Collections.emptySet();
		}
		return findPOMs(selection);
	}

	/**
	 * Finds the pom.xml files in the root of selected projects + adds selected pom.xml files
	 * @param selection
	 * @return
	 * @throws CoreException 
	 */
	public Set<IFile> findPOMs(IStructuredSelection selection) throws CoreException {
		Set<IFile> files = new HashSet<IFile>();

		for (Object o : selection.toList()) {
			if (o instanceof IAdaptable) {
				findPOMs(files, (IAdaptable) o);
			}
		}
		return files;
	}

	/**
	 * Finds the pom.xml file in the the project recursively or adds the directly selected pom.xml file
	 * @param adaptable
	 * @return
	 * @throws CoreException 
	 */
	private void findPOMs(Set<IFile> files, IAdaptable adaptable) throws CoreException {
		IFile file = (IFile) adaptable.getAdapter(IFile.class);
		if (file != null) {
			if (file.isAccessible() && file.getName().equals("pom.xml")) {
				files.add(file);
			} else {
				return;
			}
		}
		
		IContainer container = (IContainer) adaptable.getAdapter(IContainer.class);
		if (container == null || !container.isAccessible()) {
			return;
		}
		
		for (IResource member : container.members()) {
			if (member instanceof IFolder && EXCLUDED_FOLDERS.contains(member.getName())) {
				continue;
			}
			findPOMs(files, member);
		}
	}

	/**
	 * Finds pom.xml in the project directory.
	 * @param adaptable
	 * @return
	 * @throws CoreException 
	 */
	public Set<IFile> getCurrentPom() throws CoreException
	{
		Set<IFile> pomFile = new HashSet<IFile>(); ;
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
		IFile file = input.getFile();
		pomFile.add(file);
		return pomFile; 
	}
}

