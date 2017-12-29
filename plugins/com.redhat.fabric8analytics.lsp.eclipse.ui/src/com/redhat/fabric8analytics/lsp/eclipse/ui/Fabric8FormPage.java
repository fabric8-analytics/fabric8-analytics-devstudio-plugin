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

package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.IFileEditorInput; 

import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;

/**
 * Class to create contents of page Fabric8Analyses.
 * 
 * @author Geetika Batra
 *
 */
class Fabric8FormPage extends MavenPomEditorPage implements IMavenProjectChangedListener  {

	private EditorComposite dependenciesComposite;
	public Fabric8FormPage(MavenPomEditor pomEditor) {
		super(pomEditor, "fabric8.analysis ", "Fabric8 Analysis");

	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();

	    ScrolledForm form = managedForm.getForm();
	    form.setText("This page analyses stack of current open project. Click Generate Analyses Report to Proceed ");

	    form.getBody().setLayout(new GridLayout(1, true));

	    		
//	    IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
	    EditorComposite dependenciesComposite = new EditorComposite(form.getBody(), this, SWT.NONE, pomEditor);
	    dependenciesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    toolkit.adapt(dependenciesComposite);
	    

	    super.createFormContent(managedForm);

	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent[] arg0, IProgressMonitor arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadData() {
		// TODO Auto-generated method stub
		
	}
}
