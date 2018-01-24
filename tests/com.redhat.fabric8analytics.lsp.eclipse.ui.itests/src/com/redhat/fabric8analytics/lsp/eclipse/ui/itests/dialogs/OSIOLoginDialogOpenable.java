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

import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.jface.window.Openable;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;
import org.eclipse.reddeer.workbench.impl.shell.WorkbenchShell;
import org.hamcrest.Matcher;

public class OSIOLoginDialogOpenable extends Openable {

	protected static final String CONTEXT_MENU_ITEM_TEXT = "Connect to Openshift.io";

	public OSIOLoginDialogOpenable() {
		super(new WithTextMatcher(""));
	}

	public OSIOLoginDialogOpenable(Matcher<?>... shellMatchers) {
		super(shellMatchers);
	}

	@Override
	public void run() {
		new DefaultToolItem(new WorkbenchShell(), CONTEXT_MENU_ITEM_TEXT).click();
	}

}
