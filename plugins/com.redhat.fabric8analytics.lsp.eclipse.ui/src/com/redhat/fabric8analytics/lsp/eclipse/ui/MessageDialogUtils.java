package com.redhat.fabric8analytics.lsp.eclipse.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class MessageDialogUtils {

	public static void displayInfoMessage(String message) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 info message", message);
			}
		});
	}

	public static void displayErrorMessage(String message) {
		Fabric8AnalysisLSUIActivator.getDefault().logError(message);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 error message", message);
			}
		});
	}
	
	public static void displayErrorMessage(String message, Throwable t) {
		Fabric8AnalysisLSUIActivator.getDefault().logError(message, t);
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Fabric8 error message", message);
			}
		});
	}
}
