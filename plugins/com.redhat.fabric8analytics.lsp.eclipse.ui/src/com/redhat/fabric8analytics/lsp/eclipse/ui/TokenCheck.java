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
			try {
				return provider.apply(null);
			} catch (Exception e) {
				Fabric8AnalysisLSUIActivator.getDefault().logError("Error while requesting token from OSIO plugin", e);
			}
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
