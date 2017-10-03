package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.jface.layout.GridDataFactory;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.ui.part.ViewPart;

import com.redhat.fabric8analytics.lsp.eclipse.ui.URLSetter;


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

	public void updatebrowserUrl(String browseUrl) {
		// TODO Auto-generated method stub
		try{
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					
					browser.setUrl(browseUrl);
						
					}
			});
		}catch(SWTException e){
		}

	}

	
}
