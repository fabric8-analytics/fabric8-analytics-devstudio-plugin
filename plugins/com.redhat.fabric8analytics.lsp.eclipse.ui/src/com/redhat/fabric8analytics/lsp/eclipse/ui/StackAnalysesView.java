package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

public class StackAnalysesView extends ViewPart implements URLSetter {

	public static final String NAME = "com.redhat.fabric8analytics.lsp.eclipse.ui.StackAnalysesView";
	
	private static Browser browser = null;

	@Override
	public Browser getBrowser() {
		return browser;
	}

	@Override
	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);	
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	public void updatebrowserUrl(String browseUrl) {
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				if (!browser.isDisposed()) {
					browser.setUrl(browseUrl);					
				}
			}
		});
	}
}
