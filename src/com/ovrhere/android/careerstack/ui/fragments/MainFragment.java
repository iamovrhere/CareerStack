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
import android.widget.TextView;

import com.gc.materialdesign.views.Slider;
import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.wrappers.MaterialSliderWrapper;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/**
 * The main starting fragment. Currently a splash screen + entry screen.
 * Expects Activity to implement {@link OnFragmentInteractionListener} and 
 * will throw {@link ClassCastException} otherwise.
 * 
 * @author Jason J.
 * @version 0.4.0-20151006
 */
public class MainFragment extends Fragment implements OnClickListener, 
	OnCheckedChangeListener, MaterialSliderWrapper.OnValueChangedListener {
	
	/** Class name for debugging purposes. */
	private static final String CLASS_NAME = 
			MainFragment.class.getSimpleName();
	
	/** The suggested fragment tag. */
	public static final String FRAGTAG = CLASS_NAME;
	
	/**Logtag for debugging purposes. */
	private static final String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	private static final boolean DEBUG = false;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start public keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Bundle key. The value of keyword. String */
	public static final String KEY_KEYWORD_TEXT = 
			CLASS_NAME + ".KEY_KEYWORD_TEXT";
	
	/** Bundle key. The value of location. String. */
	public static final String KEY_LOCATION_TEXT = 
			CLASS_NAME + ".KEY_LOCATION_TEXT";
	
	/** Bundle key. The whether the remote check is set. Boolean. */
	public static final String KEY_REMOTE_ALLOWED = 
			CLASS_NAME + ".KEY_REMOTE_ALLOWED";
	
	/** Bundle key. The whether the relocation check is set. Boolean. */
	public static final String KEY_RELOCATE_OFFER = 
			CLASS_NAME + ".KEY_RELOCATE_OFFER";
	
	/** Bundle Key. The current distance in the seek bar. Int. */
	public static final String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The keyword input. */
	private EditText mEt_keywords = null;
	/** The location input. */
	private EditText mEt_location = null;
	/** The remote allowed checkbox. */
	private CompoundButton mCb_remoteAllowed = null;
	/** The relocation checkbox. */
	private CompoundButton mCb_relocationOffered =  null;
	
	/** The parent view for setting distance. */
	private View mDistanceView = null;		
	/** The text view to update. Can be <code>null</code>. */
	private TextView mTv_distance = null;
	
	/** The slider reference. */
	private Slider mSliderDistance = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The custom object for managing the seek bar. Can be <code>null</code>. */
	private MaterialSliderWrapper mDistanceSliderWrapper = null;
	/** The current distance. Used for saved states. */
	private int mCurrentDistanceValue = 0;
	/** The fragment request listener from main. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	
	/** Used in {@link #onSaveInstanceState(Bundle)} to determine if 
	 * views are visible. */
	private boolean mViewBuilt = false;
	
	/** The shared preference handle. */
	private SharedPreferences mPrefs = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MainFragment() {}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_DISTANCE, mCurrentDistanceValue);
		
		if (mViewBuilt == false){
			return; //no view? don't bother.
		}
		outState.putString(KEY_KEYWORD_TEXT, mEt_keywords.getText().toString());
		outState.putString(KEY_LOCATION_TEXT, mEt_location.getText().toString());
		
		outState.putBoolean(KEY_RELOCATE_OFFER, mCb_relocationOffered.isChecked());
		outState.putBoolean(KEY_REMOTE_ALLOWED, mCb_remoteAllowed.isChecked());
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = PreferenceUtils.getPreferences(getActivity());		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		mDistanceSliderWrapper = null; 
		mCurrentDistanceValue = 
				getResources().getInteger(R.integer.careerstack_seekBar_default);
		
		initInputs(rootView);
		processPrefs(rootView);
		
		if (savedInstanceState != null){
			processSavedState(rootView, savedInstanceState);
		}
		
		mViewBuilt = true;
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroy();
		mViewBuilt = false;
		mDistanceSliderWrapper = null; //view is destroyed; destroy refs
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
		mEt_keywords = (EditText) 
				rootView.findViewById(R.id.careerstack_main_editin_keywords);
		mEt_location = (EditText)
				rootView.findViewById(R.id.careerstack_main_editin_location);
		mEt_location.addTextChangedListener(locationTextWatcher);
		
		mCb_relocationOffered = (CompoundButton)
				rootView.findViewById(R.id.careerstack_main_check_offerRelocation);
		mCb_remoteAllowed = (CompoundButton)
				rootView.findViewById(R.id.careerstack_main_check_allowRemote);

		//currently not inflated
		mDistanceView = 
				rootView.findViewById(R.id.careerstack_main_viewStub_distance);		
		
		mCb_relocationOffered.setOnCheckedChangeListener(this);
		mCb_remoteAllowed.setOnCheckedChangeListener(this);
		
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
			mEt_keywords.setText(keywords);
		}
		String location = savedState.getString(KEY_LOCATION_TEXT);
		if (location != null){
			mEt_location.setText(location);
		}
		mCb_relocationOffered.setChecked(
				savedState.getBoolean(KEY_RELOCATE_OFFER));
		mCb_remoteAllowed.setChecked( 
				savedState.getBoolean(KEY_REMOTE_ALLOWED));
		
		locationTextWatcher.afterTextChanged(mEt_location.getEditableText());
		int value = savedState.getInt(KEY_DISTANCE, mCurrentDistanceValue);
		updateDistance(value);
	}
	
	/** Processes and applied preferences, if allowed. */
	private void processPrefs(View rootView){
		if (mPrefs.getBoolean(
				getString(R.string.careerstack_pref_KEY_KEEP_SEARCH_SETTINGS), 
				false) == false){
			//if we are not suppose to keep settings, discard them.
			return;
		}
		
		mEt_keywords.setText(
				mPrefs.getString(
						getString(R.string.careerstack_pref_KEY_KEYWORDS_VALUE),
						"")
				);
		String location = mPrefs.getString(
				getString(R.string.careerstack_pref_KEY_LOCATION_VALUE),
				"");
		mEt_location.setText(location);
		
		int value = mPrefs.getInt(
				getString(R.string.careerstack_pref_KEY_DISTANCE_VALUE), 
				mCurrentDistanceValue);
		updateDistance(value);
		
		//if location is not empty; prep the seekbar
		showSeekBar(rootView, location.isEmpty() == false);
		
		mCb_relocationOffered.setChecked(
				mPrefs.getBoolean(
						getString(R.string.careerstack_pref_KEY_RELOCATION_OFFERED), 
						false));
		mCb_remoteAllowed.setChecked( 
				mPrefs.getBoolean(
						getString(R.string.careerstack_pref_KEY_REMOTE_ALLOWED), 
						false));
	}
	
	/** Not intended to be called in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * @return <code>true</code> if successfully initialized Slider,
	 * <code>false</code> if something went wrong.
	 * @param view The view to initialize with.
	 */
	private boolean initSeekBar(View view){
		mSliderDistance = (Slider) 
				view.findViewById(R.id.careerstack_distanceSeekbar_slider);
		mTv_distance = (TextView)
			view.findViewById(R.id.careerstack_distanceSeekbar_text_value);
		
		if (mSliderDistance != null && mTv_distance != null){
			Resources r = getResources();
			mDistanceSliderWrapper = new MaterialSliderWrapper(
					mSliderDistance,
					r.getInteger(R.integer.careerstack_seekBar_min),
					r.getInteger(R.integer.careerstack_seekBar_max),
					r.getInteger(R.integer.careerstack_seekBar_step)
					);
			mDistanceSliderWrapper.setOnValueChangedListener(this);
			mDistanceSliderWrapper.setProgress(mCurrentDistanceValue);
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
		mDistanceView.setVisibility(visible ? View.VISIBLE : View.GONE);
		if (mDistanceSliderWrapper == null){
			if (view == null || initSeekBar(view) == false){
				if (DEBUG){
					Log.w(LOGTAG, 
							"Failed to initialize seek bar (rootView not ready?");
				}
				return; //we failed, cannot update anything yet.
			}
		}
		int progress = mDistanceSliderWrapper.getValue();
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
		mCurrentDistanceValue = value;
		//get distance with units
		String distance = UnitCheck.units(mPrefs, getResources(), value);
		if (mTv_distance != null){
			mTv_distance.setText(distance);
		}
		if (mSliderDistance != null){
			//set accessibility string
			mSliderDistance.setContentDescription(distance);
		}
	}
	
	/** Returns the distance doing necessary null checks. 
	 * @return The distance or -1;
	 */
	private int getDistance(){
		if (mDistanceSliderWrapper != null){
			return mDistanceSliderWrapper.getValue();
		}
		return -1;
	}
	
	/** Builds the bundle and sends the request off */
	private void prepareAndRequestSearch() {
		Bundle args = new Bundle();
		
		String keywords = mEt_keywords.getText().toString();
		String location = mEt_location.getText().toString();
		//store prefs as we are sending
		mPrefs.edit()
			.putString(
				getString(R.string.careerstack_pref_KEY_KEYWORDS_VALUE),
				keywords)
			.putString(
				getString(R.string.careerstack_pref_KEY_LOCATION_VALUE),
				location)
			.commit();
		
		args.putString(KEY_KEYWORD_TEXT, keywords);
		args.putString(KEY_LOCATION_TEXT, location);
		
		args.putBoolean(KEY_RELOCATE_OFFER, mCb_relocationOffered.isChecked());
		args.putBoolean(KEY_REMOTE_ALLOWED, mCb_remoteAllowed.isChecked());
		
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
				Log.d(LOGTAG, "Keywords:"+mEt_keywords.getText().toString());
				Log.d(LOGTAG, "Location:"+mEt_location.getText().toString());
				Log.d(LOGTAG, "Remote:"+mCb_remoteAllowed.isChecked());
				Log.d(LOGTAG, "Relocation:"+mCb_relocationOffered.isChecked());
				Log.d(LOGTAG, "Distance:"+getDistance());
			}
			
			prepareAndRequestSearch();
		}		
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences.Editor edit = mPrefs.edit();
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
		mPrefs.edit()
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
