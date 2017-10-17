package com.redhat.fabric8analytics.lsp.eclipse.ui;

public class Fabric8AnalysisPreferences {

	public static final String LSP_SERVER_ENABLED = "Fabric8AnalysisPreferences.LSP_SERVER_ENABLED";
	
	private static final Fabric8AnalysisPreferences INSTANCE = new Fabric8AnalysisPreferences();
	
	private Fabric8AnalysisPreferences() {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_ENABLED, true);
	}
	
	public static Fabric8AnalysisPreferences getInstance() {
		return INSTANCE;
	}
	
	public boolean isLSPServerEnabled() {
		return Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getBoolean(LSP_SERVER_ENABLED);
	}
	
	public void setLSPServerEnabled(boolean enabled) {
		Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().setDefault(LSP_SERVER_ENABLED, enabled);
	}
}
