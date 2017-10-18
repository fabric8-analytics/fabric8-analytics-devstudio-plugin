package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class Fabric8AnalysisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final String PREFERENCE_PAGE_ID = Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName() + ".preferences"; //$NON-NLS-1$

	private BooleanFieldEditor enableLSPField;
	
	public Fabric8AnalysisPreferencePage() {
		
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore());
		setTitle("Fabric8");
	}

	@Override
	protected void createFieldEditors() {
		enableLSPField = new BooleanFieldEditor(
				Fabric8AnalysisPreferences.LSP_SERVER_ENABLED, 
				"&Code analyses enabled", 
		 		getFieldEditorParent());
		addField(enableLSPField);
		
		IntegerFieldEditor intervalField = new IntegerFieldEditor(
				Fabric8AnalysisPreferences.LSP_SERVER_TOKEN_CHECK_INTERVAL, 
				"&Token check interval (in minutes)", 
				getFieldEditorParent());
		addField(intervalField);
	}
	
	@Override
	protected void performApply() {
		super.performApply();
		if (enableLSPField != null) {
			enableLSPField.load();
		}
	}
}
