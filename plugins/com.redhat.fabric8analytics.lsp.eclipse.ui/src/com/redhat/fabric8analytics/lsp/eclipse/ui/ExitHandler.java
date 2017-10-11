package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.core.WorkspaceFilesFinder;

public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN = "";
	static  String jobId;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Set<IFile> pomFiles = WorkspaceFilesFinder.getInstance().findPOMs();
		if (pomFiles.isEmpty()) {
			displayInfoMessage("No POM files found in the selection");
			return null;
		}

		String token = TokenCheck.get().getToken();
		if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
			RECOMMENDER_API_TOKEN = "Bearer "+ token;
		}

		IViewPart view;
		try {
			view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StackAnalysesView.NAME);
		} catch (PartInitException e1) {
			displayErrorMessage("Error while running stack analyses", e1);
			return null;
		} 

		try {
			String jobID = RecommenderAPIProvider.getInstance().requestAnalyses(RECOMMENDER_API_TOKEN, pomFiles);
			setJobId(jobID);
			WorkerThread workerThread = new WorkerThread((StackAnalysesView)view);
			workerThread.go();
		} catch (RecommenderAPIException e) {
			displayErrorMessage("Error while running stack analyses", e);
		}
		return null;
	}

	private void displayInfoMessage(String message) {
		Fabric8AnalysisLSUIActivator.getDefault().logInfo(message);
		MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "INFO", message);
	}
	
	private void displayErrorMessage(String message, Throwable t) {
		Fabric8AnalysisLSUIActivator.getDefault().logError(message, t);
		MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "ERROR", t.getMessage());
	}
	
	private void setJobId(String jobId) {
		ExitHandler.jobId = jobId;
	}
	public static String getJobId() {
		return ExitHandler.jobId;
	}

}
