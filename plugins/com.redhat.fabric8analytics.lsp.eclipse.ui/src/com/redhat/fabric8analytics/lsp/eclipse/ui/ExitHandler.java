package com.redhat.fabric8analytics.lsp.eclipse.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import org.eclipse.swt.SWTError;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;
import org.json.JSONException;
import org.json.JSONObject;

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
			String viewId = "de.vogella.rcp.commands.first.commands.Exit";
			IViewPart temp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId); 
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost("https://recommender.api.openshift.io/api/v1/stack-analyses/");		
			post.addHeader("Authorization" , RECOMMENDER_API_TOKEN);
			MultipartEntityBuilder builder = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			for (Map.Entry<String, FileBody>  entry : manifestLocationMap.entrySet())
			{
				builder.addPart("manifest[]", entry.getValue())
				.addTextBody("filePath[]", entry.getKey());
			}


			HttpEntity multipart = builder.build();
			post.setEntity(multipart);
			HttpResponse response = client.execute(post);
			JSONObject jsonObj = Utils.jsonObj(response);
			if (response.getStatusLine().getStatusCode()==200) {
				setJobId(jsonObj.getString("id"));
				WorkerThread workerThread = new WorkerThread((CustomView)temp);
				workerThread.go();

			}
			else {
				new UIJob(PlatformUI.getWorkbench().getDisplay(), jsonObj.toString()) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						MessageDialog.openError(getDisplay().getActiveShell(), "ERROR", jsonObj.toString());
						return Status.OK_STATUS;
					}
				}.schedule();
				return mapComposite;
			}

		} catch (SWTError | IOException | CoreException | JSONException e) {
			e.printStackTrace();
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
