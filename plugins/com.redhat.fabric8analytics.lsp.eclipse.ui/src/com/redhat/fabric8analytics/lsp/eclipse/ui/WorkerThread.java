package com.redhat.fabric8analytics.lsp.eclipse.ui;


import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

class WorkerThread implements Runnable{

	// The timer interval in milliseconds
	private static final int   TIMER_INTERVAL = 10000;

	private StackAnalysesView mainView;

	public WorkerThread(StackAnalysesView mainView){
		this.mainView = mainView;
	}

	public void go(){

		Thread t = new Thread(this);
		t.start();

	}

	public void run() {
		String jobId = ExitHandler.getJobId();
		
		try {
			URL url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
			url = FileLocator.toFileURL(url);
			mainView.updatebrowserUrl(url.toString());
			
			while(!RecommenderAPIProvider.getInstance().analysesFinished(jobId, TokenCheck.get().getToken())){
				Thread.sleep(TIMER_INTERVAL);
			}

			mainView.updatebrowserUrl(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, TokenCheck.get().getToken()));
		} catch (InterruptedException | IOException | RecommenderAPIException e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Error while running stack analyses", e);
			MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "ERROR", e.getMessage());
		}
	}
}

