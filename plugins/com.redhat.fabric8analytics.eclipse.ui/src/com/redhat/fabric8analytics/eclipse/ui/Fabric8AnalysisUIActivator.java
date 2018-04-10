/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package com.redhat.fabric8analytics.eclipse.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Fabric8AnalysisUIActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "com.redhat.fabric8analytics.eclipse.ui";

	public static final String SERVICES_EXTENSION_POINT_ID = "com.redhat.fabric8analytics.eclipse.ui.service.preference";
	public static final String LISTENERS_EXTENSION_POINT_ID = "com.redhat.fabric8analytics.eclipse.ui.login.listener";
	
	private static List<OSIOLoginListener> loginListeners;
	private static List<OSIOService> services;

	// The shared instance
	private static Fabric8AnalysisUIActivator plugin;

	/**
	 * The constructor
	 */
	public Fabric8AnalysisUIActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Fabric8AnalysisUIActivator getDefault() {
		return plugin;
	}

	public void logInfo(String msg) {
		IStatus status = new Status(Status.INFO, PLUGIN_ID, msg);
		getLog().log(status);
	}

	public void logError(String msg) {
		IStatus status = new Status(Status.ERROR, PLUGIN_ID, msg);
		getLog().log(status);
	}

	public void logError(String msg, Throwable t) {
		IStatus status = new Status(Status.ERROR, PLUGIN_ID, msg, t);
		getLog().log(status);
	}

	public List<OSIOLoginListener> getOSIOLoginListeners() {
		if (loginListeners == null) {
			loginListeners = new ArrayList<>();
			for (IConfigurationElement element : Platform.getExtensionRegistry()
					.getConfigurationElementsFor(LISTENERS_EXTENSION_POINT_ID)) {
				try {
					if (element.getName().equals("listener")) {
						OSIOLoginListener osioService = (OSIOLoginListener) element.createExecutableExtension("class");
						loginListeners.add(osioService);
					}
				} catch (CoreException e) {
				 logError("Error occured while retrieving osio login listeners", e);
				}
			}
		}
		return loginListeners;
	}

	public List<OSIOService> getOSIOServices() {
		if (services == null) {
			services = new ArrayList<>();
			for (IConfigurationElement element : Platform.getExtensionRegistry()
					.getConfigurationElementsFor(SERVICES_EXTENSION_POINT_ID)) {
				try {
					if (element.getName().equals("service")) {
						OSIOService osioService = (OSIOService) element.createExecutableExtension("class");
						services.add(osioService);
					}
				} catch (CoreException e) {
				 logError("Error occured while retrieving osio services", e);
				}
			}
		}
		return services;
	}

}
