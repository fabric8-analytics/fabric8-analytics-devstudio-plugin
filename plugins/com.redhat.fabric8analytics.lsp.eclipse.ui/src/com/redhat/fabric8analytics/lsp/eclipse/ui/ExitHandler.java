package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.mime.content.FileBody;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN = "";
	static  String jobId;
	public static List<IFile> findAllProjectFiles(IContainer container) throws CoreException {
		IResource[] members = container.members();
		List<IFile> list = new ArrayList<>();

		for (IResource member : members) {
			if (member instanceof IContainer) {
				IContainer c = (IContainer) member;
				list.addAll(findAllProjectFiles(c));
			} else if (member instanceof IFile) {
				list.add((IFile) member);
			}
		}
		return list;
	}
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell mapComposite = HandlerUtil.getActiveWorkbenchWindow(event).getShell();

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			IProject[] projects = root.getProjects();
			List<IFile> projectfiles = new ArrayList<>();
			for (IProject project : projects) {

				if (project.isAccessible()) {
					projectfiles =findAllProjectFiles(project);
				}
			}
			Map<String, FileBody> manifestLocationMap = new HashMap<>();
			for(IFile temp_file : projectfiles) {
				String temp_uri = temp_file.toString();
				String[] split_url = temp_uri.split("/");
				if(temp_uri.contains("pom.xml")) {
					System.out.println(temp_uri);
				}
				if(split_url.length>3) {
					if(split_url[2].equals("pom.xml") || split_url[3].equals("pom.xml")) {
						if(!split_url[2].equals("bin"))
						{
							FileBody fileBody = new FileBody(new File(temp_file.getLocation().toString()));
							manifestLocationMap.put(temp_uri, fileBody);
						}
					}

				}
				else {
					if(split_url[2].equals("pom.xml")) {

						FileBody fileBody = new FileBody(new File(temp_file.getLocation().toString()));
						manifestLocationMap.put(temp_uri, fileBody);

					}
				}


			}
			String token = TokenCheck.get().getToken();
			if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
				RECOMMENDER_API_TOKEN = "Bearer "+ token;
			}
			
			IViewPart temp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(StackAnalysesView.NAME); 
			
			try {
				String jobID = RecommenderAPIProvider.getInstance().requestAnalyses(RECOMMENDER_API_TOKEN, manifestLocationMap);
				setJobId(jobID);
				WorkerThread workerThread = new WorkerThread((StackAnalysesView)temp);
				workerThread.go();
			} catch (RecommenderAPIException e) {
				Fabric8AnalysisLSUIActivator.getDefault().logError("Error while running stack analyses", e);
				
				new UIJob(PlatformUI.getWorkbench().getDisplay(), "Error") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						MessageDialog.openError(getDisplay().getActiveShell(), "ERROR", e.getMessage());
						return Status.OK_STATUS;
					}
				}.schedule();
				return mapComposite;
			}
		} catch (Exception e) {
			Fabric8AnalysisLSUIActivator.getDefault().logError("Error while running stack analyses", e);
			
			new UIJob(PlatformUI.getWorkbench().getDisplay(), "Error") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					MessageDialog.openError(getDisplay().getActiveShell(), "ERROR", e.getMessage());
					return Status.OK_STATUS;
				}
			}.schedule();
		}
		return mapComposite;
	}

	private void setJobId(String jobId) {
		ExitHandler.jobId = jobId;
	}
	public static String getJobId() {
		return ExitHandler.jobId;
	}

}
