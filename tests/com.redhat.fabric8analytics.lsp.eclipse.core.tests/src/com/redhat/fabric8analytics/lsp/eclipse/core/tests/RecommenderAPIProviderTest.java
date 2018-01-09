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

import static org.hamcrest.CoreMatchers.containsString;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIException;
import com.redhat.fabric8analytics.lsp.eclipse.core.RecommenderAPIProvider;

public class RecommenderAPIProviderTest {

	private static final String TOKEN = "mytoken";

	private static final String USER_KEY = "myuserkey";

	private static final String URL = "www.myurl.com";
	
	private static final String JOB = "myjob";

	private static final String JSON = "{\"id\":12345}";

	private RecommenderAPIProvider provider;

	private CloseableHttpClient httpClient;

	private CloseableHttpResponse httpResponse;

	private HttpEntity httpEntity;

	private StatusLine statusLine;
	
	private Set<IFile> files;

	@Before
	public void setup() throws CoreException {
		httpClient = mock(CloseableHttpClient.class);
		httpResponse = mock(CloseableHttpResponse.class);
		httpEntity = mock(HttpEntity.class);
		statusLine = mock(StatusLine.class);

		provider = new RecommenderAPIProviderWithCustomClient(URL, USER_KEY, TOKEN);
		
		IProject project = createProject("myproject");
		files = new HashSet<>();
		files.add(createFile(project, "firstfile", "aaa content"));
		files.add(createFile(project, "secondfile", "bbb content"));
	}

	@After
	public void after() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		for (IProject project : root.getProjects()) {
			if (!project.isOpen()) {
				project.open(null);
			}
		}

		root.delete(true, null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkNullURL() {
		new RecommenderAPIProvider(null, USER_KEY, TOKEN);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkNullUserKey() {
		new RecommenderAPIProvider(URL, null, TOKEN);
	}

	@Test(expected=IllegalArgumentException.class)
	public void checkNullToken() {
		new RecommenderAPIProvider(URL, USER_KEY, null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void requestAnalyses_nullFiles() throws ClientProtocolException, IOException, RecommenderAPIException, CoreException {
		provider.requestAnalyses(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void requestAnalyses_emptyFiles() throws ClientProtocolException, IOException, RecommenderAPIException, CoreException {
		provider.requestAnalyses(Collections.EMPTY_SET);
	}
	
	@Test
	public void requestAnalyses_createPost() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(JSON.getBytes()));
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		final ArgumentCaptor<HttpPost> argumentCaptor = ArgumentCaptor.forClass(HttpPost.class);

		provider.requestAnalyses(files);
		
		verify(httpClient).execute(argumentCaptor.capture());
		HttpPost httpPost = argumentCaptor.getValue();
		String filesString = IOUtils.toString(httpPost.getEntity().getContent());

		assertThat(httpPost.getURI().toString(), startsWith(URL));
		assertThat(httpPost.getURI().toString(), containsString("?user_key=" + USER_KEY));
		assertThat(httpPost.getHeaders("Authorization")[0].getValue(), is(TOKEN));
		assertThat(filesString, containsString("firstfile"));
		assertThat(filesString, containsString("aaa content"));
		assertThat(filesString, containsString("secondfile"));
		assertThat(filesString, containsString("bbb content"));
	}

	@Test
	public void requestAnalyses_responseOK() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(httpResponse.getEntity()).thenReturn(httpEntity);
		when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(JSON.getBytes()));
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		String jobID = provider.requestAnalyses(files);
		verify(httpClient, atLeastOnce()).close();

		assertThat(jobID, is("12345"));
	}

	@Test(expected=RecommenderAPIException.class)
	public void requestAnalyses_responseOther() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

		provider.requestAnalyses(files);
		verify(httpClient, atLeastOnce()).close();
	}

	@Test(expected=RecommenderAPIException.class)
	public void requestAnalyses_clientException() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpPost.class))).thenThrow(IOException.class);

		provider.requestAnalyses(files);
		verify(httpClient, atLeastOnce()).close();
	}

	@Test(expected=IllegalArgumentException.class)
	public void analysesFinished_nullJob() throws ClientProtocolException, IOException, RecommenderAPIException {
		provider.analysesFinished(null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void analysesFinished_emptyStringJob() throws ClientProtocolException, IOException, RecommenderAPIException {
		provider.analysesFinished("");
	}
	
	@Test
	public void analysesFinished_createGet() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		final ArgumentCaptor<HttpGet> argumentCaptor = ArgumentCaptor.forClass(HttpGet.class);

		provider.analysesFinished(JOB);
		verify(httpClient).execute(argumentCaptor.capture());
		HttpGet httpGet = argumentCaptor.getValue();

		assertThat(httpGet.getURI().toString(), startsWith(URL));
		assertThat(httpGet.getURI().toString(), containsString("?user_key=" + USER_KEY));
		assertThat(httpGet.getHeaders("Authorization")[0].getValue(), is("Bearer mytoken"));
		assertThat(httpGet.getURI().toString(), containsString(JOB));
	}

	@Test
	public void analysesFinished_responseOK() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);

		assertThat(provider.analysesFinished(JOB), is(true));
		verify(httpClient, atLeastOnce()).close();
	}

	@Test
	public void analysesFinished_responseAccepted() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_ACCEPTED);

		assertThat(provider.analysesFinished(JOB), is(false));
		verify(httpClient, atLeastOnce()).close();
	}

	@Test(expected=RecommenderAPIException.class)
	public void analysesFinished_responseOther() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
		when(httpResponse.getStatusLine()).thenReturn(statusLine);
		when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

		provider.analysesFinished(JOB);
		
		verify(httpClient, atLeastOnce()).close();
	}

	@SuppressWarnings("unchecked")
	@Test(expected=RecommenderAPIException.class)
	public void analysesFinished_clientException() throws ClientProtocolException, IOException, RecommenderAPIException {
		when(httpClient.execute(any(HttpGet.class))).thenThrow(IOException.class);

		provider.analysesFinished(JOB);
		
		verify(httpClient, atLeastOnce()).close();
	}

	private class RecommenderAPIProviderWithCustomClient extends RecommenderAPIProvider {

		public RecommenderAPIProviderWithCustomClient(String url, String userKey, String token) {
			super(url, userKey, token);
		}
		
		@Override
		protected CloseableHttpClient createClient() {
			return httpClient;
		}
	}
	
	private IFile createFile(IProject project, String name, String content) throws CoreException{
		IFile file = project.getFile(name);
		file.create(new ByteArrayInputStream(content.getBytes()), IResource.NONE, null);
		return file;
	}

	private IProject createProject(String name) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(name);
		project.create(new NullProgressMonitor());
		project.open(null);
		return project;
	}
}
