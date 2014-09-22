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
package com.ovrhere.android.careerstack.model;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.asyncmodel.AsyncModel;
import com.ovrhere.android.careerstack.model.asyncmodel.RunnableHeadlessFragment;
import com.ovrhere.android.careerstack.model.careersstackoverflow.CareerStackOverflowRssRequest;

/** Async model for the stack overflow rss career feed.
 * Ensure that you call #dispose() in onDestroy().
 * @author Jason J.
 * @version 0.2.0-20140921
 */
public class CareersStackOverflowModel extends AsyncModel 
	implements CareerStackRequestParserWrapper.OnFeedbackListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = CareersStackOverflowModel.class
			.getSimpleName();
	/** The log tag to use. */
	final static private String LOGTAG = CLASS_NAME;
	/** The boolean for debugging. */
	final static private boolean DEBUG = false;
	/** The boolean for verbose debugging. */
	final static private boolean VERBOSE = false;
	
	/** The tag for this fragment. */
	final static private String RUNNABLE_HEADLESS_FRAG_TAG = 
			CLASS_NAME+"."+RunnableHeadlessFragment.class.getSimpleName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start public constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Bundle key. The value of tags. String */
	final static public String KEY_TAG_TEXT = 
			CLASS_NAME + ".KEY_TAG_TEXT";
	/** Bundle key. The value of keyword. String */
	final static public String KEY_KEYWORD_TEXT = 
			CLASS_NAME + ".KEY_KEYWORD_TEXT";
	/** Bundle key. The value of location. String. */
	final static public String KEY_LOCATION_TEXT = 
			CLASS_NAME + ".KEY_LOCATION_TEXT";
	/** Bundle key. The whether the remote check is set. Boolean. */
	final static public String KEY_REMOTE_ALLOWED = 
			CLASS_NAME + ".KEY_REMOTE_ALLOWED";
	/** Bundle key. The whether the relocation check is set. Boolean. */
	final static public String KEY_RELOCATE_OFFER = 
			CLASS_NAME + ".KEY_RELOCATE_OFFER";
	/** Bundle Key. The query distance. Int. */
	final static public String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	/** Bundle Key. Whether to use km (true) or miles (false). 
	 * If omitted, miles assumed. Boolean. */
	final static public String KEY_USE_METRIC = 
			CLASS_NAME + ".KEY_USE_METRIC";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Queries an rss feed for results. Accompanied by {@link Bundle} with
	 * keys. Responds via {@link #REPLY_RECORDS_RESULT}. 
	 * <p>This query is paused during
	 * {@link #dispose()} and would need resuming via 
	 * {@link #REQUEST_RESUME_QUERY}.*/
	final static public int REQUEST_RECORD_QUERY = 0x001;
	
	/** Requests that any running queries be paused. Note that
	 * the results are paused, the query runs. */
	final static public int REQUEST_PAUSE_QUERY = 0x002;
	/** Requests that any paused queries be resumed. */
	final static public int REQUEST_RESUME_QUERY = 0x003;
	
	/** Requests that the query be cancelled. */
	final static public int REQUEST_QUERY_CANCEL = 0x004;
	
	/** Reply of relevant records. 
	 * Accompanied by a {@link List} of {@link CareerItem}. */
	final static public int REPLY_RECORDS_RESULT = 0x101;
	
	/** Notification for starting update from web.*/
	final static public int NOTIFY_STARTING_QUERY = 0x201;
	/** Notification for paused query. */
	final static public int NOTIFY_QUERY_PAUSED = 0x202;
	/** Notification for resuming query.*/
	final static public int NOTIFY_QUERY_RESUMED = 0x203;
	/** Notification for cancelling query.*/
	final static public int NOTIFY_CANCELLED_QUERY = 0x204;
	
	/** The error for when a request times out. */
	final static public int ERROR_REQUEST_TIMEOUT = 0x404;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The fragment manager for getting the fragment. */
	private FragmentManager mfragManager = null;
	/** The current request wrapper. */
	private CareerStackRequestParserWrapper currentRequestWrapper =  null;
	
	/** Initializes asynchronous model. 
	 * @param activity The application's activity. */
	public CareersStackOverflowModel(FragmentActivity activity) {
		this.mfragManager = activity.getSupportFragmentManager();
		getHeadlessFrag();
		//prepare the headless fragment in advance.
		RunnableHeadlessFragment runFrag = getHeadlessFrag();
		if (runFrag.getRunnable() != null){ //if there are any runnables
			try {
				currentRequestWrapper = (CareerStackRequestParserWrapper) 
						runFrag.getRunnable();
				//update any running to the current object
				currentRequestWrapper.setFeedbackListener(this);
			} catch (ClassCastException e){}
		}
	}
	
	@Override
	public int sendMessage(int what, Bundle data) {
		switch (what) {
		case REQUEST_RECORD_QUERY:
			buildAndSendRequest(data);
			return 0;
		default:
			//break;
		}
		return super.sendMessage(what, data);
	}
	
	@Override
	public int sendMessage(int what, Object object) {
		switch (what) {
		case REQUEST_QUERY_CANCEL:
			if (currentRequestWrapper != null){
				currentRequestWrapper.cancel();
			}
			notifyHandlers(REQUEST_QUERY_CANCEL, null);
			return 0;
		case REQUEST_PAUSE_QUERY:
			if (currentRequestWrapper != null && currentRequestWrapper.isRunning()){
				currentRequestWrapper.pause();
			}
			notifyHandlers(NOTIFY_QUERY_PAUSED, null);
			return 0;
		case REQUEST_RESUME_QUERY:
			if (currentRequestWrapper != null && currentRequestWrapper.isRunning()){
				currentRequestWrapper.resume();
			}
			notifyHandlers(NOTIFY_QUERY_RESUMED, null);
			return 0;	
		default:
			//break;
		}
		return -1;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		if (currentRequestWrapper != null){
			currentRequestWrapper.pause();
		}
		mfragManager = null;
		if (DEBUG){
			Log.d(LOGTAG, "disposed");
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Starts and sends request. 
	 * @param args The arguments for the request.
	 */
	private void buildAndSendRequest(Bundle args){
		CareerStackOverflowRssRequest request = buildQuery(args);
		if (DEBUG){
			Log.d(LOGTAG, "Processing request: " + request.toString());
		}		
		currentRequestWrapper = new CareerStackRequestParserWrapper(request, this);
		
		RunnableHeadlessFragment frag = getHeadlessFrag();
		try {
			CareerStackRequestParserWrapper req = 
					(CareerStackRequestParserWrapper) frag.getRunnable();
			if (req != null && req.isRunning()){
				req.cancel(); 
				//cancel any pre-running requests
			}
		} catch (ClassCastException e){};
		frag.setRunable(currentRequestWrapper);
		frag.startThread();
		//notify listeners
		notifyHandlers(NOTIFY_STARTING_QUERY, null);
	}
	
	/** Using the {@link CareerStackOverflowRssRequest#Builder} a request is build
	 * based upon the arguments supplied in the bundle.
	 * @param args The args of the query.
	 * @return The final runnable query waiting to be run.	 */
	private CareerStackOverflowRssRequest buildQuery(Bundle args){
		CareerStackOverflowRssRequest.Builder queryBuilder = 
				new CareerStackOverflowRssRequest.Builder();
		//string
		String tags = args.getString(KEY_TAG_TEXT);
		String keywords = args.getString(KEY_KEYWORD_TEXT);
		String location = args.getString(KEY_LOCATION_TEXT);
		//checks
		boolean remoteAllowed = args.getBoolean(KEY_REMOTE_ALLOWED);
		boolean relocationOffered = args.getBoolean(KEY_RELOCATE_OFFER);
		//distance
		int distance = args.getInt(KEY_DISTANCE, -1);
		boolean useMetric =  args.getBoolean(KEY_USE_METRIC);
		boolean locationPresent = false;
		
		if (tags != null && !tags.isEmpty()){
			queryBuilder.setTags(tags);
		}
		if (keywords != null && !keywords.isEmpty()){
			queryBuilder.setSearchTerm(keywords);
		}
		if (location != null && !location.isEmpty()){
			queryBuilder.setLocation(location);
			locationPresent = true;
		}
		//semantically the same as if we checked it.
		queryBuilder.setRemote(remoteAllowed);
		queryBuilder.setRelocation(relocationOffered);
		
		//no distance without location
		if (locationPresent && distance > 0){
			queryBuilder.setDistance(distance, useMetric);
		}
		
		return queryBuilder.create();
	}
	
	/** Gets headless fragment if found in manager. Otherwise, places it into 
	 * the manager
	 * @return The headless fragment to attach runnables to.	 */
	private RunnableHeadlessFragment getHeadlessFrag() {
		RunnableHeadlessFragment runFrag = (RunnableHeadlessFragment)
				mfragManager.findFragmentByTag(RUNNABLE_HEADLESS_FRAG_TAG);
		if (runFrag == null ){
			runFrag = new RunnableHeadlessFragment();
			mfragManager.beginTransaction()
					.add(runFrag, RUNNABLE_HEADLESS_FRAG_TAG)
					.commit();
		}		
		return runFrag;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onResults(List<CareerItem> careerList) {
		notifyHandlers(REPLY_RECORDS_RESULT, careerList);
		currentRequestWrapper = null;
		getHeadlessFrag().setRunable(null);	
	}
	
	@SuppressWarnings("unused")
	@Override
	public void onException(Exception exception) {
		if (DEBUG){
			Log.d(LOGTAG, "Exception in object: " +this.hashCode());
		}
		try {
			throw exception;
		} catch  (SocketTimeoutException e){
			if (DEBUG && VERBOSE){ 
				Log.e(LOGTAG, "Timeout: "+e);
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_TIMEOUT, null);
		} catch (IOException e){
			if (DEBUG && VERBOSE){
				Log.e(LOGTAG, "IO exception, likely the xml giving up: "+e);
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_FAILED, null);
		} catch (Exception e){
			if (DEBUG && VERBOSE){
				Log.e(LOGTAG, "This. This should not be happening... "+e);
				e.printStackTrace();
			}
			notifyHandlers(ERROR_REQUEST_FAILED, null);
		}
	}

}
