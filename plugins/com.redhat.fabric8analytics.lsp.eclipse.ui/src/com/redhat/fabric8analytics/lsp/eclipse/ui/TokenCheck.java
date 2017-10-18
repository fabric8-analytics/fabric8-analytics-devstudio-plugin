package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.util.function.Function;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class TokenCheck {

	private static final TokenCheck INSTANCE = new TokenCheck();
	
	private Function<IResource, String> provider;
	
	public static TokenCheck getInstance() {
		return INSTANCE;
	}
	
	private TokenCheck() {
		provider = getProvider();
	}
	
	public String getToken() {
		if (null != provider) {
			return provider.apply(null);
		} 
		return null;
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
}
