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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.TextView;

import com.gc.materialdesign.views.Slider;
import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.wrappers.MaterialSliderWrapper;
import com.ovrhere.android.careerstack.utils.UnitCheck;

/** The dialog fragment for distance. Makes use of 
 * layout <code>viewstub_distance_seekbar.xml</code>. Requires the user use
 * <code>onActivityResult</code> and request codes to get result.
 * @author Jason J.
 * @version 0.3.0-20151006
 */
public class DistanceDialogFragment extends DialogFragment implements 
	MaterialSliderWrapper.OnValueChangedListener, DialogInterface.OnClickListener {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = DistanceDialogFragment.class
			.getSimpleName();
	
	/** Bundle key. The distance. Int. */
	final static public String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The shared preferences. */
	private SharedPreferences mPrefs = null;
	
	/** The wrapper for the seekbar. */
	private MaterialSliderWrapper mSliderWrapper =  null;
	/** The seekbar reference. */
	private Slider mSliderSeekbar = null;
	/** The displayed distance. */
	private TextView mTv_distance = null;
	
	/** The current distance. */
	private int mCurrentDistanceValue = 0;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End memebers
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Factory methods to create new instance of the dialog. 
	 * @param target The target fragment to send results to
	 * @param requestCode The request code to listen to in 
	 * {@link Fragment#onActivityResult(int, int, Intent)} 
	 * @param distance The starting distance*/
	public static DistanceDialogFragment newInstance(Fragment target, 
			int requestCode, 
			int distance) {
		DistanceDialogFragment frag = new DistanceDialogFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_DISTANCE, distance);
		frag.setArguments(args);
		frag.setTargetFragment(target, requestCode);
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true); //retain.
		mPrefs = PreferenceUtils.getPreferences(getActivity());
		
		if (getArguments() != null){
			mCurrentDistanceValue = 
					getArguments().getInt(KEY_DISTANCE, mCurrentDistanceValue);
		}
	}
	/* Required for compatibility library bug:
	 * http://stackoverflow.com/questions/8235080/fragments-dialogfragment-and-screen-rotation
	 */
	@Override
	public void onDestroyView() {
	  if (getDialog() != null && getRetainInstance())
	    getDialog().setOnDismissListener(null);
	  super.onDestroyView();
	  mSliderWrapper = null;
	  mSliderSeekbar = null;
	  mTv_distance = null;
	}
	
	
	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context ctx = getCompatContext();
		AlertDialog dialog  = 
		new AlertDialog.Builder(ctx)
			.setTitle(getResources().getString(R.string.careerstack_dialog_distance_title))
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create(); //create basic dialog
				
		//insert custom views
		View content = View.inflate(ctx, 
				R.layout.viewstub_distance_seekbar, null);
		initViews(content); //preload views
		
		Resources r = getResources();
		content.setPadding(
				r.getDimensionPixelSize(R.dimen.dialog_margins), 
				r.getDimensionPixelSize(R.dimen.dialog_margins),
				r.getDimensionPixelSize(R.dimen.dialog_margins),
				r.getDimensionPixelSize(R.dimen.dialog_margins));
		dialog.setView(content);		
		onValueUpdate(mCurrentDistanceValue);
		return dialog;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Gets the context and, by extension, the theme to use. */
	private Context getCompatContext(){
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1){
			return getActivity();
		}
		int themeId = R.style.DialogTheme;
		return new ContextThemeWrapper(getActivity(), themeId);
	}
	
	/** Initializes the views. */
	private void initViews(View rootView){
		mTv_distance = (TextView)
				rootView.findViewById(R.id.careerstack_distanceSeekbar_text_value);
		mSliderSeekbar = (Slider)
				rootView.findViewById(R.id.careerstack_distanceSeekbar_slider);
		mSliderWrapper = new MaterialSliderWrapper(mSliderSeekbar, 
				getResources().getInteger(R.integer.careerstack_seekBar_min), 
				getResources().getInteger(R.integer.careerstack_seekBar_max), 
				getResources().getInteger(R.integer.careerstack_seekBar_step));
		mSliderWrapper.setOnValueChangedListener(this);
		mSliderWrapper.setProgress(mCurrentDistanceValue);		
	}
	
	/** Prepares the result.
	 * @param sendResult <code>true</code> to send result, <code>false</code> to cancel.
	 */
	private void prepareResults(boolean sendResult){
		if (sendResult){
			Intent data = new Intent();
			data.putExtra(KEY_DISTANCE, mCurrentDistanceValue);
			getTargetFragment().onActivityResult(getTargetRequestCode(), 
					Activity.RESULT_OK, data);
		} else {
			getTargetFragment().onActivityResult(getTargetRequestCode(), 
					Activity.RESULT_CANCELED, null);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onValueUpdate(int value) {
		mCurrentDistanceValue = value;
		String distance = UnitCheck.units(mPrefs, getResources(), value);
		mTv_distance.setText(distance);
		mSliderSeekbar.setContentDescription(distance);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			mCurrentDistanceValue = mSliderWrapper.getValue();
			prepareResults(true);
			dialog.dismiss();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			prepareResults(false);
			dialog.dismiss();
			break;
		}		
	}

}
