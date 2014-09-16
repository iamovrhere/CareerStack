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
import com.ovrhere.android.careerstack.ui.wrappers.SeekBarWrapper;

/**
 * The main starting fragment. Currently a splash screen + entry screen.
 * 
 * @author Jason J.
 * @version 0.1.0-20140916
 */
public class MainFragment extends Fragment 
implements OnClickListener, OnCheckedChangeListener, 
	SeekBarWrapper.OnProgressChanged {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = 
	MainFragment.class.getSimpleName();
	/**Logtag for debugging purposes. */
	final static private String LOGTAG = CLASS_NAME;
	/** Whether or not to debug. */
	final static private boolean DEBUG = true;
	
	/** Bundle key. The value of keyword. String */
	final static private String KEY_KEYWORD_TEXT = 
			CLASS_NAME + ".KEY_KEYWORD_TEXT";
	/** Bundle key. The value of location. String. */
	final static private String KEY_LOCATION_TEXT = 
			CLASS_NAME + ".KEY_LOCATION_TEXT";
	/** Bundle key. The whether the remote check is set. Boolean. */
	final static private String KEY_REMOTE_ALLOWED = 
			CLASS_NAME + ".KEY_REMOTE_ALLOWED";
	/** Bundle key. The whether the relocation check is set. Boolean. */
	final static private String KEY_RELOCATE_OFFER = 
			CLASS_NAME + ".KEY_RELOCATE_OFFER";
	/** Bundle Key. The current distance in the seek bar. Int. */
	final static private String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The custom object for managing the seek bar. Can be <code>null</code>. */
	private SeekBarWrapper distanceSeekBarWrapper = null;
	/** The current distance. Used for saved states. */
	private int currentDistanceValue = 0;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public MainFragment() {}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_KEYWORD_TEXT, et_keywords.getText().toString());
		outState.putString(KEY_LOCATION_TEXT, et_location.getText().toString());
		
		outState.putBoolean(KEY_RELOCATE_OFFER, cb_relocationOffered.isChecked());
		outState.putBoolean(KEY_REMOTE_ALLOWED, cb_remoteAllowed.isChecked());
		
		outState.putInt(KEY_DISTANCE, currentDistanceValue);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);
		initInputs(rootView);
		if (savedInstanceState != null){
			processSavedState(rootView, savedInstanceState);
		}
		return rootView;
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
		String keywords = savedState.getString(KEY_KEYWORD_TEXT);
		if (keywords != null){
			et_keywords.setText(keywords);
		}
		String location = savedState.getString(KEY_LOCATION_TEXT);
		if (location != null){
			et_location.setText(location);
		}
		cb_relocationOffered.setChecked(
				savedState.getBoolean(KEY_RELOCATE_OFFER, false));
		cb_remoteAllowed.setChecked( 
				savedState.getBoolean(KEY_REMOTE_ALLOWED, false));
		
		currentDistanceValue = 
				savedState.getInt(KEY_DISTANCE, currentDistanceValue);
	}
	
	
	/** Not intented to be called in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
	 * @return <code>true</code> if successfully initialized seekbar,
	 * <code>false</code> if something went wrong.
	 * @param view The view to initialize with.
	 */
	private boolean initSeekBar(View view){
		SeekBar sb_distanceSeek = (SeekBar) 
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
			distanceSeekBarWrapper.setOnProgressChangedListener(this);
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
	 * @param visible <code>true</code> to show, <code>false</code> to hide.
	 */
	private void showSeekBar(boolean visible){
		distanceView.setVisibility(visible ? View.VISIBLE : View.GONE);
		if (distanceSeekBarWrapper == null){
			if (getView() == null || initSeekBar(getView()) == false){
				if (DEBUG){
					Log.w(LOGTAG, 
							"Failed to initialize seek bar (rootView not ready?");
				}
				return; //we failed, cannot update anything et.
			}
		}
		int progress = distanceSeekBarWrapper.getValue();
		updateDistance(progress);
	}
	
	/** Updates distance to the value supplied.
	 * @param value The distance value 
	 */
	private void updateDistance(int value){
		currentDistanceValue = value;
		if (tv_distance != null){
			//TODO metric unit checks
			tv_distance.setText(
					String.format(
							getResources().getString(R.string.careerstack_formatString_distanceValue_miles), 
							value)
					);
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
			//TODO search action (i.e. launch intent/fragment)
			break;
		}		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.careerstack_main_check_allowRemote:
			// TODO set preferences here
			break;
		case R.id.careerstack_main_check_offerRelocation:
			
			//break;
		default:
			//break;
		}			
	}
	
	@Override
	public void onValueUpdate(int progress) {
		updateDistance(progress);		
	}
}
