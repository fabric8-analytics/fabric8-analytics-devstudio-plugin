package com.redhat.bayesian.lsp.eclipse;

import org.eclipse.jface.layout.GridDataFactory;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.ui.part.ViewPart;

import com.redhat.bayesian.lsp.eclipse.URLSetter;


public class CustomView extends ViewPart implements URLSetter {
	private static final String Finally = null;

	private static Browser browser = null;
	


	@Override
	public Browser getBrowser() {
	  return browser;
	}
	
	@Override
	public void createPartControl(Composite parent) {
			
		try 
		{	
			if (browser == null)
			{
				browser = new Browser(parent, SWT.NONE);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(browser);
//				browser.setUrl("http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/fc9ba06b0f1643a7a6ff3470a4c264c2");
//				browser.setVisible(true);
			}
		}
		catch(Exception ce) 
		{
			ce.printStackTrace();
		
		}

			
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
