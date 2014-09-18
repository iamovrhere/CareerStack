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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.ui.wrappers.SeekBarWrapper;

/** The dialog fragment for distance. Makes use of 
 * layout <code>viewstub_distance_seekbar.xml</code>. Requires the user use
 * <code>onActivityResult</code> and request codes to get result.
 * @author Jason J.
 * @version 0.1.0-20140917
 */
public class DistanceDialogFragment extends DialogFragment 
implements SeekBarWrapper.OnValueChangedListener, DialogInterface.OnClickListener {	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = DistanceDialogFragment.class
			.getSimpleName();
	
	/** Bundle key. The distance. Int. */
	final static public String KEY_DISTANCE = 
			CLASS_NAME + ".KEY_DISTANCE";
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The wrapper for the seekbar. */
	private SeekBarWrapper seekbarWrapper =  null;
	/** The displayed distance. */
	private TextView tv_distance = null;
	/** The current distance. */
	private int currentDistanceValue = 0;
	
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
		if (getArguments() != null){
			currentDistanceValue = 
					getArguments().getInt(KEY_DISTANCE, currentDistanceValue);
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
	}
	
	
	@SuppressLint("InflateParams")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog dialog  = 
		new AlertDialog.Builder(getActivity())
			.setTitle(getResources().getString(R.string.careerstack_dialog_distance_title))
			.setPositiveButton(android.R.string.ok, this)
			.setNegativeButton(android.R.string.cancel, this)
			.create(); //create basic dialog
				
		LayoutInflater inflater = (LayoutInflater)
				getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		//insert custom views
		View content = inflater.inflate(R.layout.viewstub_distance_seekbar, null);
		initViews(content); //preload views
		Resources r = getResources();
		content.setPadding(
				r.getDimensionPixelSize(R.dimen.dialog_margins), 
				r.getDimensionPixelSize(R.dimen.dialog_margins),
				r.getDimensionPixelSize(R.dimen.dialog_margins),
				r.getDimensionPixelSize(R.dimen.dialog_margins));
		dialog.setView(content);		
		onValueUpdate(currentDistanceValue);
		return dialog;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Initializes the views. */
	private void initViews(View rootView){
		tv_distance = (TextView)
				rootView.findViewById(R.id.careerstack_distanceSeekbar_text_value);
		SeekBar seekbar = (SeekBar)
				rootView.findViewById(R.id.careerstack_distanceSeekbar_seekBar);
		seekbarWrapper = new SeekBarWrapper(seekbar, 
				getResources().getInteger(R.integer.careerstack_seekBar_min), 
				getResources().getInteger(R.integer.careerstack_seekBar_max), 
				getResources().getInteger(R.integer.careerstack_seekBar_step));
		seekbarWrapper.setOnValueChangedListener(this);
		seekbarWrapper.setProgress(currentDistanceValue);		
	}
	
	/** Prepares the result.
	 * @param sendResult <code>true</code> to send result, <code>false</code> to cancel.
	 */
	private void prepareResults(boolean sendResult){
		if (sendResult){
		Intent data = new Intent();
		data.putExtra(KEY_DISTANCE, currentDistanceValue);
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
		currentDistanceValue = value;
		//TODO unit checking here
		tv_distance.setText(
				String.format(
						getString(R.string.careerstack_formatString_distanceValue_miles), 
						value)
				);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			currentDistanceValue = seekbarWrapper.getValue();
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
