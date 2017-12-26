package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.m2e.editor.pom.MavenPomEditor;
import org.eclipse.m2e.editor.pom.MavenPomEditorPageFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;

public class Fabric8PomEditorFactory extends MavenPomEditorPageFactory {

    @Override
    public void addPages(MavenPomEditor pomEditor) {
        IFormPage page = new Fabric8FormPage(pomEditor);
        try {
            pomEditor.addPage(page);
        } catch (PartInitException e) {
            //TODO Log Error properly
            e.printStackTrace();
        }
    }

    }