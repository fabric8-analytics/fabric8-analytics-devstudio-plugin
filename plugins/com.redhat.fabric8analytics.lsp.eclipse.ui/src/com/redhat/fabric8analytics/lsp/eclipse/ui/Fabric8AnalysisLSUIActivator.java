package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Fabric8AnalysisLSUIActivator extends AbstractUIPlugin {

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

}
