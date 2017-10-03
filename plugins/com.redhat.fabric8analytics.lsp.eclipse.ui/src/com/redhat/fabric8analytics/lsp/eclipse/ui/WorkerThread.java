package com.redhat.fabric8analytics.lsp.eclipse.ui;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.Bundle;

import com.redhat.fabric8analytics.lsp.eclipse.ui.ExitHandler;
import com.redhat.fabric8analytics.lsp.eclipse.ui.Utils;

class WorkerThread implements Runnable{

	// The timer interval in milliseconds
	private static final int   TIMER_INTERVAL = 10000;

	private CustomView mainView;

	public WorkerThread(CustomView mainView){
		this.mainView = mainView;
	}

	public void go(){

		Thread t = new Thread(this);
		t.start();

	}

	public void run() {
		int getResponseStatus=202;
		String jobId = ExitHandler.getJobId();
		try {
//			Bundle bundle = Platform.getBundle("com.redhat.fabric8analytics.lsp.eclipse");
//			URL fileURL = bundle.getEntry("library/index.html");
//			File file = null;
//			try {
//			    file = new File(FileLocator.resolve(fileURL).toURI());
//			} catch (URISyntaxException e1) {
//			    e1.printStackTrace();
//			} catch (IOException e1) {
//			    e1.printStackTrace();
//			}
//			BufferedReader br = null;
//			FileReader fr = null;
//			fr = new FileReader(FileLocator.resolve(fileURL).toURI().toString());
//			br = new BufferedReader(fr);
//
//			String sCurrentLine;
//
//			while ((sCurrentLine = br.readLine()) != null) {
//				System.out.println(sCurrentLine);
//			}

//			mainView.updatebrowserUrl((fileURL).toURI().toString());
			URL url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);
			mainView.updatebrowserUrl(url.toString());
			while(mainView != null &&  getResponseStatus==202){
				Thread.sleep(TIMER_INTERVAL);
				getResponseStatus = Utils.checkStackProgress(jobId);


			}
			if(getResponseStatus==200) {

				mainView.updatebrowserUrl("http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/" + jobId);
			}
		} catch (InterruptedException | PartInitException | IOException e) {
			e.printStackTrace();
		} 
	}
}

