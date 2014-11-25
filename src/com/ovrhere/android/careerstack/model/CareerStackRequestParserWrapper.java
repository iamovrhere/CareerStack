package com.ovrhere.android.careerstack.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.careersstackoverflow.CareerStackOverflowRssRequest;
import com.ovrhere.android.careerstack.model.careersstackoverflow.CareersStackOverflowRssXmlParser;
import com.ovrhere.android.careerstack.model.requests.AbstractSimpleHttpRequest;

/** The request + parser wrapper for easier handling (as both
 * will happen on the same thread. This allows for pauses and resuming of the 
 * process.
 * @author Jason J.
 * @version 0.2.0-20141125	 */
class CareerStackRequestParserWrapper implements Runnable,
	AbstractSimpleHttpRequest.OnRequestEventListener,
	CareersStackOverflowRssXmlParser.OnAsyncUpdateListener {
	
	/** Class name for debugging purposes. */
	final static private String LOGTAG = 
				CareerStackRequestParserWrapper.class.getSimpleName();
	/** Debugging bool. */
	final static private boolean DEBUG = false;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The the task has started. */
	final private AtomicBoolean running = new AtomicBoolean(false);	
	/** The actual request. */
	final private AbstractSimpleHttpRequest mRequest;	
	/** The timeout period. */
	final private long TIMEOUT;
	
	/** The current parser. */
	private CareersStackOverflowRssXmlParser mParser =  null;
	
	/** The final results. Default is null. */
	private List<CareerItem> resultItems = null;
	/** The current start time. */
	private long startTime = -1;	

	
	/** The results listener. */
	volatile private OnFeedbackListener mListener = null;
	/** If the runnable is currently paused. */
	volatile private boolean isPaused = false;
		
	
	/** Wraps the {@link CareerStackOverflowRssRequest} & 
	 * {@link CareersStackOverflowRssXmlParser} to handle both in same runnable.
	 * @param request The request to send
	 * @param listener The feedback listener; for results and more. 
	 */
	public CareerStackRequestParserWrapper(AbstractSimpleHttpRequest request,
			OnFeedbackListener listener){
		this.mListener = listener;
		this.mRequest = request;
		this.mRequest.setOnRequestEventListener(this);
		this.TIMEOUT = request.getRequestTimeout();
	}
	/** Set results listener. */
	public void setFeedbackListener(OnFeedbackListener listener) {
		this.mListener = listener;
	}
	/** Note this will cancel the entire thread, causing it to throw an 
	 * InterruptedException. Will null all InputStreams. */
	public void cancel(){
		pauseParser(false); //always must be running at cancel
		mRequest.cancel();
	}
	
	/** Pauses the wrapper results response. 
	 * If <code>pause > request timeout</code>, it gives up. Same as calling
	 * {@link #pause(boolean)} with <code>false</code>. */
	public void pause(){
		pause(false);
	}
	
	/** Pauses the request/parsing. If <code>pause > request timeout</code>, it gives up.		 
	 * @param force <code>true</code> to force a full pause (with results)
	 * <code>false</code> to only pause the wrapper/results*/
	public void pause(boolean force){
		isPaused = true;
		if (force){
			pauseParser(true);
		}
		if (DEBUG){
			Log.d(LOGTAG, "paused");
		}	
	}
	
	/** Resumes the request/parsing. */
	public void resume(){	
		isPaused = false;
		pauseParser(false);
		if (DEBUG){
			Log.d(LOGTAG, "resumed");
		}	
	}
	
	/** Returns <code>true</code> if #run() has run/is running. */
	public boolean isRunning(){
		return running.get();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods.
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Pauses and resumes parsed based on value of pause
	 *  @param pause <code>true</code> to pause, <code>false</code> to resume
	 */
	private void pauseParser(boolean pause){
		if (mParser != null){
			if (pause){
				mParser.pause();
			} else {
				mParser.resume();
			}
		} 
	}
	
	/** Blocking. Checks the pause time every 200 ms. If pause still waiting
	 * after timeout time, return false.
	 * @return <code>true</code> if to continue is lifted, 
	 * <code>false</code> if we are still paused or thread interupted.
	 * @throws InterruptedException
	 */
	private boolean checkContinue() throws InterruptedException{
		final int waitTime = 200;
		while (isPaused){
			if (DEBUG){
				Log.d(LOGTAG, "Sleeping for " + waitTime + " millis  ");
			}
			Thread.sleep(waitTime);
			if (System.currentTimeMillis() - startTime > TIMEOUT + waitTime ){
				return false;
			}
		}
		if (Thread.interrupted()){
			return false;
		}
		return true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The results interface; sends feedback on the entire procedure.
	 * @version 0.2.0-20141125 */
	public static interface OnFeedbackListener {
		/** Publishes results when available. */
		public void onResults(List<CareerItem> careerList);
		/** Sends exceptions if encountered. */
		public void onException(Exception exception);
		
		/** Sends the number of results parsed thus far. 
		 * @param count The parse count
		 * @param total The full total of expected results		 */
		public void onAsyncCountUpdate(int count, int total);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Override methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void run() {
		try {
			running.set(true);;
			isPaused = false;
			startTime = System.currentTimeMillis();
			mRequest.run();
		} catch (Exception e){
			running.set(false);
			mListener.onException(e);
		}
	}
	
	@Override
	public void onStart(InputStream in) {
		try {	
			if (checkContinue()){
				mParser = new CareersStackOverflowRssXmlParser(this);
				resultItems = mParser.parseXmlStream(in);
			}
			checkContinue();
			
		} catch (XmlPullParserException e) {
			mListener.onException(e);
		} catch (IOException e) {
			mListener.onException(e);
		} catch (InterruptedException e) {
			mListener.onException(e);
		} finally {
			running.set(false);
		}
	}	
	
	@Override
	public void onResponseCode(int responseCode) {
		if (DEBUG){
			Log.d(LOGTAG, "Reponse code: " + responseCode);
		}		
	}
	
	@Override
	public void onException(Exception exception) {
		if (DEBUG){
			Log.d(LOGTAG, "Exception in object: " +this.hashCode());
		}
		mListener.onException(exception);
		running.set(false);
	}
	
	@Override
	public void onComplete() {
		if (DEBUG){
			Log.d(LOGTAG, "request: onComplete in: " + this.hashCode());
		}
		try {
			if (checkContinue()){
				if (resultItems != null){
					mListener.onResults(resultItems);
				} else {
					//we will give a timeout
					mListener.onException(new SocketTimeoutException());
				}
			}			
		} catch (InterruptedException e) {
			mListener.onException(e);
		} finally {
			running.set(false);
		}
	}
	
	//start CareersStackOverflowRssXmlParser listeners
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.model.careersstackoverflow.CareersStackOverflowRssXmlParser.OnAsyncUpdateListener#onParseCountUpdate(int, int)
	 */
	@Override
	public void onParseCountUpdate(int parseCount, int total) {
		mListener.onAsyncCountUpdate(parseCount, total);
	}
	//end CareersStackOverflowRssXmlParser listeners
}