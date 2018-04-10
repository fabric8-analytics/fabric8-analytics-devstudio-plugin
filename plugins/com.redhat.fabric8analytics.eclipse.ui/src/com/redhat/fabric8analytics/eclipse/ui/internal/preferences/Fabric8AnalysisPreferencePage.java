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

package com.redhat.fabric8analytics.eclipse.ui.internal.preferences;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.redhat.fabric8analytics.eclipse.ui.Fabric8AnalysisUIActivator;
import com.redhat.fabric8analytics.eclipse.ui.OSIOService;
import com.redhat.fabric8analytics.eclipse.ui.ServiceEnablementException;
import com.redhat.fabric8analytics.eclipse.ui.internal.MessageDialogUtils;

public class Fabric8AnalysisPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String EXTENSION_POINT_ID = "com.redhat.fabric8analytics.eclipse.ui.service.preference";

	@Override
	public void init(IWorkbench workbench) {
		setTitle("Fabric8");
		setDescription("Following services will be enabled");
	}

	@Override
	protected void performApply() {
		super.performApply();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		for (OSIOService service : Fabric8AnalysisUIActivator.getDefault().getOSIOServices()) {
			Button button = new Button(composite, SWT.CHECK);
			button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			button.setText(service.getServiceName());
			button.setSelection(service.isEnabled());
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						service.enable(button.getSelection());
					} catch (ServiceEnablementException e1) {
						button.setSelection(!button.getSelection());
						MessageDialogUtils.displayErrorMessage("Error occured while enabling osio service", e1);
					}
				}
			});
		}
		return composite;

	}
}
