package com.redhat.fabric8analytics.lsp.eclipse.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
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
				IFile pom = findPOM((IAdaptable) o);
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
	private IFile findPOM(IAdaptable adaptable) {
		IProject project = (IProject) ((IAdaptable) adaptable).getAdapter(IProject.class);

		if (project != null) {
			if (project.isAccessible()) {
				return project.getFile("pom.xml");
			} else {
				return null;
			}
		} 

		IFile file = (IFile) ((IAdaptable) adaptable).getAdapter(IFile.class);
		if (file != null && file.getName().equals("pom.xml")) {
			return file;
		}
		return null;
	}
}
