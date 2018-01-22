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

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import com.redhat.fabric8analytics.lsp.eclipse.core.Fabric8AnalysisLSCoreActivator;
import com.redhat.fabric8analytics.lsp.eclipse.core.Fabric8AnalysisPreferences;


public class Fabric8AnalysisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final String PREFERENCE_PAGE_ID = Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName() + ".preferences"; //$NON-NLS-1$

	private BooleanFieldEditor enableLSPField;

	public Fabric8AnalysisPreferencePage() {

	}
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, Fabric8AnalysisLSCoreActivator.PLUGIN_ID));
		setTitle("Fabric8");
		setDescription("Following services will be enabled");
	}

	@Override
	protected void createFieldEditors() {
		enableLSPField = new BooleanFieldEditor(
				Fabric8AnalysisPreferences.LSP_SERVER_ENABLED, 
				"&Fabric8 Analytics",
				getFieldEditorParent());
		addField(enableLSPField);
	}

	@Override
	protected void performApply() {
		super.performApply();
		if (enableLSPField != null) {
			enableLSPField.load();
		}
	}
}
