package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

public class Fabric8AnalysisPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	static final String PREFERENCE_PAGE_ID = Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName() + ".preferences"; //$NON-NLS-1$

	public Fabric8AnalysisPreferencePage() {
		super("Fabric8 analysis", null, SWT.FLAT);
		setPreferenceStore(Fabric8AnalysisLSUIActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new StringFieldEditor(Fabric8AnalyticsStreamConnectionProvider.RECOMMENDER_API_TOKEN, "RECOMMENDER_API_TOKEN", getFieldEditorParent()));
		// TODO:
		// * multi-line text area
		// * Explanation of how to get the token
		// * Link opening web browser on the right page to get token
		// * screenshots
	}

	@Override
	protected Control createContents(Composite parent) {
		Control res = super.createContents(parent);
		Link label = new Link(getFieldEditorParent(), SWT.NONE);
		label.setText("You can retrieve the value of RECOMMENDER_API_TOKEN on <A>your OpenShift.io account</A>");
		label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, ((GridLayout)getFieldEditorParent().getLayout()).numColumns, 1));
		label.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser(PREFERENCE_PAGE_ID);
					browser.openURL(new URL("http://openshift.io"));
				} catch (Exception ex) {
					Fabric8AnalysisLSUIActivator.getDefault().getLog().log(new Status(
						IStatus.ERROR,
						Fabric8AnalysisLSUIActivator.getDefault().getBundle().getSymbolicName(),
						ex.getMessage(),
						ex
					));
				}
			}
		});
		Label screenshot = new Label(getFieldEditorParent(), SWT.NONE);
		screenshot.setText("TODO: Place screenshot to localize token here");
		screenshot.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, ((GridLayout)getFieldEditorParent().getLayout()).numColumns, 1));
		return res;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

}
