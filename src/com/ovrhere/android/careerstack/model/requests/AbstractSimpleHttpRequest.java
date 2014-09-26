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
package com.ovrhere.android.careerstack.model.requests;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import android.util.Log;

/** Outlines the basics of a simple GET http request. The request is set in
 * {@link #preparedRequest} and executed in the {@link #run()}.
 * Only allows requests to run sequentially for one object. Do not forget to 
 * call {@link #setOnRequestEventListener(OnRequestEventListener)}.
 * @author Jason J.
 * @version 0.2.0-20140920
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
	/** Lock for {@link #run()} to synchronize on. */
	final private Object reqLock = new Object();
	
	/** The request timeout period in milliseconds. */
	private int requestTimeout = DEFAULT_TIMEOUT;
	
	/** The urlConntection used in #run(). */
	private HttpURLConnection urlConnection = null;
	/** The input stream used in #run(). */
	private InputStream input = null;
			
	/** The required listener for the request. */
	volatile protected OnRequestEventListener mRequestEventListener = null;	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	/** Sets a request event listener. 
	 * @param onRequestEventListener The implementer of this interface	 */
	public void setOnRequestEventListener(
			OnRequestEventListener onRequestEventListener) {
		this.mRequestEventListener = onRequestEventListener;
	}
	/** Sets how long in milliseconds before the request gives up. Will not 
	 * take effect during a request. This will cause a {@link SocketTimeoutException}.
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
	
	/** Note this will cancel the entire thread, causing it to throw an
	 * {@link InterruptedException}.
	 *  Will <code>null</code> all {@link InputStream}s.*/
	public void cancel(){
		Thread t =Thread.currentThread();
		synchronized (t) {
			disconnect();
			t.interrupt();
		}
	}
	
	/** Returns the prepared request to perform in {@link #run()}
	 * @return A valid request to run.	 */
	abstract protected String getPreparedRequest();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Over ridge methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public String toString() {
		return super.toString()+
				String.format("[request: %s]", getPreparedRequest());
	}
	
	@Override
	public void run() {
		synchronized (reqLock) {
			final int QUERY_TIMEOUT = requestTimeout;
			final String preparedRequest = getPreparedRequest();
			
			int responseCode = 0;
			if (Thread.interrupted()){
				return;
			}
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

				if (Thread.interrupted()){
					return;
				}
				input = new BufferedInputStream(urlConnection.getInputStream());
				
				if (mRequestEventListener != null){
					mRequestEventListener.onResponseCode(responseCode);
					mRequestEventListener.onStart(input);
				}
				if (Thread.interrupted()){
					return;
				}
								
				if (mRequestEventListener != null){
					mRequestEventListener.onComplete();	
				}
				/*
				 * Exceptions begin here.
				 */
			} catch (MalformedURLException e){
				if (DEBUG){
					Log.e(LOGTAG, "Poorly formed request: "+e);
				}
				onException(responseCode, e);
				
			} catch (SocketTimeoutException e){
				onException(responseCode, e);
				
			} catch(IOException e){
				if (DEBUG){
					Log.e(LOGTAG, 
							"Cannot perform request (response code:"+
							responseCode+"): "+e);
				}
				onException(responseCode, e);
				
			}  catch (Exception e){
				if (DEBUG){
					Log.w(LOGTAG, "Unexpected error occurred: " + e);
				}
				if (mRequestEventListener != null){
					mRequestEventListener.onException(e);
				}
				
			} finally {
				disconnect();
			}
		}
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Processes the exception requests safely. */
	private void onException(int responseCode, IOException e) {
		if (mRequestEventListener != null){
			mRequestEventListener.onResponseCode(responseCode);
			mRequestEventListener.onException(e);
		}
	}
	
	/** Disconnect cleanup. */
	private void disconnect() {
		try {
			if (urlConnection != null ){
				urlConnection.disconnect();
				urlConnection = null;
			}
			if (input != null){
				input.close();
				input = null;
			}
		} catch (IOException e) {}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Provides a list of methods to notify the listener of runnable events 
	 * and results.
	 *  @version 0.2.0-20140914	 */
	public interface OnRequestEventListener {
		/** Sends any exceptions encountered to the listener.
		 * @param e The forwarded exception, if any.
		 * Possible exceptions include:
		 * <ul><li> {@link MalformedURLException} - Poorly formed request</li>
		 * <li> {@link SocketTimeoutException} - Request timed out according
		 * to value set</li>
		 * <li> {@link IOException} - During exception an IO error occurred </li> 
		 * <li> {@link Exception} - Unknown exception </li>
		 * </ul>	 */
		public void onException(Exception e);
		/** Sends any response code received.
		 * @param responseCode The HTTP response codes or 0.		 */
		public void onResponseCode(int responseCode);
		/** Sent when the request starts
		 * @param in The input stream being used		 */
		public void onStart(InputStream in);
		/** When the request run has been concluded successfully. */
		public void onComplete();
	}

}

