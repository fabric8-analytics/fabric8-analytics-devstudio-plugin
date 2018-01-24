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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests.view;

import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.workbench.impl.view.WorkbenchView;
import org.jboss.tools.openshift.reddeer.condition.BrowserContainsText;

public class StackAnalysesView extends WorkbenchView {

	protected InternalBrowser internalBrowser;

	// basically clone of
	// https://github.com/eclipse/reddeer/blob/02584bf1d10c8922bf616e670a8aeeca8a7b4f23/plugins/org.eclipse.reddeer.eclipse/src/org/eclipse/reddeer/eclipse/ui/browser/WebBrowserView.java
	// because constructors cannot be overriden
	public StackAnalysesView() {
		super("Stack Analyses");
		internalBrowser = new InternalBrowser(new DefaultShell());
	}


	/**
	 * 
	 * Waits for text in browser
	 * 
	 * @param String
	 *            text
	 * @return boolean if text appears in browser
	 */
	public boolean contains(String text) {
		try {
			internalBrowser.setFocus();
			log.info("Internal browser URL is: " + internalBrowser.getURL());
			log.info("Internal browser Text is: " + internalBrowser.getText());
			new WaitUntil(new BrowserContainsText(text, false), TimePeriod.VERY_LONG);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

}
