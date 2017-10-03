package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.util.function.Function;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class TokenCheck {

	static final String RECOMMENDER_API_TOKEN = "RECOMMENDER_API_TOKEN";

	private static final TokenCheck INSTANCE = new TokenCheck();
	
	private Function<IResource, String> provider;
	
	private String token;
	
	
	public static TokenCheck get() {
		return INSTANCE;
	}
	
	private TokenCheck() {
		provider = getProvider();
		IPreferenceStore preferenceStore = Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (RECOMMENDER_API_TOKEN.equals(event.getProperty())) {
					token = (String)event.getNewValue();
				}
			}
		});
	}
	
	private Function<IResource, String> getProvider() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.jboss.tools.openshift.io.core.tokenProvider");
		for(IConfigurationElement element : elements) {
			try {
				return (Function<IResource, String>) element.createExecutableExtension("class");
			} catch (CoreException e) {
				Fabric8AnalysisLSUIActivator.getDefault().getLog().log(new Status(IStatus.ERROR, Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName(), e.getLocalizedMessage(), e));
			}
		}
		return null;
	}

	public String getToken() {
		if (null != provider) {
			return provider.apply(null);
		} else {
			if (null == token) {
				ensureToken();
			}
		}
		return token;
	}
	
	private void ensureToken() {
		token = Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore().getString(RECOMMENDER_API_TOKEN);
		if (null == token) {
			getTokenFromUI();
		}
	}


	void getTokenFromUI() {
		Display display = Display.getCurrent();
		if (null == display) {
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					getToken(Display.getCurrent());
				}
			});
		} else {
			getToken(display);
		}
	}
	
	void getToken(Display display) {
		MessageDialog.openError(display.getActiveShell(), "Incorrect RECOMMENDER_API_TOKEN",
				"You need to set the RECOMMENDER_API_TOKEN to the API token of your OpenShift.io account for Fabric8 analysis to work.\n" +
				"You're going to be redirected to the Preference page for that.");
		PreferencesUtil.createPreferenceDialogOn(display.getActiveShell(),
				Fabric8AnalysisPreferencePage.PREFERENCE_PAGE_ID,
				null, null).open();
	}
}
