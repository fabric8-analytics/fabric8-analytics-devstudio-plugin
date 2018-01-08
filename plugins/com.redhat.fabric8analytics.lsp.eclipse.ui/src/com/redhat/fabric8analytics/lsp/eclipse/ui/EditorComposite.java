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

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.json.JSONException;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.AnalysesJobHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.MessageDialogUtils;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.ThreeScaleIntegration;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.WorkspaceFilesFinder;

/**
 * Class to create composite for page Fabric8Analyses.
 * 
 * @author Geetika Batra
 *
 */
public class EditorComposite extends Composite{

	protected MavenPomEditorPage editorPage;

	public static Browser editorBrowser;

	public static String jobID;

	private MavenPomEditor pomEditor;
	private FormToolkit toolkit = new FormToolkit(Display.getCurrent());

	public EditorComposite(Composite composite, MavenPomEditorPage editorPage, int flags, MavenPomEditor pomEditor) {
		super(composite, flags);
		this.editorPage = editorPage;
		this.pomEditor = pomEditor;
		createComposite();

	}


	private void createComposite() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		Button buttonGo = new Button(this, SWT.NONE);
		buttonGo.setLayoutData(new GridData(GridData.CENTER));
		buttonGo.setText("Generate Stack Report");
		editorBrowser = new Browser(this, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(editorBrowser);
		buttonGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent s) {
				Set<IFile> pomFiles = (Set<IFile>) WorkspaceFilesFinder.getInstance().getCurrentProject();
				if (pomFiles.isEmpty()) {
					MessageDialogUtils.displayInfoMessage("No POM files found in the selection");
					return;
				}
				try {

					String token = TokenCheck.getInstance().getToken();

					if (token == null) {
						MessageDialogUtils.displayInfoMessage("Cannot run analyses because login into OpenShift.io failed");
					}
					Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
					String RECOMMENDER_API_TOKEN = "Bearer "+ token;
					String RECOMMENDER_3SCALE_TOKEN = token;

					String serverURL = Fabric8AnalysisPreferences.getInstance().getProdURL();
					String userKey = Fabric8AnalysisPreferences.getInstance().getUserKey();

					if(serverURL == null && userKey == null) {
						ThreeScaleIntegration.getInstance().set3ScalePreferences(RECOMMENDER_3SCALE_TOKEN);
						serverURL = Fabric8AnalysisPreferences.getInstance().getProdURL();
						userKey = Fabric8AnalysisPreferences.getInstance().getUserKey();
					}
					jobID = RecommenderAPIProvider.getInstance().requestAnalyses(RECOMMENDER_API_TOKEN, pomFiles, serverURL, userKey);


					new AnalysesJobHandler("Analyses check Job", token, true).schedule();

					editorBrowser.setUrl(RecommenderAPIProvider.getInstance().getAnalysesURL(jobID, token));

				}	
				catch (RecommenderAPIException | StorageException | UnsupportedEncodingException | JSONException e) {
					MessageDialogUtils.displayErrorMessage("Error while running stack analyses", e);
				};


			}
		});
	}
	public static void updateBrowser(String url) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				if (!editorBrowser.isDisposed()) {
					editorBrowser.setUrl(url);
				}
			}
		});

	}
}
