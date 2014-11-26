package com.ovrhere.android.careerstack.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.CareersStackOverflowModel;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.adapters.CareerItemFilterListAdapter;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/**
 * The fragment to perform searches and display cursory results.
 * Expects Activity to implement {@link OnFragmentInteractionListener} and 
 * will throw {@link ClassCastException} otherwise.
 * @author Jason J.
 * @version 0.8.1-20141126
 */
public class SearchResultsFragment extends Fragment 
implements OnClickListener, OnItemClickListener, Handler.Callback {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = SearchResultsFragment.class
			.getSimpleName();	
	/**Logtag for debugging purposes. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	final static private boolean DEBUG = false;
	
		
	/** Bundle key. This stores {@link #careerList}.
	 *  List<Parcelable>/List<CareerItem> */
	final static private String KEY_CAREER_LIST = 
			CLASS_NAME +".KEY_CAREER_LIST";
	/** Bundle key. Whether or not the fragment is currently loading results
	 * {@link #isLoadingResults}. Boolean */
	final static private String KEY_IS_LOADING_RESULTS = 
			CLASS_NAME +".KEY_IS_LOADING_RESULTS";
	/** Bundle key. Whether or not the fragment has failed with last results
	 * {@link #resultsTimeout}. Boolean */
	final static private String KEY_RESULTS_TIMEOUT = 
			CLASS_NAME +".KEY_RESULTS_TIMEOUT";
	
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
	final static public String KEY_TAG_TEXT = 
			CareersStackOverflowModel.KEY_TAG_TEXT;
	/** Bundle key. The value of keyword. String */
	final static public String KEY_KEYWORD_TEXT = 
			CareersStackOverflowModel.KEY_KEYWORD_TEXT;
	/** Bundle key. The value of location. String. */
	final static public String KEY_LOCATION_TEXT = 
			CareersStackOverflowModel.KEY_LOCATION_TEXT;
	/** Bundle key. The whether the remote check is set. Boolean. */
	final static public String KEY_REMOTE_ALLOWED = 
			CareersStackOverflowModel.KEY_REMOTE_ALLOWED;
	/** Bundle key. The whether the relocation check is set. Boolean. */
	final static public String KEY_RELOCATE_OFFER = 
			CareersStackOverflowModel.KEY_RELOCATE_OFFER;
	/** Bundle Key. The current distance in the seek bar. Int. */
	final static public String KEY_DISTANCE = 
			CareersStackOverflowModel.KEY_DISTANCE;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	/** The container for retry. */
	private View retryContainer = null;
	/** The container for canceling + progress bar. */
	private View progressContainer = null;
	/** The list of results. */
	private ListView lv_resultsView = null;
	
	/** The loading progress, set to blank by default. */
	private TextView tv_progress = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The list of elements. Should never be <code>null</code>. */
	private ArrayList<CareerItem> careerList = new ArrayList<CareerItem>();	
	/** The adapter for the results. */
	private CareerItemFilterListAdapter resultAdapter = null;
	
	
	/** If the fragment is currently loading results. Default false. */
	private boolean isLoadingResults = false;
	
	/** If the fragment failed during last load.  Default false. */
	private boolean resultsTimeout = false;
		
	/** The fragment request listener from activity. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	/** Used in {@link #onSaveInstanceState(Bundle)} to determine if 
	 * views are visible. */
	@SuppressWarnings("unused")
	private boolean viewBuilt = false;
	
	/** The async model for making requests. */
	private CareersStackOverflowModel asyncModel = null;
	/** The args for previous query performed. Starts off as args. */ 
	private Bundle prevQuery = null;
	
	/** The shared preferences to used. */
	private SharedPreferences prefs = null;
	
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
		prefs = PreferenceUtils.getPreferences(getActivity());
		
		asyncModel = new CareersStackOverflowModel(getActivity());
		asyncModel.addMessageHandler(new Handler(this));		
	}
	
	@Override
	public void onDestroy() {
		synchronized (asyncModel) {
			super.onDestroy();
			if (isLoadingResults){
				asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_PAUSE_QUERY);
			}
			asyncModel.dispose();		
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
		
		//default loading to true to start with loading/spinner
		showLoadingBlock(false);
		
		if (backStackState != null){
			debugSavedState(backStackState);
			processArgBundle(backStackState);
		} else if (savedInstanceState != null){
			processArgBundle(savedInstanceState);			
		} else if (getArguments() != null) {
			Bundle args = getArguments();
			processArgBundle(args);
			
			//if no values are set and we aren't loading, request.
			if (!isLoadingResults){ 
				sendModelRequest(args);
			}
			showLoadingBlock(false);
		} 
		
		viewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (isLoadingResults){
			asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_RESUME_QUERY);
		}
		//prevQuery still empty?
		if (prevQuery == null){
			prevQuery = getArguments(); //still set args
		}
	}
	
	
	@Override
	public void onDestroyView() {
		synchronized (asyncModel) {
			Bundle state = new Bundle();
			buildSaveState(state); //holds the state
			mFragInteractionListener.onHoldSavedStateRequest(state);
			
			super.onDestroyView();
			viewBuilt = false;
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
		
		if (prevQuery == null){
			prevQuery = new Bundle();
		}
		prevQuery.clear(); //clean the old request //TODO reallow
		
		sendModelRequest(args);		
	}

	
	/** Resends the previous request. If no request was previously sent, an
	 * empty request is sent.	 */
	public void retryRequest() {
		if (prevQuery == null){ //should never be null, but just in case
			prevQuery = new Bundle();
		}
		//we are about to request, set up loading
		showLoadingBlock(true);
		sendModelRequest(prevQuery);
	};
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the output views. */
	private void initOutputs(View rootView){
		resultAdapter = new CareerItemFilterListAdapter(getActivity());
						
		lv_resultsView = (ListView)
				rootView.findViewById(R.id.careerstack_searchResults_list_searchResults);
		lv_resultsView.setAdapter(resultAdapter);
		lv_resultsView.setOnItemClickListener(this);
		
		retryContainer =
				rootView.findViewById(R.id.careerstack_searchResults_layout_retry);
		progressContainer =
				rootView.findViewById(R.id.careerstack_searchResults_layout_progressBar);
		
		tv_progress = (TextView)
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
			isLoadingResults = args.getBoolean(KEY_IS_LOADING_RESULTS, false);
			resultsTimeout = args.getBoolean(KEY_RESULTS_TIMEOUT, false);
			
			if (resultsTimeout){
				showRetryBlock(false);
			} else if (isLoadingResults){
				showLoadingBlock(false);
			} else {
				showResults(false);
			}
			
			if (args.getParcelableArrayList(KEY_CAREER_LIST) != null){
				try {
						careerList = args.getParcelableArrayList(KEY_CAREER_LIST);
						resultAdapter.setCareerItems(careerList);
					} catch (ClassCastException e){
						Log.e(LOGTAG, "Career list mistmatched class?" + e);
					}
			}
			if (prevQuery == null){
				prevQuery = getArguments();
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
		if (prevQuery == null){ //should never be null, but just in case
			prevQuery = new Bundle();
		}
		prevQuery.putAll(args);
		prevQuery.putBoolean(
				CareersStackOverflowModel.KEY_USE_METRIC, 
				UnitCheck.useMetric(prefs, getResources()) );
		 
		asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_RECORD_QUERY, prevQuery);
	}
	
	/** Builds the saved state from views and other elements. */
	private void buildSaveState(Bundle outState) {
		outState.putParcelableArrayList(KEY_CAREER_LIST, careerList);
		
		outState.putBoolean(KEY_IS_LOADING_RESULTS, isLoadingResults);
		outState.putBoolean(KEY_RESULTS_TIMEOUT, resultsTimeout);
		
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
			Log.d(LOGTAG, "careerList size: " + careerList.size());
		}
	}
	
	/** Sets the search terms for the adapter. */
	private void setSearchTerms(){
		if (prevQuery == null){
			prevQuery = new Bundle();
		}
		String keyword = prevQuery.getString(KEY_KEYWORD_TEXT); 
		if (keyword == null){
			keyword = prevQuery.getString(KEY_TAG_TEXT);
		}
		
		String location = prevQuery.getString(KEY_LOCATION_TEXT);
		if (location == null){
			location = "";
		}
		resultAdapter.setSearchTerms(
				keyword, 
				location, 
				prevQuery.getBoolean(KEY_REMOTE_ALLOWED, false), 
				prevQuery.getBoolean(KEY_RELOCATE_OFFER, false));		
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
	 * {@link #isLoadingResults} to false and 
	 * {@link #resultsTimeout} to <code>true</code>	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility. */
	private void showRetryBlock(boolean animate){
		isLoadingResults = false;
		resultsTimeout = true;
		
		if (animate){
			fadeViews(retryContainer, progressContainer, lv_resultsView);
		} else {
			retryContainer.setVisibility(View.VISIBLE);
			progressContainer.setVisibility(View.GONE);
			lv_resultsView.setVisibility(View.GONE);
		}
		
		if (DEBUG){
			Log.v(LOGTAG, "showRetryBlock: " + animate);
		}
	}
	
	/** Shows the loading/progress block, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to true and 
	 * {@link #resultsTimeout} to <code>false</code>.	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility.  */
	private void showLoadingBlock(boolean animate){
		isLoadingResults = true;
		resultsTimeout = false;	
		
		//always clear progress
		if (tv_progress != null){
			tv_progress.setText("");
		}
		
		if (animate){
			fadeViews(progressContainer, retryContainer, lv_resultsView);
		} else {
			retryContainer.setVisibility(View.GONE);
			progressContainer.setVisibility(View.VISIBLE);
			lv_resultsView.setVisibility(View.GONE);
		}	
		
		if (DEBUG){
			Log.v(LOGTAG, "showLoadingBlock: " + animate);
		}
	}
	
	/** Shows the list view, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to false and 
	 * {@link #resultsTimeout} to <code>false</code>	
	 * @param animate <code>true</code> to fade views, <code>false</code> 
	 * to just switch visibility.  */
	private void showResults(boolean animate){
		isLoadingResults = false;
		resultsTimeout = false;	
		
		if (animate){
			fadeViews(lv_resultsView, progressContainer, retryContainer);
		} else {
			retryContainer.setVisibility(View.GONE);
			progressContainer.setVisibility(View.GONE);
			lv_resultsView.setVisibility(View.VISIBLE);
		}
		
		if (DEBUG){
			Log.v(LOGTAG, "showResults: " + animate);
		}
	}
	
	/** Resets the list position to top. */
	private void resetListPosition(){
		//ensure we are at the top. 
		lv_resultsView.setSelectionFromTop(0, 0);
		lv_resultsView.postDelayed(new Runnable() {@Override
			public void run() {
				try{ //catch as we are delaying
					lv_resultsView.setSelectionFromTop(0, 0);
				}catch(Exception e){}
			}
		}, 50);
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
			if (careerList.isEmpty()){
				showRetryBlock(false);
			} else {
				showResults(true);
			}
			//set cancelled appearance first, before cancelling
			asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_QUERY_CANCEL);
			//fall through
		}
	}

	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try{
			CareerItem item = (CareerItem) resultAdapter.getItem(position);
			if (item != null){
				mFragInteractionListener.onCareerItemRequest(item);
			}
		} catch (ClassCastException e){}
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
					if (msg.arg1 > -1 && msg.arg2 > 0 && tv_progress != null){
						//updates to be x / y; e.g. "5 / 133"
						tv_progress.setText(
								new StringBuilder().append(msg.arg1)
													.append(" / ")
													.append(msg.arg2)
													.toString() );
					}
					return true;
					
				case CareersStackOverflowModel.REPLY_RECORDS_RESULT:
					
					if (msg.obj instanceof List<?>){
						try {
							careerList = (ArrayList<CareerItem>) msg.obj;
							//cast check
							if (careerList.size() > 0){
								@SuppressWarnings("unused")
								CareerItem item = careerList.get(0);
							}
							
							resultAdapter.setCareerItems(careerList);
							setSearchTerms();
							
						} catch (ClassCastException e){
							Log.e(LOGTAG, "Mismatched class? How irregular: " + e );
						}
					}
					
					if (isLoadingResults){ //if we have not cancelled
						showResults(true);
						resetListPosition();
					}
					isLoadingResults = false; //ensure we are complete
					
					return true;
					
				case CareersStackOverflowModel.ERROR_REQUEST_TIMEOUT:
					if (isLoadingResults){ //if we were loading results, and timeout
						showRetryBlock(true);
					}
					return true;
					
				case CareersStackOverflowModel.ERROR_REQUEST_FAILED:
					if (careerList.isEmpty() || isLoadingResults){
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
