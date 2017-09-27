package com.redhat.fabric8analytics.lsp.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.swt.*;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.json.JSONObject;
import com.redhat.fabric8analytics.lsp.eclipse.CustomView;
import com.redhat.fabric8analytics.lsp.eclipse.Utils;
import com.redhat.fabric8analytics.lsp.eclipse.TokenCheck;

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

				projectfiles =findAllProjectFiles(project);
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
			String token = TokenCheck.getToken();
			if(token==null || token.isEmpty()) {					
				TokenCheck.checkToken();	
				token = TokenCheck.getToken();
			}
			if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
				RECOMMENDER_API_TOKEN = "Bearer "+ token;
			}
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
				String jobId = getJobId();


				UIJob test = new UIJob(PlatformUI.getWorkbench().getDisplay(), jobId) {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							String viewId = "de.vogella.rcp.commands.first.commands.Exit";
							IViewPart stackView=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId); 
							int getResponseStatus = 202;
							URL url = new URL("platform:/plugin/com.redhat.fabric8analytics.lsp.eclipse/templates/index.html");

							url = FileLocator.toFileURL(url);
							((CustomView) stackView).updatebrowserUrl(url.toString());
							while(getResponseStatus==202)
							{
								TimeUnit.SECONDS.sleep(10);
								getResponseStatus = Utils.checkStackProgress(jobId);
							}

							if(getResponseStatus==200) {


								((CustomView) stackView).updatebrowserUrl("http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/" + jobId);
							}
						} catch (PartInitException | IOException | InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} 
						return Status.OK_STATUS;
					}
				};
				test.schedule();
				//				WorkerThread workerThread = new WorkerThread((CustomView)temp);
				//				workerThread.go();

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

		} catch (SWTError | IOException | CoreException e) {
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
