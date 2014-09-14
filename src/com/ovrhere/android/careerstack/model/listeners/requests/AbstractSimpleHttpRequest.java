/*
 * Copyright 2014 Jason J.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ovrhere.android.careerstack.model.listeners.requests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

/** Outlines the basics of a simple GET http request. The request is set in
 * {@link #preparedRequest} and executed in the {@link #run()}.
 * Only allows requests to run sequentially for one object.
 * @author Jason J.
 * @version 0.1.0-20140914
 */
public abstract class AbstractSimpleHttpRequest implements Runnable{
	/** The logtag for debugging. */
	final static private String LOGTAG = AbstractSimpleHttpRequest.class
			.getSimpleName();
	/** Whether or not to output debugging logs. */ 
	final static private boolean DEBUG = false;
	
	/** The default timeout period in milliseconds. */ 
	final static protected int DEFAULT_TIMEOUT = 10000; //ms
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The request timeout period in milliseconds. */
	private int requestTimeout = DEFAULT_TIMEOUT;
	
	/** The prepared request ready for execution. */
	protected String preparedRequest = "";
	/** The required listener for the request. */
	protected OnRequestEventListener mRequestEventListener = null;	
	
		
	/** Sets a request event listener. 
	 * @param onRequestEventListener The implementer of this interface	 */
	public void setOnRequestEventListener(
			OnRequestEventListener onRequestEventListener) {
		this.mRequestEventListener = onRequestEventListener;
	}
	/** Sets how long in milliseconds before the request gives up. Will not 
	 * take effect during a request.
	 * @param requestTimeout The time in milliseconds	 */
	public void setRequestTimeout(int requestTimeout) {
		if (requestTimeout < 0){
			throw new IllegalArgumentException("Cannot give negative timeout");
		}
		this.requestTimeout = requestTimeout;
	}
	/** Returns the timeout period before giving up.
	 * @return The timeout in milliseconds	 */
	public long getRequestTimeout() {
		return requestTimeout;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Runnable
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void run() {
		synchronized (preparedRequest) {
			final int QUERY_TIMEOUT = requestTimeout;
			
			HttpURLConnection urlConnection = null;
			int responseCode = 0;
			try {
				URL url = new URL(preparedRequest);
				urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout((int) QUERY_TIMEOUT);
				urlConnection.setReadTimeout((int) QUERY_TIMEOUT);
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoInput(true);
			    // Start query
				urlConnection.connect();
				responseCode = urlConnection.getResponseCode();

				InputStream in = 
						new BufferedInputStream(urlConnection.getInputStream());
				
				mRequestEventListener.onStart(in);
			} catch (MalformedURLException e){
				if (DEBUG){
					Log.e(LOGTAG, "Poorly formed request: "+e);
				}
				mRequestEventListener.onException(e);
				return;
			} catch(IOException e){
				if (DEBUG){
					Log.e(LOGTAG, 
							"Cannot perform request (response code:"+
							responseCode+"): "+e);
				}
				mRequestEventListener.onResponseCode(responseCode);
				mRequestEventListener.onException(e);
				return;				
			}  catch (Exception e){
				if (DEBUG){
					Log.w(LOGTAG, "Unexpected error occurred: " + e);
				}
				mRequestEventListener.onException(e);
				return;
			}
			mRequestEventListener.onComplete();	
		}
		
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Provides a list of methods to notify the listener of runnable events 
	 * and results.
	 *  @version 0.2.0-20140914	 */
	public interface OnRequestEventListener {
		/** Sends any exceptions encountered to the listener.
		 * @param e The forwarded exception, if any.		 */
		public void onException(Exception e);
		/** Sends any response code received.
		 * @param responseCode The HTTP response codes or 0.		 */
		public void onResponseCode(int responseCode);
		/** Sent when the request starts
		 * @param in The input stream being used		 */
		public void onStart(InputStream in);
		/** When the request run has been concluded. */
		public void onComplete();
	}

}

