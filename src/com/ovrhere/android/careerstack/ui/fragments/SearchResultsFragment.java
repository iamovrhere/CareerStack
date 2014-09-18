package com.ovrhere.android.careerstack.ui.fragments;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
import com.ovrhere.android.careerstack.ui.adapters.CareerItemFilterListAdapter;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.DistanceDialogFragment;
import com.ovrhere.android.careerstack.ui.listeners.OnFragmentRequestListener;

/**
 * The fragment to perform searches and display cursory results.
 * @author Jason J.
 * @version 0.1.0-20140918
 */
public class SearchResultsFragment extends Fragment 
implements OnClickListener, OnCheckedChangeListener, OnItemClickListener {	
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start public keys
	////////////////////////////////////////////////////////////////////////////////////////////////
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
	/** Bundle Key. The current distance in the seek bar. Int. */
	final static public String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	
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
		
	/** The fragment request listener from main. */
	private OnFragmentRequestListener mFragmentRequestListener = null;
	/** Used in {@link #onSaveInstanceState(Bundle)} to determine if 
	 * views are visible. */
	private boolean viewBuilt = false;
	
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
				mFragmentRequestListener.onRequestPopSavedState(TAG_BACKSTACK_STATE);
		if (savedInstanceState != null){
			processArgBundle(savedInstanceState);
		} else if (backStackState != null){
			debugSavedState(backStackState);
			processArgBundle(backStackState);
		} else if (getArguments() != null) {
			processArgBundle(getArguments());
			//if no values set, request.
			populateDummyValues();
		} 
		viewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewBuilt = false;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mFragmentRequestListener = 
					(OnFragmentRequestListener) activity;
		} catch (ClassCastException e){
			Log.e(LOGTAG, "Activity must implement :" +
					OnFragmentRequestListener.class.getSimpleName());
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
		Button search = (Button) 
				rootView.findViewById(R.id.careerstack_searchResults_button_search);
		search.setOnClickListener(this);
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
		}
		debugSavedState(outState);
	}
	
	/** Shows the retry block, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to false.	 */
	private void showRetryBlock(){
		retryContainer.setVisibility(View.VISIBLE);
		progressContainer.setVisibility(View.GONE);
		lv_resultsView.setVisibility(View.GONE);
		isLoadingResults = false;
	}
	
	/** Shows the loading/progress block, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to true.	 */
	private void showLoadingBlock(){
		retryContainer.setVisibility(View.GONE);
		progressContainer.setVisibility(View.VISIBLE);
		lv_resultsView.setVisibility(View.GONE);
		isLoadingResults = true;
	}
	
	/** Shows the list view, hiding the other 2 views. Sets
	 * {@link #isLoadingResults} to false.	 */
	private void showResults(){
		retryContainer.setVisibility(View.GONE);
		progressContainer.setVisibility(View.GONE);
		lv_resultsView.setVisibility(View.VISIBLE);
		isLoadingResults = false;
	}
	
	/** Updates the distance button's text & value of currentDistanceValue. */
	private void updateDistanceButton(int value){
		// TODO units check here
		btn_distance.setText(
				String.format(
						getString(R.string.careerstack_formatString_distanceValue_milesShort),
						value
						)
				);
		btn_distance.setContentDescription(
				String.format(
						getString(R.string.careerstack_formatString_distanceValue_miles),
						value
						));
		currentDistanceValue = value;
	}
	/** Populates the list with dummy values for testing. */
	private void populateDummyValues(){
		careerList.clear();
		//is joke! See? Yeah, bored. 
		CareerItem.Builder buildCareer = new CareerItem.Builder();
		buildCareer.setDetails(
				"Mid-to-Senior Java/Android Developer", 
				"Touch Lab Inc (New York, NY)", 
				"Are you a great dev who wants to work on new and interesting "
				+ "projects, with new problems to solve all the time? We’re "
				+ "looking for great devs who want to work on really interesting "
				+ "projects.\n\n"  +  

				"You know that Android is the dominant smartphone platform, and "
				+ "will be powering many different types of devices in the years "
				+ "to come.  At Touch Lab, Android is all that we do and our goal "
				+ "is to be the best Android development shop around.  Period.  "
				+ "Come help us make that happen.\n\n"+

				"We need developers that want to help define what great software "
				+ "development is, both for Android specifically, and small, "
				+ "agile teams in general.\n\n...", 
				"http://careers.stackoverflow.com/jobs/64475/mid-to-senior-java-android-developer-touch-lab-inc", 
				new Date());
		careerList.add(buildCareer.create());
		
		buildCareer = new CareerItem.Builder();
		buildCareer.setDetails(
				"Sr. QA Engineer - Mobile Automation", 
				"Pandora Media, Inc. (Oakland, CA)", "Description 2", 
				"http://careers.stackoverflow.com/jobs/67335/sr-qa-engineer-mobile-automation-pandora-media-inc", 
				new Date());
		careerList.add(buildCareer.create());
		
		buildCareer = new CareerItem.Builder();
		buildCareer.setDetails(
				"Android Developer", 
				"The LateRooms Group (Manchester, UK)", "Description 3", 
				"http://careers.stackoverflow.com/jobs/55085/android-developer-the-laterooms-group", 
				new Date());
		careerList.add(buildCareer.create());
		
		buildCareer = new CareerItem.Builder();
		buildCareer.setDetails(
				"Software Developer - Android Apps", 
				"Esri, Inc. (Redlands, CA)", "Description 4", 
				"http://careers.stackoverflow.com/jobs/65631/software-developer-android-apps-esri-inc", 
				new Date());
		careerList.add(buildCareer.create());
		
		buildCareer = new CareerItem.Builder();
		buildCareer.setDetails(
				"Senior Android Developer (m/f)", 
				"XING AG (Hamburg, Deutschland)", "Description 5", 
				"http://careers.stackoverflow.com/jobs/47374/senior-android-developer-m-f-xing-ag", 
				new Date());
		careerList.add(buildCareer.create());
		
		resultAdapter.setCareerItems(careerList);
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
	/// Implementer listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
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
		//TODO set up buttons
		case R.id.careerstack_searchResults_button_search:
			//TODO actual searching
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
			//TODO retry last search (ignoring fields)
			break;
		case R.id.careerstack_searchResults_button_cancel:
			//TODO cancel download
			//fall through
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.careerstack_searchResults_check_allowRemote:
			break;
		case R.id.careerstack_searchResults_check_offerRelocation:
			break;
		}		
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		CareerItem item = resultAdapter.getItem(position);
		if (item != null){
			Bundle state = new Bundle();
			buildSaveState(state); //holds the state
			mFragmentRequestListener.onRequestHoldSavedState(
					TAG_BACKSTACK_STATE, state);	
			
			Fragment frag = CareerItemFragment.newInstance(item);
			mFragmentRequestListener.onRequestNewFragment(
					frag, 
					CareerItemFragment.class.getName(), 
					true);
		}
	}
	
}
