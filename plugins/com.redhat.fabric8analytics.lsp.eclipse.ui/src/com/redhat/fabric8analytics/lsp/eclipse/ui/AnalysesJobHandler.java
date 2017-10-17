package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

public class AnalysesJobHandler extends Job{
			
		private static final int   TIMER_INTERVAL = 10000;
		private static String jobId = null;
		public AnalysesJobHandler(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

		protected IStatus run(IProgressMonitor monitor) {
			URL url;
			try {
				url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse.ui/templates/index.html");
				url = FileLocator.toFileURL(url);
				IViewPart mainView = ExitHandler.getView();
				jobId = ExitHandler.getJobId();
				((StackAnalysesView) mainView).updatebrowserUrl(url.toString());
				setTimerAnalyses();
				syncWithUi(mainView);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}
		private void setTimerAnalyses() {
			try {
				while(!RecommenderAPIProvider.getInstance().analysesFinished(jobId, TokenCheck.getInstance().getToken())){
					Thread.sleep(TIMER_INTERVAL);
				}
			} catch (RecommenderAPIException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private void syncWithUi(IViewPart mainView) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					((StackAnalysesView) mainView).updatebrowserUrl(RecommenderAPIProvider.getInstance().getAnalysesURL(jobId, TokenCheck.getInstance().getToken()));
				}
			});

		}
}
