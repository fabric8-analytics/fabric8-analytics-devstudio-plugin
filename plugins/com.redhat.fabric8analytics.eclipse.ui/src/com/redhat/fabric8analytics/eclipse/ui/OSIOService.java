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

public interface OSIOService {
	
	public String getServiceName();
	
	public boolean isEnabled();
	
	public void enable(boolean enabled) throws ServiceEnablementException;

}
