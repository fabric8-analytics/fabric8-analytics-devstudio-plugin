package com.redhat.fabric8analytics.lsp.eclipse;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.swt.*;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.progress.UIJob;
import org.json.JSONObject;
import com.redhat.fabric8analytics.lsp.eclipse.CustomView;
import com.redhat.fabric8analytics.lsp.eclipse.Utils;
import com.redhat.fabric8analytics.lsp.eclipse.TokenCheck;

public class ExitHandler extends AbstractHandler {
	private String RECOMMENDER_API_TOKEN = "";
	static  String jobId;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//		final String jobId;
		Shell mapComposite = HandlerUtil.getActiveWorkbenchWindow(event).getShell();

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
				if(!RECOMMENDER_API_TOKEN.equals("Bearer " + token)) {
					RECOMMENDER_API_TOKEN = "Bearer "+ token;
				}

				
				

			    
				String viewId = "de.vogella.rcp.commands.first.commands.Exit";
				IViewPart temp=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId); 
				CloseableHttpClient client = HttpClients.createDefault();
				HttpPost post = new HttpPost("https://recommender.api.openshift.io/api/v1/stack-analyses-v2/");		
				post.addHeader("Authorization" , RECOMMENDER_API_TOKEN);
				FileBody fileBody = new FileBody(new File(filePath.toString()));
				MultipartEntityBuilder builder = MultipartEntityBuilder.create()
						.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
						.addPart("manifest[]", fileBody)
						.addTextBody("filePath[]", filePath.toString());

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


			}



		} catch (SWTError | IOException | PartInitException e) {
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
