package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Fabric8AnalysisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final String PREFERENCE_PAGE_ID = Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName() + ".preferences"; //$NON-NLS-1$

	public Fabric8AnalysisPreferencePage() {
		setPreferenceStore(Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore());
		setTitle("Fabric8");
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor formatOnSave = new BooleanFieldEditor(
				Fabric8AnalysisPreferences.LSP_SERVER_ENABLED, 
				"&Code analyses enabled", 
		 		getFieldEditorParent());
		addField(formatOnSave);		
	}
}
