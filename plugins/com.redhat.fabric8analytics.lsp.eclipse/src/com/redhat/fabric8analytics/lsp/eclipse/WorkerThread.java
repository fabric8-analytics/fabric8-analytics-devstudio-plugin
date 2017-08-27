package com.redhat.fabric8analytics.lsp.eclipse;


import java.io.IOException;
import java.net.URL;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.ui.PartInitException;

import com.redhat.fabric8analytics.lsp.eclipse.ExitHandler;
import com.redhat.fabric8analytics.lsp.eclipse.Utils;

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
			URL url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse/templates/index.html");
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

