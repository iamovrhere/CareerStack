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
package com.ovrhere.android.careerstack.ui.fragments.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/** Creates a search dialog akin to a search bar.
 * <p>
 * Note that the activity must implement {@link OnDialogResultsListener}
 * or it will throw a {@link ClassCastException}. Note that the dialog will dismiss itself
 * after search or cancel is pressed.
 * </p>
 * @author Jason J.
 * @version 0.1.2-20141118
 */
public class SearchBarDialogFragment extends DialogFragment 
	implements OnClickListener, OnCheckedChangeListener {

	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = SearchBarDialogFragment.class
			.getSimpleName();
	

	/** The request code for the distance fragment. Used with 
	 * {@link #onActivityResult(int, int, Intent)} and 
	 * {@link DistanceDialogFragment}.	 */
	final static private int REQUEST_CODE_DISTANCE_FRAG = 0x101;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start public keys
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
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End views
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The listener for the activity. */
	private OnDialogResultsListener mResultsListener = null;
	
	/** Whether or not the dialog has been dismissed.
	 * Must be <code>true</code> to dismiss. */
	private boolean dialogDismissed = false;
	
	/** The current distance. Used for saved states. */
	private int currentDistanceValue = 0;
	
	/** The shared preferences to used. */
	private SharedPreferences prefs = null;
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The factory method for dialog construction. 
	 * @param args The args to forward one with the initial search params
	 * @return The dialog fragment to show
	 */
	static public SearchBarDialogFragment newInstance(Bundle args){
		SearchBarDialogFragment frag = new SearchBarDialogFragment();
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		buildSaveState(outState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*set style before inflation to allow:
		 * 1. No border
		 * 2. Forced dark themes 
		 */
		setStyle(R.style.SearchBarDialogStyle, R.style.SearchBarDialogStyle_Dark);
		
		currentDistanceValue = 
				getResources().getInteger(R.integer.careerstack_seekBar_default);
		prefs = PreferenceUtils.getPreferences(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		dialogDismissed = false;
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.dialog_fragment_search_bar, container,
						false);
		
		initAnimationAndDim();  				 
		initInputs(rootView);
		
		if (savedInstanceState != null){
			processArgBundle(savedInstanceState);
		} else if (getArguments() != null){
			processArgBundle(getArguments());
		}
		
		return rootView;
	}

	
	
	@Override
	public void onResume() {
		super.onResume();
		//must be done after view is created
		if (getDialog() != null){
			Window win = getDialog().getWindow();
			win.setLayout(	ViewGroup.LayoutParams.MATCH_PARENT, 
							ViewGroup.LayoutParams.WRAP_CONTENT);
			getDialog().setCanceledOnTouchOutside(true);
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
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mResultsListener = (OnDialogResultsListener) activity;
		} catch (ClassCastException e){
			Log.e(CLASS_NAME, 
					"Activity must implement 'DialogInterface.OnClickListener': " +e);
			throw e;					
		}
	}
	
	/* Required for compatibility library bug:
	 * http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
	 */
	@Override
	public void onDestroyView() {
	  if (getDialog() != null && dialogDismissed == false){
	    getDialog().setOnDismissListener(null);
	  }
	  super.onDestroyView();
	}
	
		
	@Override
	public void dismiss() {
		if (dialogDismissed){
			//only if the dialog has been formally dismissed do we dismiss.
			super.dismiss();
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initializer helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes the dialogs animations and dim. */
	private void initAnimationAndDim() {
		Window win = getDialog().getWindow();
		win.setGravity(Gravity.TOP);
        win.setWindowAnimations(R.style.SearchBarAnimation);
        
        WindowManager.LayoutParams lp = win.getAttributes();  
        lp.dimAmount = 0.3f;   //TODO abstract? is it necessary?
        win.setAttributes(lp);  
        win.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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
				rootView.findViewById(R.id.careerstack_searchBar_editin_keywords);
		et_location = (EditText)
				rootView.findViewById(R.id.careerstack_searchBar_editin_location);
		et_location.addTextChangedListener(locationTextWatcher);
	}
	
	/** Initializes checkboxes. */
	private void initCheckBoxes(View rootView) {
		cb_relocationOffered = (CompoundButton)
				rootView.findViewById(R.id.careerstack_searchBar_check_offerRelocation);
		cb_remoteAllowed = (CompoundButton)
				rootView.findViewById(R.id.careerstack_searchBar_check_allowRemote);		
		cb_relocationOffered.setOnCheckedChangeListener(this); 
		cb_remoteAllowed.setOnCheckedChangeListener(this);
	}
	
	/** Initializes buttons, finding and setting listeners. */
	private void initButtons(View rootView) {
		btn_distance = (Button) 
				rootView.findViewById(R.id.careerstack_searchBar_button_distance);
		btn_distance.setOnClickListener(this);
		
		Button btn_search = (Button) 
				rootView.findViewById(R.id.careerstack_searchBar_button_search);
		btn_search.setOnClickListener(this);		
		ImageButton imgbtn_cancel = (ImageButton) 
				rootView.findViewById(R.id.careerstack_searchBar_imgbutton_cancel);
		imgbtn_cancel.setOnClickListener(this);
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
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Dismiss helper that dismisses dialog by setting #dialogDismissed true
	 * and then calling this.dismiss(). */
	private void dismissDialog() {
		dialogDismissed = true;
		this.dismiss();
	}
	
	/** Builds the saved state from views (if built) and other elements. */
	private void buildSaveState(Bundle outState) {
		outState.putString(KEY_KEYWORD_TEXT, et_keywords.getText().toString());
		outState.putString(KEY_LOCATION_TEXT, et_location.getText().toString());
		
		outState.putBoolean(KEY_RELOCATE_OFFER, cb_relocationOffered.isChecked());
		outState.putBoolean(KEY_REMOTE_ALLOWED, cb_remoteAllowed.isChecked());		
		
		outState.putInt(KEY_DISTANCE, currentDistanceValue);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// View helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
		

	
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
			btn_distance.setEnabled(content.isEmpty() == false);
		}
	};
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.careerstack_searchBar_button_search:
			Bundle results  = new Bundle();
			buildSaveState(results);						
			prefs.edit()
				.putString(
						getString(R.string.careerstack_pref_KEY_KEYWORDS_VALUE),
						et_keywords.getText().toString())
				.putString(
						getString(R.string.careerstack_pref_KEY_LOCATION_VALUE),
						et_location.getText().toString())
				.commit();
			
			mResultsListener.onSearch(this, results);
			dismissDialog();
			break;
			
		case R.id.careerstack_searchBar_imgbutton_cancel:
			mResultsListener.onCancel(this);
			dismissDialog();
			break;
			
		case R.id.careerstack_searchBar_button_distance:
			DistanceDialogFragment dialog = 
				DistanceDialogFragment.newInstance(this,
						REQUEST_CODE_DISTANCE_FRAG, 
						currentDistanceValue);
			dialog.show(getFragmentManager(), 
					DistanceDialogFragment.class.getName());			
			break;
		}				
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences.Editor edit = prefs.edit();
		switch (buttonView.getId()) {
		case R.id.careerstack_searchBar_check_allowRemote:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_REMOTE_ALLOWED), 
					isChecked).commit();
			break;
		case R.id.careerstack_searchBar_check_offerRelocation:
			edit.putBoolean(
					getString(R.string.careerstack_pref_KEY_RELOCATION_OFFERED), 
					isChecked).commit();
			break;
		}		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The dialog result interface
	 * 
	 * @author Jason J.
	 * @version 0.1.0-20141006
	 */
	static public interface OnDialogResultsListener{
		/** The action given when search is pressed.
		 * @param dialog The dialog itself 
		 * @param searchParams The search parameters in the dialog */
		public void onSearch(DialogFragment dialog, Bundle searchParams);
		/** The action given when the dialog is cancelled. 
		 * @param dialog The dialog itself */
		public void onCancel(DialogFragment dialog);
	}
}
