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
package com.ovrhere.android.careerstack.ui.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.wrappers.SeekBarWrapper;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/**
 * The main starting fragment. Currently a splash screen + entry screen.
 * Expects Activity to implement {@link OnFragmentInteractionListener} and 
 * will throw {@link ClassCastException} otherwise.
 * 
 * @author Jason J.
 * @version 0.3.0-20141003
 */
public class MainFragment extends Fragment 
	implements OnClickListener, OnCheckedChangeListener, 
				SeekBarWrapper.OnValueChangedListener {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = 
			MainFragment.class.getSimpleName();
	
	/** The suggested fragment tag. */
	final static public String FRAGTAG = CLASS_NAME;
	
	/**Logtag for debugging purposes. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	final static private boolean DEBUG = false;
	
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The keyword input. */
	private EditText et_keywords = null;
	/** The location input. */
	private EditText et_location = null;
	/** The remote allowed checkbox. */
	private CompoundButton cb_remoteAllowed = null;
	/** The relocation checkbox. */
	private CompoundButton cb_relocationOffered =  null;
	
	/** The parent view for setting distance. */
	private View distanceView = null;		
	/** The text view to update. Can be <code>null</code>. */
	private TextView tv_distance = null;
	
	/** The seekbar reference. */
	private SeekBar sb_distanceSeek = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The custom object for managing the seek bar. Can be <code>null</code>. */
	private SeekBarWrapper distanceSeekBarWrapper = null;
	/** The current distance. Used for saved states. */
	private int currentDistanceValue = 0;
	/** The fragment request listener from main. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	
	/** Used in {@link #onSaveInstanceState(Bundle)} to determine if 
	 * views are visible. */
	private boolean viewBuilt = false;
	
	/** The shared preference handle. */
	private SharedPreferences prefs = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MainFragment() {}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_DISTANCE, currentDistanceValue);
		
		if (viewBuilt == false){
			return; //no view? don't bother.
		}
		outState.putString(KEY_KEYWORD_TEXT, et_keywords.getText().toString());
		outState.putString(KEY_LOCATION_TEXT, et_location.getText().toString());
		
		outState.putBoolean(KEY_RELOCATE_OFFER, cb_relocationOffered.isChecked());
		outState.putBoolean(KEY_REMOTE_ALLOWED, cb_remoteAllowed.isChecked());
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceUtils.getPreferences(getActivity());		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		distanceSeekBarWrapper = null; 
		currentDistanceValue = 
				getResources().getInteger(R.integer.careerstack_seekBar_default);
		
		initInputs(rootView);
		processPrefs(rootView);
		
		if (savedInstanceState != null){
			processSavedState(rootView, savedInstanceState);
		}
		
		viewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroy();
		viewBuilt = false;
		distanceSeekBarWrapper = null; //view is destroyed; destroy refs
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
	/// Initialize helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes all the native input views on the fragment. */
	private void initInputs(View rootView){
		et_keywords = (EditText) 
				rootView.findViewById(R.id.careerstack_main_editin_keywords);
		et_location = (EditText)
				rootView.findViewById(R.id.careerstack_main_editin_location);
		et_location.addTextChangedListener(locationTextWatcher);
		
		cb_relocationOffered = (CompoundButton)
				rootView.findViewById(R.id.careerstack_main_check_offerRelocation);
		cb_remoteAllowed = (CompoundButton)
				rootView.findViewById(R.id.careerstack_main_check_allowRemote);

		//currently not inflated
		distanceView = 
				rootView.findViewById(R.id.careerstack_main_viewStub_distance);		
		
		cb_relocationOffered.setOnCheckedChangeListener(this);
		cb_remoteAllowed.setOnCheckedChangeListener(this);
		
		Button search = (Button) 
				rootView.findViewById(R.id.careerstack_main_button_search);
		search.setOnClickListener(this);
	}
	
	/** Processes the returning state and applies values to views
	 * Assumes views are valid.
	 * @param rootView any last minute changes done with this
	 * @param savedState The state to unpack
	 * @see #initInputs(View)
	 */
	private void processSavedState(View rootView, Bundle savedState){
		if (DEBUG){
			Log.d(LOGTAG, "Process save state");
		}
		String keywords = savedState.getString(KEY_KEYWORD_TEXT);
		if (keywords != null){
			et_keywords.setText(keywords);
		}
		String location = savedState.getString(KEY_LOCATION_TEXT);
		if (location != null){
			et_location.setText(location);
		}
		cb_relocationOffered.setChecked(
				savedState.getBoolean(KEY_RELOCATE_OFFER));
		cb_remoteAllowed.setChecked( 
				savedState.getBoolean(KEY_REMOTE_ALLOWED));
		
		locationTextWatcher.afterTextChanged(et_location.getEditableText());
		int value = savedState.getInt(KEY_DISTANCE, currentDistanceValue);
		updateDistance(value);
	}
	
	/** Processes and applied preferences, if allowed. */
	private void processPrefs(View rootView){
		if (prefs.getBoolean(
				getString(R.string.careerstack_pref_KEY_KEEP_SEARCH_SETTINGS), 
				false) == false){
			//if we are not suppose to keep settings, discard them.
			return;
		}
		
		et_keywords.setText(
				prefs.getString(
						getString(R.string.careerstack_pref_KEY_KEYWORDS_VALUE),
						"")
				);
		String location = prefs.getString(
				getString(R.string.careerstack_pref_KEY_LOCATION_VALUE),
				"");
		et_location.setText(location);
		
		int value = prefs.getInt(
				getString(R.string.careerstack_pref_KEY_DISTANCE_VALUE), 
				currentDistanceValue);
		updateDistance(value);
		
		//if location is not empty; prep the seekbar
		showSeekBar(rootView, location.isEmpty() == false);		
		
		cb_relocationOffered.setChecked(
				prefs.getBoolean(
						getString(R.string.careerstack_pref_KEY_RELOCATION_OFFERED), 
						false));
		cb_remoteAllowed.setChecked( 
				prefs.getBoolean(
						getString(R.string.careerstack_pref_KEY_REMOTE_ALLOWED), 
						false));
	}
	
	/** Not intended to be called in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * @return <code>true</code> if successfully initialized seekbar,
	 * <code>false</code> if something went wrong.
	 * @param view The view to initialize with.
	 */
	private boolean initSeekBar(View view){
		sb_distanceSeek = (SeekBar) 
				view.findViewById(R.id.careerstack_distanceSeekbar_seekBar);
		tv_distance = (TextView)
			view.findViewById(R.id.careerstack_distanceSeekbar_text_value);
		
		if (sb_distanceSeek != null && tv_distance != null){
			Resources r = getResources();
			distanceSeekBarWrapper = new SeekBarWrapper(
					sb_distanceSeek,
					r.getInteger(R.integer.careerstack_seekBar_min),
					r.getInteger(R.integer.careerstack_seekBar_max),
					r.getInteger(R.integer.careerstack_seekBar_step)
					);
			distanceSeekBarWrapper.setOnValueChangedListener(this);
			distanceSeekBarWrapper.setProgress(currentDistanceValue);
			return true;
		} else {
			return false; //end early
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Shows or hides seekbar block based bool.
	 * If being shown for the first time, the view is inflated and the stepper
	 * initialized.
	 * @param view The view to inflate with  
	 * @param visible <code>true</code> to show, <code>false</code> to hide.
	 */
	private void showSeekBar(View view, boolean visible){
		distanceView.setVisibility(visible ? View.VISIBLE : View.GONE);
		if (distanceSeekBarWrapper == null){
			if (view == null || initSeekBar(view) == false){
				if (DEBUG){
					Log.w(LOGTAG, 
							"Failed to initialize seek bar (rootView not ready?");
				}
				return; //we failed, cannot update anything yet.
			}
		}
		int progress = distanceSeekBarWrapper.getValue();
		updateDistance(progress);
	} 
	
	/** Shows or hides seekbar block based bool.
	 * If being shown for the first time, the view is inflated and the stepper
	 * initialized. Same as {@link #showSeekBar(View, boolean)} with #getView()
	 * @param visible <code>true</code> to show, <code>false</code> to hide.
	 */
	private void showSeekBar(boolean visible){
		showSeekBar(getView(), visible);
	}
	
	/** Updates distance to the value supplied.
	 * @param value The distance value 
	 */
	private void updateDistance(int value){
		currentDistanceValue = value;
		//get distance with units
		String distance = UnitCheck.units(prefs, getResources(), value);
		if (tv_distance != null){
			tv_distance.setText(distance);
		}
		if (sb_distanceSeek != null){
			//set accessibility string
			sb_distanceSeek.setContentDescription(distance);
		}
	}
	
	/** Returns the distance doing necessary null checks. 
	 * @return The distance or -1;
	 */
	private int getDistance(){
		if (distanceSeekBarWrapper != null){
			return distanceSeekBarWrapper.getValue();
		}
		return -1;
	}
	
	/** Builds the bundle and sends the request off */
	private void prepareAndRequestSearch() {
		Bundle args = new Bundle();
		
		String keywords = et_keywords.getText().toString();
		String location = et_location.getText().toString();
		//store prefs as we are sending
		prefs.edit()
			.putString(
				getString(R.string.careerstack_pref_KEY_KEYWORDS_VALUE),
				keywords)
			.putString(
				getString(R.string.careerstack_pref_KEY_LOCATION_VALUE),
				location)
			.commit();
		
		args.putString(KEY_KEYWORD_TEXT, keywords);
		args.putString(KEY_LOCATION_TEXT, location);
		
		args.putBoolean(KEY_RELOCATE_OFFER, cb_relocationOffered.isChecked());
		args.putBoolean(KEY_REMOTE_ALLOWED, cb_remoteAllowed.isChecked());
		
		if (getDistance() > 0){
			args.putInt(KEY_DISTANCE, getDistance());
		}
		
		mFragInteractionListener.onSearchRequest(args);
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
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
			showSeekBar(content.isEmpty() == false);
		}
	};
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.careerstack_main_button_search:
			if (DEBUG){
				Log.d(LOGTAG, "Keywords:"+et_keywords.getText().toString());
				Log.d(LOGTAG, "Location:"+et_location.getText().toString());
				Log.d(LOGTAG, "Remote:"+cb_remoteAllowed.isChecked());
				Log.d(LOGTAG, "Relocation:"+cb_relocationOffered.isChecked());
				Log.d(LOGTAG, "Distance:"+getDistance());
			}
			
			prepareAndRequestSearch();
		}		
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences.Editor edit = prefs.edit();
		switch (buttonView.getId()) {
		case R.id.careerstack_main_check_allowRemote:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_REMOTE_ALLOWED), 
					isChecked).commit();
			break;
		case R.id.careerstack_main_check_offerRelocation:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_RELOCATION_OFFERED), 
					isChecked).commit();
			break;
		default:
			//break;
		}			
	}
	
	@Override
	public void onValueUpdate(int value) {
		updateDistance(value);
		prefs.edit()
			.putInt(
				getString(R.string.careerstack_pref_KEY_DISTANCE_VALUE), 
				value).commit();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Returning to the roots of simple design. We re-opt for this.
	/**
	 * The interaction listener that the activity must implement to handle the 
	 * {@link MainFragment}'s requests. 
	 * @author Jason J.
	 * @version 0.1.0-20141003
	 */
	static public interface OnFragmentInteractionListener {		
		/** Sends activity a search request to be handled.
		 * @param bundle The bundle of search arguments given by the 
		 * {@link MainFragment} keys.
		 * @return <code>true</code> if the activity has honoured the request,
		 * <code>false</code> if has been ignored.		 */
		public boolean onSearchRequest(Bundle bundle);
	}
}
