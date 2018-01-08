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
package com.redhat.fabric8analytics.lsp.eclipse.ui.itests.dialogs;

import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.common.wait.AbstractWait;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.swt.impl.browser.InternalBrowser;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.jboss.tools.openshift.reddeer.condition.BrowserContainsText;

public class OSIOLoginDialog {
	// https://github.com/jbosstools/jbosstools-openshift/blob/master/itests/org.jboss.tools.openshift.ui.bot.test/src/org/jboss/tools/openshift/ui/bot/test/integration/openshift/io/GetOpenShiftIOTokenTest.java

	protected static final String CONTEXT_MENU_ITEM_TEXT = "Exit"; // TODO change

	protected DefaultShell browser;

	private static final Logger log = Logger.getLogger(OSIOLoginDialog.class);

	OSIOLoginDialog() {
		new DefaultToolItem(new WorkbenchShell(), CONTEXT_MENU_ITEM_TEXT).click();
		browser = new DefaultShell();
	}

	public static OSIOLoginDialog openLoginDialog() {
		return new OSIOLoginDialog();
	}

	public void waitWhileLoading(String text) {
		// new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		// ^^ commented because recent Central lags
		new WaitUntil(new BrowserContainsText(text), TimePeriod.VERY_LONG);
	}

	public void login() {
		InternalBrowser internalBrowser = new InternalBrowser(browser);
		waitWhileLoading("OpenShift.io Developer Preview");

		// by account provider
		switch (System.getProperty("OSLoginProvider")) {
		case "JBossDeveloper":
			internalBrowser.execute("document.getElementById(\"social-jbossdeveloper\").click()");
			// did not find out any better solution then sleep because of superfast reloading of page before JS click() redirect to new url
			AbstractWait.sleep(TimePeriod.SHORT);
			log.info("Waiting for JBossDeveloper portal");
			waitWhileLoading("JBoss<strong>Developer</strong>");

			internalBrowser.execute(String.format("document.getElementById(\"username\").value=\"%s\"",
					System.getProperty("OSusername")));
			internalBrowser.execute(String.format("document.getElementById(\"password\").value=\"%s\"",
					System.getProperty("OSpassword")));

			internalBrowser.execute("document.getElementById(\"fm1\").submit.click()");

			break;
		default:
			internalBrowser.execute(String.format("document.getElementById(\"username\").value=\"%s\"",
					System.getProperty("OSusername")));
			internalBrowser.execute(String.format("document.getElementById(\"password\").value=\"%s\"",
					System.getProperty("OSpassword")));
			internalBrowser.execute(
					"document.getElementById(\"password\").parentElement.parentElement.parentElement.submit()");

		}

		new WaitWhile(new JobIsRunning(), TimePeriod.LONG);
		new DefaultShell("OpenShift.io");
		new OkButton().click();
	}

}
