package com.ovrhere.android.careerstack.ui.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.CareersStackOverflowModel;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.adapters.CareerItemFilterListAdapter;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.DistanceDialogFragment;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/**
 * The fragment to perform searches and display cursory results.
 * Expects Activity to implement {@link OnFragmentInteractionListener} and 
 * will throw {@link ClassCastException} otherwise.
 * @author Jason J.
 * @version 0.3.0-20141003
 */
public class SearchResultsFragment extends Fragment 
implements OnClickListener, OnCheckedChangeListener, OnItemClickListener,
	Handler.Callback {	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = SearchResultsFragment.class
			.getSimpleName();	
	/**Logtag for debugging purposes. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	final static private boolean DEBUG = false;
	
	/** The request code for the distance fragment. Used with 
	 * {@link #onActivityResult(int, int, Intent)} and 
	 * {@link DistanceDialogFragment}.	 */
	final static private int REQUEST_CODE_DISTANCE_FRAG = 0x101;
	
	/** Tag. The to use in
	 *  {@link OnFragmentRequestListener#onRequestHoldSavedState(String, Bundle)}. */
	@Deprecated
	final static private String TAG_BACKSTACK_STATE = 
			CLASS_NAME+".TAG_BACKSTACK_STATE";
	
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
	
	/** The keyword input. */
	private EditText et_keywords = null;
	/** The location input. */
	private EditText et_location = null;
	/** The remote allowed checkbox. */
	private CompoundButton cb_remoteAllowed = null;
	/** The relocation checkbox. */
	private CompoundButton cb_relocationOffered =  null;
	/** The button used for distance. */
	private Button btn_distance = null;
	/** The button used for searching. */
	private Button btn_search = null;
	
	/** The container for retry. */
	private View retryContainer = null;
	/** The container for canceling + progress bar. */
	private View progressContainer = null;
	/** The list of results. */
	private ListView lv_resultsView = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The list of elements. Should never be <code>null</code>. */
	private ArrayList<CareerItem> careerList = new ArrayList<CareerItem>();	
	/** The adapter for the results. */
	private CareerItemFilterListAdapter resultAdapter = null;
	
	/** The current distance. Used for saved states. */
	private int currentDistanceValue = 0;
	
	/** If the fragment is currently loading results. */
	private boolean isLoadingResults = false;
	/** If the fragment failed during last load. */
	private boolean resultsTimeout = false;
		
	/** The fragment request listener from activity. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	/** Used in {@link #onSaveInstanceState(Bundle)} to determine if 
	 * views are visible. */
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
		super.onDestroy();
		if (isLoadingResults){
			asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_PAUSE_QUERY);
		}
		asyncModel.dispose();
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_search_results, container,
				false);
		currentDistanceValue = 
				getResources().getInteger(R.integer.careerstack_seekBar_default);
		
		initInputs(rootView);
		initOutputs(rootView);
		Bundle backStackState = 
				mFragInteractionListener.onPopSavedStateRequest();
		
		//ensure the view is built first, as requests are still heavy
		
		if (backStackState != null){
			debugSavedState(backStackState);
			processArgBundle(backStackState);
		} else if (savedInstanceState != null){
			processArgBundle(savedInstanceState);			
		} else if (getArguments() != null) {
			Bundle args = getArguments();
			processArgBundle(args);
			//if no values set, request.
			sendRequest(args);
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
		Bundle state = new Bundle();
		buildSaveState(state); //holds the state
		mFragInteractionListener.onHoldSavedStateRequest(state);
		
		super.onDestroyView();
		viewBuilt = false;
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE_DISTANCE_FRAG:
			if (Activity.RESULT_OK == resultCode){
				int value = 
						data.getIntExtra(DistanceDialogFragment.KEY_DISTANCE, 
								currentDistanceValue);
				updateDistanceButton(value);
			}
			break;

		default:
			//fall through
		}
	}
	
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
	}
	
	/** Initializes all the native input views on the fragment. */
	private void initInputs(View rootView){
		initTextInput(rootView); 		
		initCheckBoxes(rootView);		
		initButtons(rootView);
		
	}
	/** Initializes text input. */
	private void initTextInput(View rootView) {
		et_keywords = (EditText) 
				rootView.findViewById(R.id.careerstack_searchResults_editin_keywords);
		et_location = (EditText)
				rootView.findViewById(R.id.careerstack_searchResults_editin_location);
		et_location.addTextChangedListener(locationTextWatcher);
	}
	/** Initializes checkboxes. */
	private void initCheckBoxes(View rootView) {
		cb_relocationOffered = (CompoundButton)
				rootView.findViewById(R.id.careerstack_searchResults_check_offerRelocation);
		cb_remoteAllowed = (CompoundButton)
				rootView.findViewById(R.id.careerstack_searchResults_check_allowRemote);		
		cb_relocationOffered.setOnCheckedChangeListener(this); 
		cb_remoteAllowed.setOnCheckedChangeListener(this);
	}
	/** Intializes buttons, finding and setting listeners. */
	private void initButtons(View rootView) {
		btn_distance = (Button) 
				rootView.findViewById(R.id.careerstack_searchResults_button_distance);
		btn_distance.setOnClickListener(this);
		btn_search = (Button) 
				rootView.findViewById(R.id.careerstack_searchResults_button_search);
		btn_search.setOnClickListener(this);
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
		String keywords = args.getString(KEY_KEYWORD_TEXT);
		if (keywords != null){
			et_keywords.setText(keywords);
		}
		String location = args.getString(KEY_LOCATION_TEXT);
		if (location != null){
			et_location.setText(location);
		}
		cb_relocationOffered.setChecked(
				args.getBoolean(KEY_RELOCATE_OFFER, false));
		cb_remoteAllowed.setChecked( 
				args.getBoolean(KEY_REMOTE_ALLOWED, false));
		
		currentDistanceValue = 
				args.getInt(KEY_DISTANCE, currentDistanceValue);
		updateDistanceButton(currentDistanceValue);
		
		isLoadingResults = args.getBoolean(KEY_IS_LOADING_RESULTS);
		resultsTimeout = args.getBoolean(KEY_RESULTS_TIMEOUT);
		
		if (resultsTimeout){
			showRetryBlock();
		} else if (isLoadingResults){
			showLoadingBlock();
		} else {
			showResults();
		}
		
		if (args.getParcelableArrayList(KEY_CAREER_LIST) != null){
			try {
					careerList = args.getParcelableArrayList(KEY_CAREER_LIST);
					resultAdapter.setCareerItems(careerList);
				} catch (ClassCastException e){
					Log.e(LOGTAG, "Career list mistmatched class?" + e);
				}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Builds the saved state from views (if built) and other elements. */
	private void buildSaveState(Bundle outState) {
		if (viewBuilt){
			//Edit text views & checkviews take care of themselves.
			outState.putString(KEY_KEYWORD_TEXT, et_keywords.getText().toString());
			outState.putString(KEY_LOCATION_TEXT, et_location.getText().toString());
			
			outState.putBoolean(KEY_RELOCATE_OFFER, cb_relocationOffered.isChecked());
			outState.putBoolean(KEY_REMOTE_ALLOWED, cb_remoteAllowed.isChecked());		
			
			outState.putInt(KEY_DISTANCE, currentDistanceValue);
			outState.putParcelableArrayList(KEY_CAREER_LIST, careerList);
			
			outState.putBoolean(KEY_IS_LOADING_RESULTS, isLoadingResults);
			outState.putBoolean(KEY_RESULTS_TIMEOUT, resultsTimeout);
		}
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// View updates start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Same as calling {@link #enableInput(boolean)} with
	 * <code>isLoadingResults == false</code> value.	 */
	private void checkInput(){
		enableInput(isLoadingResults == false);
	}
	
	/** Enables or disables input based upon the input given.
	 * @param enabled <code>true</code> to enable buttons and edit views,
	 * <code>false</code> to disable.	 */
	private void enableInput(boolean enabled){
		et_keywords.setEnabled(enabled);
		et_location.setEnabled(enabled);
		
		cb_relocationOffered.setEnabled(enabled);
		cb_remoteAllowed.setEnabled(enabled);
		
		btn_search.setEnabled(enabled);
		
		//more convoluted logic for the distance button
		if (enabled){
			//Forcibly check with text watcher.
			locationTextWatcher.afterTextChanged(
						et_location.getEditableText()
					);
		} else {
			btn_distance.setEnabled(false);
		}
		
	}
	
	/** Shows the retry block, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to false and 
	 * {@link #resultsTimeout} to <code>true</code>	 */
	private void showRetryBlock(){
		retryContainer.setVisibility(View.VISIBLE);
		progressContainer.setVisibility(View.GONE);
		lv_resultsView.setVisibility(View.GONE);
		isLoadingResults = false;
		resultsTimeout = true;
		checkInput();
	}
	
	/** Shows the loading/progress block, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to true and 
	 * {@link #resultsTimeout} to <code>false</code>.	 */
	private void showLoadingBlock(){
		retryContainer.setVisibility(View.GONE);
		progressContainer.setVisibility(View.VISIBLE);
		lv_resultsView.setVisibility(View.GONE);
		isLoadingResults = true;
		resultsTimeout = false;
		checkInput();
	}
	
	/** Shows the list view, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to false and 
	 * {@link #resultsTimeout} to <code>false</code>	 */
	private void showResults(){
		retryContainer.setVisibility(View.GONE);
		progressContainer.setVisibility(View.GONE);
		lv_resultsView.setVisibility(View.VISIBLE);
		isLoadingResults = false;
		resultsTimeout = false;
		checkInput();
	}
	
	/** Updates the distance button's text & value of currentDistanceValue. */
	private void updateDistanceButton(int value){
		btn_distance.setText(
				UnitCheck.unitsShort(prefs, getResources(), value)
				);
		btn_distance.setContentDescription(
				UnitCheck.units(prefs, getResources(), value)
				);
		currentDistanceValue = value;
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt(
				getString(R.string.careerstack_pref_KEY_DISTANCE_VALUE), 
				value).commit();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Request updates start here
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Prepares and sends request off to model.
	 * Sets {@link #prevQuery}.
	 * @param args The args to modify for sending.  */
	private void sendRequest(Bundle args){
		if (prevQuery == null){ //should never be, but in case
			prevQuery = new Bundle();
		}
		prevQuery.putAll(args);
		prevQuery.putBoolean(
				CareersStackOverflowModel.KEY_USE_METRIC, 
				UnitCheck.useMetric(prefs, getResources()) );
		prevQuery.remove(KEY_IS_LOADING_RESULTS);
		prevQuery.remove(KEY_CAREER_LIST);
		asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_RECORD_QUERY, prevQuery);
	}
	
	/** Prepares and sends request. */
	private void prepareAndSendRequest(){
		Bundle args = new Bundle();
		buildSaveState(args);
		sendRequest(args);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//TODO move search functionality into dialog
	
	/** The text watcher for the location. */
	private TextWatcher locationTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			//nothing to do
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//nothing to do
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			String content = s.toString().trim();
			//if we have content, show, otherwise hide.
			btn_distance.setEnabled(content.isEmpty() == false);
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.careerstack_searchResults_button_search:
			prepareAndSendRequest();
			break;
			
		case R.id.careerstack_searchResults_button_distance:
			DistanceDialogFragment dialog = 
				DistanceDialogFragment.newInstance(this,
						REQUEST_CODE_DISTANCE_FRAG, 
						currentDistanceValue);
			dialog.show(getFragmentManager(), 
					DistanceDialogFragment.class.getName());			
			break;
			
		case R.id.careerstack_searchResults_button_retry:
			sendRequest(prevQuery);
			break;
			
		case R.id.careerstack_searchResults_button_cancel:
			asyncModel.sendMessage(CareersStackOverflowModel.REQUEST_QUERY_CANCEL);
			if (careerList.isEmpty()){
				showRetryBlock();
			} else {
				showResults();
			}
			//fall through
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences.Editor edit = prefs.edit();
		switch (buttonView.getId()) {
		case R.id.careerstack_searchResults_check_allowRemote:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_REMOTE_ALLOWED), 
					isChecked).commit();
			break;
		case R.id.careerstack_searchResults_check_offerRelocation:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_RELOCATION_OFFERED), 
					isChecked).commit();
			break;
		}		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CareerItem item = resultAdapter.getItem(position);
		if (item != null){
			mFragInteractionListener.onCareerItemRequest(item);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean handleMessage(Message msg) {
		try {
			if (DEBUG){
				Log.d(LOGTAG, "Message: " +msg.what);
			}
			switch (msg.what) {
			case CareersStackOverflowModel.NOTIFY_STARTING_QUERY:
				showLoadingBlock();
				return true;
				
			case CareersStackOverflowModel.NOTIFY_CANCELLED_QUERY:
				showResults();
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
					} catch (ClassCastException e){
						Log.e(LOGTAG, "Mismatched class? How irregular: " + e );
					}
				}
				showResults();
				return true;
				
			case CareersStackOverflowModel.ERROR_REQUEST_TIMEOUT:
				showRetryBlock();
				return true;
			case CareersStackOverflowModel.ERROR_REQUEST_FAILED:
				if (careerList.isEmpty()){
					showRetryBlock();
				} else {
					showResults();
				}
				return true;
			}
		} catch (Exception e){
			if (DEBUG){
				Log.d(LOGTAG, "Seems we have a problem here: " + e );
				e.printStackTrace();
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
