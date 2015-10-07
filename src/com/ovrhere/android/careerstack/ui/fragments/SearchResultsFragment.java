package com.ovrhere.android.careerstack.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.CareersStackOverflowModel;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.adapters.CareerItemRecyclerAdapter;
import com.ovrhere.android.careerstack.ui.adapters.CareerItemRecyclerAdapter.OnCareerItemClickListener;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/**
 * The fragment to perform searches and display cursory results.
 * Expects Activity to implement {@link OnFragmentInteractionListener} and 
 * will throw {@link ClassCastException} otherwise.
 * @author Jason J.
 * @version 0.9.0-20151007
 */
public class SearchResultsFragment extends Fragment 
	implements OnClickListener, OnCareerItemClickListener, Handler.Callback {
	
	/** Class name for debugging purposes. */
	private static final String CLASS_NAME = SearchResultsFragment.class
			.getSimpleName();	
	/**Logtag for debugging purposes. */
	private static final String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	private static final boolean DEBUG = false;
	
		
	/** Bundle key. This stores {@link #mCareerList}.
	 *  List<Parcelable>/List<CareerItem> */
	private static final String KEY_CAREER_LIST = 
			CLASS_NAME +".KEY_CAREER_LIST";
	/** Bundle key. Whether or not the fragment is currently loading results
	 * {@link #mIsLoadingResults}. Boolean */
	private static final String KEY_IS_LOADING_RESULTS = 
			CLASS_NAME +".KEY_IS_LOADING_RESULTS";
	/** Bundle key. Whether or not the fragment has failed with last results
	 * {@link #mResultsTimeout}. Boolean */
	private static final String KEY_RESULTS_TIMEOUT = 
			CLASS_NAME +".KEY_RESULTS_TIMEOUT";
	
	/** Bundle key. The previous query state. Bundle. */
	private static final String KEY_PREV_QUERY = 
			CLASS_NAME +".KEY_PREV_QUERY";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start public keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/*
	 * Using a quick cheat here: Could rebuilt the argument bundles for the model
	 * OR we can just rename our keys to be the model's and reuse state/argument
	 * bundles (with mild augmentation if necessary).
	 * 
	 * Option 2 it is!
	 */
	
	/** Bundle key. The value of tags. String */
	public static final String KEY_TAG_TEXT = 
			CareersStackOverflowModel.KEY_TAG_TEXT;
	/** Bundle key. The value of keyword. String */
	public static final String KEY_KEYWORD_TEXT = 
			CareersStackOverflowModel.KEY_KEYWORD_TEXT;
	/** Bundle key. The value of location. String. */
	public static final String KEY_LOCATION_TEXT = 
			CareersStackOverflowModel.KEY_LOCATION_TEXT;
	/** Bundle key. The whether the remote check is set. Boolean. */
	public static final String KEY_REMOTE_ALLOWED = 
			CareersStackOverflowModel.KEY_REMOTE_ALLOWED;
	/** Bundle key. The whether the relocation check is set. Boolean. */
	public static final String KEY_RELOCATE_OFFER = 
			CareersStackOverflowModel.KEY_RELOCATE_OFFER;
	/** Bundle Key. The current distance in the seek bar. Int. */
	public static final String KEY_DISTANCE = 
			CareersStackOverflowModel.KEY_DISTANCE;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/** The container for retry. */
	private View mRetryContainer = null;
	/** The container for canceling + progress bar. */
	private View mProgressContainer = null;
	/** The list of results. */
	private RecyclerView mResultsList = null;
	
	/** The loading progress, set to blank by default. */
	private TextView mTv_progress = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The list of elements. Should never be <code>null</code>. */
	private ArrayList<CareerItem> mCareerList = new ArrayList<CareerItem>();	
	/** The adapter for the results. */
	private CareerItemRecyclerAdapter mResultAdapter = null;
	
	
	/** If the fragment is currently loading results. Default false. */
	private boolean mIsLoadingResults = false;
	
	/** If the fragment failed during last load.  Default false. */
	private boolean mResultsTimeout = false;
		
	/** The fragment request listener from activity. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	
	/** Used to determine if views are valid. 
	 * Set true in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * false in {@link #onDestroyView()} */
	@SuppressWarnings("unused")
	private boolean mViewBuilt = false;
	
	/** The async model for making requests. */
	private CareersStackOverflowModel mAsyncModel = null;
	/** The args for previous query performed. Starts off as args. */ 
	private Bundle mPrevQuery = null;
	
	/** The shared preferences to used. */
	private SharedPreferences mPrefs = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Factory method to create fragment.
	 * @param args The list of search terms to start with.
	 * Consult the list of public keys.
	 * @return A new instance of fragment SearchResultsFragment.
	 */
	public static SearchResultsFragment newInstance(Bundle args) {
		SearchResultsFragment fragment = new SearchResultsFragment();
		fragment.setArguments(args);
		return fragment;
	}

	public SearchResultsFragment() {}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		buildSaveState(outState);
	}
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceUtils.getPreferences(getActivity());
		
		mAsyncModel = new CareersStackOverflowModel(getActivity());
		mAsyncModel.addMessageHandler(new Handler(this));		
	}
	
	@Override
	public void onDestroy() {
		synchronized (mAsyncModel) {
			super.onDestroy();
			if (mIsLoadingResults){
				mAsyncModel.sendMessage(CareersStackOverflowModel.REQUEST_PAUSE_QUERY);
			}
			mAsyncModel.dispose();		
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_search_results, container,
				false);
		
		initButtons(rootView);
		initOutputs(rootView);
				
		Bundle backStackState = 
				mFragInteractionListener.onPopSavedStateRequest();
		
		if (backStackState != null){
			debugSavedState(backStackState);
			processArgBundle(backStackState);
			
		} else if (savedInstanceState != null){
			processArgBundle(savedInstanceState);	
			
		} else if (getArguments() != null) {
			Bundle args = getArguments();
			processArgBundle(args);
			
			//if no values are set and we aren't loading, request.
			if (!mIsLoadingResults){ 
				sendModelRequest(args);
			}
			showLoadingBlock(false);
		} 
		
		mViewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mIsLoadingResults){
			mAsyncModel.sendMessage(CareersStackOverflowModel.REQUEST_RESUME_QUERY);
		}
		//prevQuery still empty?
		if (mPrevQuery == null){
			mPrevQuery = getArguments(); //still set args
		}
	}
	
	
	@Override
	public void onDestroyView() {
		synchronized (mAsyncModel) {
			Bundle state = new Bundle();
			buildSaveState(state); //holds the state
			mFragInteractionListener.onHoldSavedStateRequest(state);
			
			super.onDestroyView();
			mViewBuilt = false;
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mFragInteractionListener = 
					(OnFragmentInteractionListener) activity;
		} catch (ClassCastException e){
			Log.e(LOGTAG, "Activity must implement :" +
					OnFragmentInteractionListener.class.getSimpleName());
			throw e;
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Request updates start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Prepares and sends request off to model while setting views.
	 * Clears and sets the internal previous request.
	 * @param args The args to send according to the KEYS given.  */
	public void sendRequest(Bundle args){
		//we are about to request, set up loading
		showLoadingBlock(true);
		
		if (mPrevQuery == null){
			mPrevQuery = new Bundle();
		}
		mPrevQuery.clear(); //clean the old request //TODO reallow
		
		sendModelRequest(args);		
	}

	
	/** Resends the previous request. If no request was previously sent, an
	 * empty request is sent.	 */
	public void retryRequest() {
		if (mPrevQuery == null){ //should never be null, but just in case
			mPrevQuery = new Bundle();
		}
		//we are about to request, set up loading
		showLoadingBlock(true);
		sendModelRequest(mPrevQuery);
	};
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the output views. */
	private void initOutputs(View rootView){
		mResultAdapter = new CareerItemRecyclerAdapter(getActivity(), this);
						
		mResultsList = (RecyclerView)
				rootView.findViewById(R.id.careerstack_searchResults_list_searchResults);
		mResultsList.setAdapter(mResultAdapter);
		mResultsList.setHasFixedSize(true);
		 // use a vertical linear layout manager
        LinearLayoutManager listContainer  = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        
        mResultsList.setLayoutManager(listContainer); 
        mResultsList.setItemAnimator(new DefaultItemAnimator());
		
		mRetryContainer =
				rootView.findViewById(R.id.careerstack_searchResults_layout_retry);
		mProgressContainer =
				rootView.findViewById(R.id.careerstack_searchResults_layout_progressBar);
		
		mTv_progress = (TextView)
				rootView.findViewById(R.id.careerstack_searchResults_text_progress);
	}
	
	
	/** Initializes buttons, finding and setting listeners. */
	private void initButtons(View rootView) {
		Button cancelSearch = (Button) 
				rootView.findViewById(R.id.careerstack_searchResults_button_cancel);
		cancelSearch.setOnClickListener(this);
		Button retrySearch = (Button) 
				rootView.findViewById(R.id.careerstack_searchResults_button_retry);
		retrySearch.setOnClickListener(this);
	}
	
	/** Processes the returning state and applies values to views
	 * Assumes views are valid.
	 * @param savedState The state to unpack
	 * @see #initInputs(View)
	 */
	private void processArgBundle(Bundle args){
		//safety first, always try restore block
		try {
			//if we began loading results before the view was recreated
			mIsLoadingResults = mIsLoadingResults || args.getBoolean(KEY_IS_LOADING_RESULTS, false);
			//if results timeout out before view was recreated
			mResultsTimeout = mResultsTimeout || args.getBoolean(KEY_RESULTS_TIMEOUT, false);
			
			if (mResultsTimeout){
				showRetryBlock(false);
			} else if (mIsLoadingResults){
				showLoadingBlock(false);
			} else {
				showResults(false);
			}
			
			if (args.getParcelableArrayList(KEY_CAREER_LIST) != null){
				try {
						mCareerList = args.getParcelableArrayList(KEY_CAREER_LIST);
						mResultAdapter.setCareerItems(mCareerList);
					} catch (ClassCastException e){
						Log.e(LOGTAG, "Career list mistmatched class?" + e);
					}
			}
			
			if (mPrevQuery == null){
				//only restore previous query if we don't have one already 
				mPrevQuery = args.getBundle(KEY_PREV_QUERY);
				if (mPrevQuery == null){ //still null? try the args.
					mPrevQuery = getArguments();
				}
			}
			setSearchTerms();
			
		} catch (Exception e){
			if (DEBUG){
				Log.e(LOGTAG, "This should not be happening: " + e);
				e.printStackTrace();
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	

	/** Send the model the request. Sets the internal previous request. 
	 * No view setting.
	 * @param args The args to send according to the KEYS given.  */
	private void sendModelRequest(Bundle args) {
		if (mPrevQuery == null){ //should never be null, but just in case
			mPrevQuery = new Bundle();
		}
		mPrevQuery.putAll(args);
		mPrevQuery.putBoolean(
				CareersStackOverflowModel.KEY_USE_METRIC, 
				UnitCheck.useMetric(mPrefs, getResources()) );
		 
		mAsyncModel.sendMessage(CareersStackOverflowModel.REQUEST_RECORD_QUERY, mPrevQuery);
	}
	
	/** Builds the saved state from views and other elements. */
	private void buildSaveState(Bundle outState) {
		outState.putParcelableArrayList(KEY_CAREER_LIST, mCareerList);
		
		outState.putBoolean(KEY_IS_LOADING_RESULTS, mIsLoadingResults);
		outState.putBoolean(KEY_RESULTS_TIMEOUT, mResultsTimeout);
		
		outState.putBundle(KEY_PREV_QUERY, mPrevQuery);
		
		debugSavedState(outState);
	}
	
	/** Debugs the saved state (provided {@link #DEBUG} is true. */
	private void debugSavedState(Bundle outState){
		if (DEBUG){
			for(String key : outState.keySet()){
				Log.d(LOGTAG,
						String.format("Bundle pair: [%s] -> [%s]", key,
								outState.get(key))
						);
			}
			Log.d(LOGTAG, "careerList size: " + mCareerList.size());
		}
	}
	
	/** Sets the search terms for the adapter. */
	private void setSearchTerms(){
		if (mPrevQuery == null){
			mPrevQuery = new Bundle();
		}
		String keyword = mPrevQuery.getString(KEY_KEYWORD_TEXT); 
		if (keyword == null){
			keyword = mPrevQuery.getString(KEY_TAG_TEXT);
		}
		
		String location = mPrevQuery.getString(KEY_LOCATION_TEXT);
		if (location == null){
			location = "";
		}
		mResultAdapter.setSearchTerms(
				keyword, 
				location, 
				mPrevQuery.getBoolean(KEY_REMOTE_ALLOWED, false), 
				mPrevQuery.getBoolean(KEY_RELOCATE_OFFER, false));		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// View updates start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Created a new fade out listener. Sets the view to GONE when animation ends. */
	static private Animation.AnimationListener newFadeOutViewListener(final View view){
		return new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				//view.setVisibility(View.VISIBLE); //assume visible
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				view.setVisibility(View.GONE);				
			}
		};
	}
	
	/** Fades views according to visibility.
	 * If hideView1 is visible, it sets the animation to fade it and 
	 * @param showView The view to fade into visibility.
	 * @param hideView1 The view to fade/hide
	 * @param hideView2 The view to fade/hide
	 */
	private void fadeViews(View showView, View hideView1, View hideView2){
		final Animation fadein = 
				AnimationUtils.loadAnimation(getActivity(), R.anim.quick_fade_in);
		final Animation fadeout = 
				AnimationUtils.loadAnimation(getActivity(), R.anim.quick_fade_out);
		showView.setAnimation(fadein);
		//make visible AFTER setting animation, so it starts dim.
		showView.setVisibility(View.VISIBLE);  
		
		if (hideView1.getVisibility() == View.VISIBLE){
			fadeout.setAnimationListener(newFadeOutViewListener(hideView1));
			hideView1.setAnimation(fadeout);
						
			hideView2.setVisibility(View.GONE);			
		} else if (hideView2.getVisibility() == View.VISIBLE){
			fadeout.setAnimationListener(newFadeOutViewListener(hideView2));
			hideView2.setAnimation(fadeout);
			
			hideView1.setVisibility(View.GONE);
		}
		//otherwise: do nothing, they're both invisible
	}
	
	
	/** Shows the retry block, hiding the other 2 views. Sets
	 * {@link #mIsLoadingResults} to false and 
	 * {@link #mResultsTimeout} to <code>true</code>	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility. */
	private void showRetryBlock(boolean animate){
		mIsLoadingResults = false;
		mResultsTimeout = true;
		
		if (	mRetryContainer != null && mProgressContainer != null && 
				mResultsList != null ){ //ensure views valid
			if (animate){
				fadeViews(mRetryContainer, mProgressContainer, mResultsList);
			} else {
				mRetryContainer.setVisibility(View.VISIBLE);
				mProgressContainer.setVisibility(View.GONE);
				mResultsList.setVisibility(View.GONE);
			}
		}
		
		if (DEBUG){
			Log.v(LOGTAG, "showRetryBlock: " + animate);
		}
	}
	
	/** Shows the loading/progress block, hiding the other 2 views. Sets
	 * {@link #mIsLoadingResults} to true and 
	 * {@link #mResultsTimeout} to <code>false</code>.	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility.  */
	private void showLoadingBlock(boolean animate){
		mIsLoadingResults = true;
		mResultsTimeout = false;	
		
		if (	mRetryContainer != null && mProgressContainer != null && 
				mResultsList != null && mTv_progress != null){ //ensure views valid
			//always clear progress
			mTv_progress.setText("");
			
			if (animate){
				fadeViews(mProgressContainer, mRetryContainer, mResultsList);
			} else {
				mRetryContainer.setVisibility(View.GONE);
				mProgressContainer.setVisibility(View.VISIBLE);
				mResultsList.setVisibility(View.GONE);
			}
		}
		
		if (DEBUG){
			Log.v(LOGTAG, "showLoadingBlock: " + animate);
		}
	}
	
	/** Shows the list view, hiding the other 2 views. Sets
	 * {@link #mIsLoadingResults} to false and 
	 * {@link #mResultsTimeout} to <code>false</code>	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility.  */
	private void showResults(boolean animate){
		mIsLoadingResults = false;
		mResultsTimeout = false;	
		
		if (	mRetryContainer != null && mProgressContainer != null && 
				mResultsList != null ){ //ensure views valid
			if (animate){
				fadeViews(mResultsList, mProgressContainer, mRetryContainer);
			} else {
				mRetryContainer.setVisibility(View.GONE);
				mProgressContainer.setVisibility(View.GONE);
				mResultsList.setVisibility(View.VISIBLE);
			}
		}
		
		if (DEBUG){
			Log.v(LOGTAG, "showResults: " + animate);
		}
	}
	
	/** Resets the list position to top. */
	private void resetListPosition(){
		if (mResultsList != null){ //ensure view valid
			//ensure we are at the top. 
			mResultsList.scrollToPosition(0);
			mResultsList.postDelayed(new Runnable() {@Override
				public void run() {
					try { //catch as we are delaying
						mResultsList.scrollToPosition(0);
					} catch(Exception e) {}
				}
			}, 50);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
		
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		
		case R.id.careerstack_searchResults_button_retry:
			retryRequest();
			break;
			
		case R.id.careerstack_searchResults_button_cancel:
			if (mCareerList.isEmpty()){
				showRetryBlock(false);
			} else {
				showResults(true);
			}
			//set cancelled appearance first, before cancelling
			mAsyncModel.sendMessage(CareersStackOverflowModel.REQUEST_QUERY_CANCEL);
			//fall through
		}
	}

	
	@Override
	public void onCareerItemClick(CareerItem item) {
		mFragInteractionListener.onCareerItemRequest(item);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Message msg) {
		synchronized (this) {
			try {
				if (DEBUG){
					Log.d(LOGTAG, "Message: " +msg.what);
				}
				switch (msg.what) {
				case CareersStackOverflowModel.NOTIFY_STARTING_QUERY:
					//showLoadingBlock(true); //taken care of in sendRequest(Bundle)
					return true;
					
				case CareersStackOverflowModel.NOTIFY_CANCELLED_QUERY:
					//showResults(true); //taken care of in onClick(View)
					return true;
					
				case CareersStackOverflowModel.NOTIFY_PROGRESS_UPDATE:
					if (msg.arg1 > -1 && msg.arg2 > 0 && mTv_progress != null){
						//updates to be x / y; e.g. "5 / 133"
						mTv_progress.setText(
								new StringBuilder().append(msg.arg1)
													.append(" / ")
													.append(msg.arg2)
													.toString() );
					}
					return true;
					
				case CareersStackOverflowModel.REPLY_RECORDS_RESULT:
					
					if (msg.obj instanceof List<?>){
						try {
							mCareerList = (ArrayList<CareerItem>) msg.obj;
							//cast check
							if (mCareerList.size() > 0){
								@SuppressWarnings("unused")
								CareerItem item = mCareerList.get(0);
							}
							
							mResultAdapter.setCareerItems(mCareerList);
							setSearchTerms();
							
						} catch (ClassCastException e){
							Log.e(LOGTAG, "Mismatched class? How irregular: " + e );
						}
					}
					
					if (mIsLoadingResults){ //if we have not cancelled
						showResults(true);
						resetListPosition();
					}
					mIsLoadingResults = false; //ensure we are complete
					
					return true;
					
				case CareersStackOverflowModel.ERROR_REQUEST_TIMEOUT:
					if (mIsLoadingResults){ //if we were loading results, and timeout
						showRetryBlock(true);
					}
					return true;
					
				case CareersStackOverflowModel.ERROR_REQUEST_FAILED:
					if (mCareerList.isEmpty() || mIsLoadingResults){
						//if we have no elements, or were still loading results 
						//i.e. not cancelled, retry block
						showRetryBlock(true);
					} else {
						showResults(true);
					}
					return true;
					
				}
			} catch (Exception e){
				if (DEBUG){
					Log.d(LOGTAG, "Seems we have a problem here: " + e );
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The interaction listener that the activity must implement to handle the 
	 * {@link SearchResultsFragment}'s requests. 
	 * @author Jason J.
	 * @version 0.1.0-20141003
	 */
	static public interface OnFragmentInteractionListener {		
		/** Sends activity a search request to be handled.
		 * @param bundle The bundle of search arguments given by the 
		 * {@link MainFragment} keys.
		 * @return <code>true</code> if the activity has honoured the request,
		 * <code>false</code> if has been ignored.		 */
		public boolean onCareerItemRequest(CareerItem item);
		
		/** Requests the the activity return the fragments saved state. 
		 * @return The saved state as built  by frag or <code>null</code>	 */
		public Bundle onPopSavedStateRequest();
		
		/** Requests the activity hold its save state while it sits in the 
		 * backstack (lazy solution)
		 * @param savedState The fragment's saved state
		 * @return <code>true</code> if the activity has honoured the request,
		 * <code>false</code> if has been ignored.		 */
		public boolean onHoldSavedStateRequest(Bundle savedState);
	}
	
}
