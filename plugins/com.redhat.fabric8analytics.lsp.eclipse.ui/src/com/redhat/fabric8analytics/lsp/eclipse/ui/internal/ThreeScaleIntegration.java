/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package com.redhat.fabric8analytics.lsp.eclipse.ui.internal;

import org.eclipse.equinox.security.storage.StorageException;
import org.json.JSONException;

import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleData;

/**
 * Helper class for 3scale Integration.
 *
 * @author Geetika Batra
 *
 */
public class ThreeScaleIntegration {

	private static final ThreeScaleIntegration INSTANCE = new ThreeScaleIntegration();

	public static ThreeScaleIntegration getInstance() {
		return INSTANCE;
	}

	public void set3ScalePreferences(String token) throws ThreeScaleAPIException, JSONException, StorageException {
		ThreeScaleData data = new ThreeScaleAPIProvider(token).register3Scale();
		Fabric8AnalysisPreferences.getInstance().setProdURL(data.getProd());
		Fabric8AnalysisPreferences.getInstance().setStageURL(data.getStage());
		Fabric8AnalysisPreferences.getInstance().setUserKey(data.getUserKey());
	}
}

