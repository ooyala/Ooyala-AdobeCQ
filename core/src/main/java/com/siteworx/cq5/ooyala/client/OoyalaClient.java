package com.siteworx.cq5.ooyala.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siteworx.cq5.ooyala.client.request.OoyalaQueryRequest;
import com.siteworx.cq5.ooyala.client.request.OoyalaRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.core.util.Base64;

/**
 * Copyright (c) 2012, Ooyala, Inc.
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * •	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * •	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation 
 *     and/or other materials provided with the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

/**
 * Ooyala REST API client. The public methods in this class are thread-safe.
 *
 * @author leonardo@siteworx.com
 * @author rboll@siteworx.com
 *
 */
public class OoyalaClient {
	
	private static final Logger log = LoggerFactory.getLogger(OoyalaClient.class);
	
	private final MessageDigest digest;
	private final Client client;
	
	/**
	 * Constructs the Ooyala Client.
	 *
	 * @throws NoSuchAlgorithmException Throws an exception if SHA-256 algorithm is not found by MessageDigest.
	 */
	public OoyalaClient(){
		try {
			digest = MessageDigest.getInstance("SHA-256");
			ClientConfig config = new DefaultClientConfig();

			client = new Client(new URLConnectionClientHandler(
					new HttpURLConnectionFactory() {
						Proxy p = null;
						@Override
						public HttpURLConnection getHttpURLConnection(URL url)
								throws IOException {
							if (p == null) {
								if (System.getProperties().containsKey("http.proxyHost")) {
									p = new Proxy(Proxy.Type.HTTP,
											new InetSocketAddress(
													System.getProperty("http.proxyHost"),
													Integer.getInteger("http.proxyPort", 80)));
								} else {
									p = Proxy.NO_PROXY;
								}
							}
							return (HttpURLConnection) url.openConnection(p);
						}
					}), config);
			client.setConnectTimeout(15000);//15 second timeout
			client.setReadTimeout(60000);//60 second timeout
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Sends the request to Ooyala, expecting a JSON response.
	 *
	 * @param request An OoyalaRequest.
	 * @return The JSON string returned by Ooyala's API.
	 * @throws OoyalaClientException
	 */
	public String request(OoyalaRequest request) throws OoyalaClientException{
		return request(request.getProtocol(), request.getHost(), request.getCredentials(), request.getMethod().name(), request.getRequestPath(), request.getParameters(), null);
	}
	
	/**
	 * Sends the request to Ooyala, expecting a JSON response.
	 *
	 * @param request An OoyalaQueryRequest.
	 * @return The JSON string returned by Ooyala's API.
	 * @throws OoyalaClientException
	 */
	public String request(OoyalaQueryRequest request) throws OoyalaClientException {
		return request(request.getProtocol(), request.getHost(), request.getCredentials(), request.getMethod().name(), request.getRequestPath(), request.getParameters(), request.getQueryParameters());
	}
	/**
	 * Sends the request to Ooyala, expecting a JSON response.
	 *
	 * @param protocol The protocol to use for the request.
	 * @param host The host to use for the request.
	 * @param credentials The {@link OoyalaApiCredential} object for authenticating the API request.
	 * @param method The HTTP method to be used when connecting to Ooyala. Usually GET.
	 * @param requestPath The request path for the type of object expected in the API response.
	 * @param parameters The request parameters.
	 * @param queryParameters The request query parameters.
	 * @return The JSON string returned by Ooyala's API.
	 * @throws OoyalaClientException
	 */
	private String request(String protocol, String host, OoyalaApiCredential credentials, String method, String requestPath, SortedMap<String, String> parameters, Map<String, String> queryParameters) throws OoyalaClientException {
		if(!parameters.containsKey("expires")){
			parameters.put("expires", Long.toString(((System.currentTimeMillis()/1000)+60)));
		}
		
		final StringBuilder requestBuilder = new StringBuilder(protocol).append("://").append(host).append(requestPath).append("?");
		
		final String signature = getSignature(credentials.getApiSecret(), method, requestPath, parameters, queryParameters);
		final String parameterString = getParameters(parameters, queryParameters, signature);

		final String request = requestBuilder.append(parameterString).toString();
		if(log.isDebugEnabled())log.debug("Ooyala: request URL = " + request);
		
		long start = System.nanoTime();
		final WebResource webResource = client.resource(request);
		if(log.isDebugEnabled())log.debug("Ooyala: request took "+(System.nanoTime()-start)+" ns.");
		
		final Builder builder = webResource.type(MediaType.APPLICATION_JSON_TYPE);
		
		try{
			final ClientResponse clientResponse = (ClientResponse) builder.get(ClientResponse.class);
			
			if (clientResponse.getStatus() != 200) {
				throw new OoyalaClientException("HTTP response code from REST request : " + clientResponse.getStatus() + " " + clientResponse.getEntity(String.class));
			}

			return (String) clientResponse.getEntity(String.class);
		}catch(UniformInterfaceException uie){
			throw new OoyalaClientException(uie);
		}catch(ClientHandlerException che){
			throw new OoyalaClientException(che);
		}
	}

	/**
	 * Concatenates the parameters, queryParameters, and signature into a url string.
	 *
	 * @param parameters The request parameters.
	 * @param queryParameters The request queryParameters.
	 * @param signature The signature.
	 * @return A URL encoded string of all parameters, queryParameters, and the signature.
	 */
	private String getParameters(SortedMap<String, String> parameters, Map<String, String> queryParameters, final String signature) {
		final StringBuilder parametersBuffer = new StringBuilder();
		parametersBuffer.append("signature").append("=").append(signature);
		for(final Entry<String, String> entry : parameters.entrySet()){
			parametersBuffer.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}
		if (queryParameters != null && queryParameters.size() > 0) {
			parametersBuffer.append("&where=");
			for (final Entry<String, String> entry : queryParameters.entrySet()) {
				if(log.isDebugEnabled())log.debug("key = " + entry.getKey() + ", value = " + entry.getValue());
				if (!entry.getKey().equals("label"))
					try {
						parametersBuffer.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
					}
				else {
					if(log.isDebugEnabled())log.debug("WENT TO LABELS INCLUDE");
					try {
						parametersBuffer.append("labels%20INCLUDES%20" + "'" + URLEncoder.encode(entry.getValue(), "UTF-8") + "'");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return parametersBuffer.toString();
	}

	/**
	 * Builds a signature for authenticating the request with Ooyala. See Ooyala documentation at http://api.ooyala.com/docs/v2 for more info.
	 * Note: This method assumes that no parameters from the parameters map or the queryParameters map will occur alphabetically after the word "where".
	 * 
	 * @param secret The API secret to use for digesting.
	 * @param method 
	 * @param requestPath The request path for the type of object expected in the API response.
	 * @param parameters The request parameters.
	 * @param queryParameters The request queryParameters.
	 * @return The string digest of all parameters concatenated.
	 */
	private String getSignature(String secret, String method, String requestPath, SortedMap<String, String> parameters, Map<String, String> queryParameters) {
		final StringBuilder parametersBuffer = new StringBuilder();
		parametersBuffer.append(secret).append(method).append(requestPath);
		for(final Entry<String, String> entry : parameters.entrySet()){
			parametersBuffer.append(entry.getKey()).append("=").append(entry.getValue());
		}
		if (queryParameters != null && queryParameters.size() > 0) {
			parametersBuffer.append("where=");
			for (final Entry<String, String> entry : queryParameters.entrySet()) {
				if(log.isDebugEnabled())log.debug("key = " + entry.getKey() + ", value = " + entry.getValue());
				if (!entry.getKey().equals("label"))
					parametersBuffer.append(entry.getKey()).append("=").append(entry.getValue());
				else {
					if(log.isDebugEnabled())log.debug("WENT TO LABELS INCLUDE");
					parametersBuffer.append("labels INCLUDES " + "'" + entry.getValue() + "'");
				}
			}
		}
		try{
			return generateURLEncodedSignature(parametersBuffer.toString());
		}catch(CloneNotSupportedException cnse){//these exceptions are highly unlikely to ocurr, so lets just rethrow them as RTE, in case they do happen
			throw new RuntimeException(cnse);
		}catch(UnsupportedEncodingException uee){
			throw new RuntimeException(uee);
		}
	}
	
	/**
	 * URL encodes the query and digest for transportation.
	 *
	 * @param query The query string.
	 * @return The URL encoded query string.
	 * @throws CloneNotSupportedException
	 * @throws UnsupportedEncodingException
	 */
	private String generateURLEncodedSignature(String query) throws CloneNotSupportedException, UnsupportedEncodingException {
		final MessageDigest digester = (MessageDigest) digest.clone();
		digester.reset();
	    final String urlEncodedSignature = URLEncoder.encode(new String(Base64.encode(digester.digest(query.getBytes("UTF-8"))), "UTF-8").substring(0, 43), "UTF-8");
	    if(log.isDebugEnabled()) log.debug("SIGNATURE = " + urlEncodedSignature);
		return urlEncodedSignature;
	}
	
}
