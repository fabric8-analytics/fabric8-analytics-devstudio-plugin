package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Fabric8AnalysisLSUIActivator extends AbstractUIPlugin {
	
	public static final String PLUGIN_ID = "com.redhat.fabric8analytics.lsp.eclipse.ui";

	// The shared instance
	private static Fabric8AnalysisLSUIActivator plugin;
	
	/**
	 * The constructor
	 */
	public Fabric8AnalysisLSUIActivator() {
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
	public static Fabric8AnalysisLSUIActivator getDefault() {
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
}
