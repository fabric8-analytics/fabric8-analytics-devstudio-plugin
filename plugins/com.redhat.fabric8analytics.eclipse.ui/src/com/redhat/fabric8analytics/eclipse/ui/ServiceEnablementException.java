/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package com.redhat.fabric8analytics.eclipse.ui;

public class ServiceEnablementException extends Exception {
	
	private static final long serialVersionUID = -3361522461631654408L;

	public ServiceEnablementException(Exception e) {
		super(e);
	}

	public ServiceEnablementException(String msg, Exception e) {
		super(msg,e );
	}
}
