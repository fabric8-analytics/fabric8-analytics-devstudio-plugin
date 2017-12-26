package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectChangedListener;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;


class Fabric8FormPage extends FormPage implements IMavenProjectChangedListener {

        public Fabric8FormPage(MavenPomEditor pomEditor) {
            super(pomEditor, "fabric8.analysis ", "Fabric8 Analysis");
        }
        @Override
        protected void createFormContent(IManagedForm managedForm) {
            MavenPlugin.getMavenProjectRegistry().addMavenProjectChangedListener(this);
            // Create content here
            String token = TokenCheck.getInstance().getToken();
        }
        @Override
        public void mavenProjectChanged(MavenProjectChangedEvent[] events,  IProgressMonitor monitor) {
            //react to Maven project changes
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    //Update UI components here
                }
            });
        }
        @Override
        public void dispose() {
            MavenPlugin.getMavenProjectRegistry().removeMavenProjectChangedListener(this);
            super.dispose();
        }
    }
