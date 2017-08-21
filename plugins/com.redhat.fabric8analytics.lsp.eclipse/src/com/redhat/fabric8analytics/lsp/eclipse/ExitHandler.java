package com.redhat.fabric8analytics.lsp.eclipse;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.swt.*;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.progress.UIJob;
import org.json.JSONObject;
import com.redhat.fabric8analytics.lsp.eclipse.CustomView;
import com.redhat.fabric8analytics.lsp.eclipse.Utils;
import com.redhat.fabric8analytics.lsp.eclipse.JobIdNullException;
import com.redhat.fabric8analytics.lsp.eclipse.TokenCheck;

public class ExitHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		String jobId = null;
		Shell mapComposite = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
		String RECOMMENDER_API_TOKEN =  "Bearer ";
		try {

			ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();

			ISelection selection = service.getSelection();

			if (selection instanceof IStructuredSelection)
			{
				Object selected = ((IStructuredSelection)selection).getFirstElement();

				IFile file = (IFile)Platform.getAdapterManager().getAdapter(selected, IFile.class);
				IPath filePath = file.getLocation();


				String token = TokenCheck.getToken();
				while(token==null || token.isEmpty()) {					
					TokenCheck.checkToken();	
					token = TokenCheck.getToken();
				}
				RECOMMENDER_API_TOKEN = RECOMMENDER_API_TOKEN + token;

				String viewId = "de.vogella.rcp.commands.first.commands.Exit";
				IViewPart temp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId); 
				Browser browser =  ((CustomView) temp).getBrowser();

				CloseableHttpClient client = HttpClients.createDefault();
				HttpPost post = new HttpPost("https://recommender.api.openshift.io/api/v1/stack-analyses-v2/");		
				post.addHeader("Authorization" , RECOMMENDER_API_TOKEN);
				FileBody fileBody = new FileBody(new File(filePath.toString()));
				MultipartEntityBuilder builder = MultipartEntityBuilder.create()
						.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
						.addPart("manifest[]", fileBody);

				HttpEntity multipart = builder.build();
				post.setEntity(multipart);
				HttpResponse response = client.execute(post);
				JSONObject jsonObj = Utils.jsonObj(response);
				if (response.getStatusLine().getStatusCode()==200) {

					jobId = jsonObj.getString("id");

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


				if(jobId  != null) {
					int getResponseStatus = 202;
					while(getResponseStatus==202) {

						TimeUnit.SECONDS.sleep(30);
						HttpGet get = new HttpGet("https://recommender.api.openshift.io/api/v1/stack-analyses-v2/" + jobId);
						get.addHeader("Authorization" , RECOMMENDER_API_TOKEN);
						HttpResponse getResponse = client.execute(get);
						getResponseStatus = getResponse.getStatusLine().getStatusCode();
					}
					if(getResponseStatus==200) {
						browser.setUrl("http://ops-portal-v2-ops-portal-ide.dev.rdu2c.fabric8.io/#/analyze/" + jobId);
					}

				}
				else {
					throw new JobIdNullException("Job Id is Null");
				}

			}



		} catch (SWTError | CoreException | IOException | InterruptedException | JobIdNullException e) {
			e.printStackTrace();
		}



		return mapComposite;
	}
}
