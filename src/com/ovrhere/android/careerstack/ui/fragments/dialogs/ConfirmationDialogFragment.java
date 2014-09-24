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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;

/** Simple confirmation dialog that retains it's instance.
 * Give a title, message, positive, negative buttons.
 * Requires either activity to implement 
 * DialogInterface.OnClickListener or 
 * call {@link Builder#setTargetFragment(Fragment, int)}
 * @author Jason J.
 * @version 0.1.0-20140923
 */
public class ConfirmationDialogFragment extends DialogFragment 
	implements DialogInterface.OnClickListener {
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = ConfirmationDialogFragment.class
			.getSimpleName();
	
	/** Bundle Key. The title resource id. Int. */
	final static private String KEY_TITLE_RES = 
			CLASS_NAME + ".KEY_TITLE_RES";
	/** Bundle Key. The message resource id. Int. */
	final static private String KEY_MESSAGE_RES = 
			CLASS_NAME + ".KEY_MESSAGE_RES";
	/** Bundle Key. The positive button resource id. Int. */
	final static private String KEY_POSITIVE_RES = 
			CLASS_NAME +".KEY_POSITIVE_RES";
	/** Bundle Key. The negative button resource id. Int. */
	final static private String KEY_NEGATIVE_RES = 
			CLASS_NAME +".KEY_NEGATIVE_RES";
		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	private int positiveResource = android.R.string.ok;
	private int negativeResource = android.R.string.cancel;
	private int messageResource = -1;
	private int titleResource = android.R.string.dialog_alert_title ;
	
	/** The optional listener that is sometimes activity. Can be null. */
	private DialogInterface.OnClickListener mListener = null;
	
	protected ConfirmationDialogFragment() {}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (getTargetFragment() == null){ //no target? we need one
			try {
				mListener = (DialogInterface.OnClickListener) activity;
			} catch (ClassCastException e){
				Log.e(CLASS_NAME, "No Target Fragment set, "
						+ "no Activity OnClickListener set. "
						+ "You need to do one these: " + e);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true); //retain.
		Bundle args = getArguments();
		if (args != null){
			positiveResource = args.getInt(KEY_POSITIVE_RES, positiveResource);
			negativeResource = args.getInt(KEY_NEGATIVE_RES, negativeResource);
			messageResource = args.getInt(KEY_MESSAGE_RES, messageResource);
			titleResource = args.getInt(KEY_TITLE_RES, titleResource);
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String msg = messageResource > 0 ? getString(messageResource) : "";
		
		return new AlertDialog.Builder(getActivity())
					.setTitle(titleResource)
					.setMessage(msg)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton(positiveResource, this)
					.setNegativeButton(negativeResource, this)
					.create();
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
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal class
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Builds a simple confirmation dialog.
	 * Note that responses are given via Activites implementing
	 * {@link DialogInterface#OnClickListener}
	 * unless {@link #setTargetFragment(Fragment, int)}  is et.
	 * @version 0.1.0-20140923 */
	public static class Builder {
		private DialogFragment frag = new ConfirmationDialogFragment();
		private Bundle args = new  Bundle(); 
		/** If set, the dialog responds via 
		 * <code>fragment</code> via 
	     * {@link Fragment#onActivityResult(int, int, android.content.Intent)}
       	 *  with <code>resultCode</code>. Results are either 
       	 *  {@link Activity#RESULT_OK} or {@link Activity#RESULT_CANCELED}.
       	 *  Otherwise the {@link DialogInterface#OnClickListener} is 
       	 *  called via the activity.
		 * @param fragment The fragment to respond to.
		 * @param requestCode The request code to give in onActivityResult
		 * @return Builder for chaining.
		 */
		public Builder setTargetFragment(Fragment fragment, int requestCode){
			frag.setTargetFragment(fragment, requestCode);
			return this;
		}
		
		/** Sets the message resource to use. If not given a blank string is shown. */
		public Builder setMessage(int stringId){
			args.putInt(KEY_MESSAGE_RES, stringId);
			return this;
		}
		
		/** Sets the confirm resource to use. 
		 * If not supplied <code>android.R.string.ok</code> is used. */
		public Builder setPositive(int stringId){
			args.putInt(KEY_POSITIVE_RES, stringId);
			return this;
		}
		
		/** Sets the negative resource to use. 
		 * If not supplied <code>android.R.string.cancel</code> is used. */
		public Builder setNegative(int stringId){
			args.putInt(KEY_NEGATIVE_RES, stringId);
			return this;
		}
		
		/** Sets the title resource to use. 
		 * If not supplied <code>android.R.string.dialog_alert_title</code> 
		 * is used.  */
		public Builder setTitle(int stringId){
			args.putInt(KEY_TITLE_RES, stringId);
			return this;
		}
		/** Builds fragment and returns it. */
		public DialogFragment create(){
			frag.setArguments(args);
			return frag;
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mListener != null && getTargetFragment() == null){
			//only if we have a listener and target fragment.
			mListener.onClick(dialog, which);
			return;
		}
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			getTargetFragment().onActivityResult(getTargetRequestCode(), 
					Activity.RESULT_OK, null);
			dialog.dismiss();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			getTargetFragment().onActivityResult(getTargetRequestCode(), 
					Activity.RESULT_CANCELED, null);
			dialog.dismiss();
			break;
		}		
	}
	
}
