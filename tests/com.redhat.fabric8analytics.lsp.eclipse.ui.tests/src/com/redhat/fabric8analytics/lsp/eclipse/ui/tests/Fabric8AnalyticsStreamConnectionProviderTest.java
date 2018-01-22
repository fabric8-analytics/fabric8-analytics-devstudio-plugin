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
package com.redhat.fabric8analytics.lsp.eclipse.ui.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.equinox.security.storage.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.redhat.fabric8analytics.lsp.eclipse.ui.Fabric8AnalyticsStreamConnectionProvider;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.Fabric8AnalysisPreferences;
import com.redhat.fabric8analytics.lsp.eclipse.ui.internal.TokenCheck;

public class Fabric8AnalyticsStreamConnectionProviderTest {
	
	private static final String TOKEN = "mytoken";
	
	private TokenCheck tokenCheck;

	private Fabric8AnalyticsStreamConnectionProviderWrapper provider;
	
	@Before
	public void setup() throws StorageException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
		
		tokenCheck = mock(TokenCheck.class);
		when(tokenCheck.getToken()).thenReturn(TOKEN);
		
		provider = new Fabric8AnalyticsStreamConnectionProviderWrapper(tokenCheck);
		Fabric8AnalysisPreferences.getInstance().setToken("myToken");
		Fabric8AnalysisPreferences.getInstance().setUserKey("userKey");
	}
	
	@After
	public void after() {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
	}
	
	@Test
	public void createProcessBuilder() throws StorageException {
		ProcessBuilder builder = provider.createProcessBuilder();
		List<String> commands = builder.command();
		
		assertThat(commands.size(), is(3));
		assertThat(commands.get(0), containsString("node"));
		assertThat(commands.get(1), containsString("server.js"));
		assertThat(commands.get(2), containsString("--stdio"));
		
		assertEquals(System.getProperty("user.dir"), builder.directory().getAbsolutePath());
		
		Map<String, String> params = builder.environment();
		
		assertThat(params, hasEntry(Fabric8AnalyticsStreamConnectionProvider.RECOMMENDER_API_TOKEN, TOKEN));
		assertThat(params, hasEntry(Fabric8AnalyticsStreamConnectionProvider.RECOMMENDER_API_URL, Fabric8AnalysisPreferences.getInstance().getProdURL() + Fabric8AnalyticsStreamConnectionProvider.VERSION_ROUTE));
	}
	
	@Test(expected=IOException.class)
	public void start_noToken() throws IOException {
		tokenCheck = mock(TokenCheck.class);
		when(tokenCheck.getToken()).thenReturn(null);
		
		provider = new Fabric8AnalyticsStreamConnectionProviderWrapper(tokenCheck);
		provider.start();
	}
	
	@Test(expected=IOException.class)
	public void start_analysesDisabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		provider.start();
	}
	
	@Test
	public void start() throws IOException {
		provider.start();
		
		assertThat(provider.getInputStream(), is(not(nullValue())));
		assertThat(provider.getOutputStream(), is(not(nullValue())));
	}
	
	@Test
	public void stop() {
		provider.stop();

		assertTrue("No exception expected", true);
	}
	
	@Test
	public void startAndStop() throws IOException {
		provider.start();
		provider.stop();
		
		assertTrue("No exception expected", true);
	}
	
	@Test
	public void propertyEnabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		
		assertThat(provider.getInputStream(), is(nullValue()));
		assertThat(provider.getOutputStream(), is(nullValue()));
		
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(true);
		
		assertThat(provider.getInputStream(), is(not(nullValue())));
		assertThat(provider.getOutputStream(), is(not(nullValue())));
	}
	
	@Test
	public void propertyDisabled() throws IOException {
		Fabric8AnalysisPreferences.getInstance().setLSPServerEnabled(false);
		
		assertTrue("No exception expected", true);
		// there is no way of knowing if the server is stopped
	}
}
