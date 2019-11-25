/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/

package com.redhat.fabric8analytics.lsp.eclipse.core.tests;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.ThreeScaleAPIProvider;
import com.redhat.fabric8analytics.lsp.eclipse.core.data.ThreeScaleData;

public class ThreeScaleAPIProviderTest {
	
	private static final String PROD = "produrl";
	
	private static final String STAGE = "stageurl";
	
	private static final String USER_KEY = "myuserkey";
	
	private static final String TOKEN = "mytoken";
	
	private static final String URL = "www.myurl.com";
	
	private static final String JSON_ENDPOINTS = "{\"prod\":" + PROD +  ", \"stage\":"+ STAGE +"}";
	
	private static final String JSON = "{\"endpoints\":" + JSON_ENDPOINTS + ", \"user_key\": "+ USER_KEY +"}";

	private ThreeScaleAPIProvider provider;

	private CloseableHttpClient httpClient;

	private CloseableHttpResponse httpResponse;

	private HttpEntity httpEntity;

	private StatusLine statusLine;
	
	@Before
	public void setup() throws CoreException {
		httpClient = mock(CloseableHttpClient.class);
		httpResponse = mock(CloseableHttpResponse.class);
		httpEntity = mock(HttpEntity.class);
		statusLine = mock(StatusLine.class);

		provider = new ThreeScaleAPIProviderWithCustomClient();
	}
	
	@Test
	public void register3Scale_createGet() throws UnsupportedOperationException, IOException, ThreeScaleAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(JSON.getBytes()));
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		final ArgumentCaptor<HttpGet> argumentCaptor = ArgumentCaptor.forClass(HttpGet.class);

		provider.register3Scale();
		verify(httpClient).execute(argumentCaptor.capture());
		HttpGet httpGet = argumentCaptor.getValue();
		assertThat(httpGet.getURI().toString(), startsWith(String.format(ThreeScaleAPIProvider.THREE_SCALE_URL,ThreeScaleAPIProvider.SERVICE_ID)));
	}

	@Test
	public void register3Scale_responseOK() throws ClientProtocolException, IOException, ThreeScaleAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		
		
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(JSON.getBytes()));
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		ThreeScaleData data = provider.register3Scale();

		verify(httpClient, atLeastOnce()).close();
		assertThat(data.getProd(), is(PROD));
		assertThat(data.getStage(), is(STAGE));
		assertThat(data.getUserKey(), is(USER_KEY));
		
	}

	@Test(expected=ThreeScaleAPIException.class)
	public void register3Scale_responseOther() throws ClientProtocolException, IOException, ThreeScaleAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

		provider.register3Scale();
		
		verify(httpClient, atLeastOnce()).close();
	}

	@Test(expected=ThreeScaleAPIException.class)
	public void register3Scale_clientException() throws ClientProtocolException, IOException, ThreeScaleAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenThrow(IOException.class);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

		provider.register3Scale();
		
		verify(httpClient, atLeastOnce()).close();
	}
	
	private class ThreeScaleAPIProviderWithCustomClient extends ThreeScaleAPIProvider {

		public ThreeScaleAPIProviderWithCustomClient() {
			super();
		}
		
		@Override
		protected CloseableHttpClient createClient() {
			return httpClient;
		}
	}
}
