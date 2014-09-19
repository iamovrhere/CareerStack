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
package com.ovrhere.android.careerstack.utils;

import android.content.Context;
import android.widget.Toast;

/** A toast manager to simplify outputting toasts. 
 * never store statically.
 * @author Jason J.
 * @version 0.1.0-20140918 */
public class ToastManager {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = ToastManager.class.getSimpleName();
	
	/** The current context. (We are keeping a toast anyway, we cannot leak
	 * any MORE.	 */
	final private Context mcontext;
	
	/** The current toast in play. */
	private Toast currentToast = null;	
	
	/** Builds toast manager. Do not keep outside context.
	 * @param context The current context	 */
	public ToastManager(Context context) {
		this.mcontext = context.getApplicationContext();
	}
	
	
	/** Toast for duration {@link Toast#LENGTH_LONG}. If currently toasting
	 * cancel toast and retoast.
	 * @param toastString The string to toast.	 */
	public void toastLong(String toastString){
		toast(toastString, Toast.LENGTH_LONG);
	}
	/** Toast for duration {@link Toast#LENGTH_SHORT}.
	 * @param toastString The string to toast.	 */
	public void toastShort(String toastString){
		toast(toastString, Toast.LENGTH_SHORT);
	}
	/** Returns whether the current toast is visible. 
	 * @return <code>true</code> if visible, <code>false</code> otherwise. 
	 */
	public boolean isToastVisible(){
		return currentToast != null && currentToast.getView().isShown();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Toasts and cancels any pre-existing toasts. 
	 * @param toastString The toast string to toast.
	 * @param duration The duration to forward	 */
	private void toast(String toastString, int duration) {
		if (isToastVisible()){
			//We have a visible toast, adjust and show			
			currentToast.setText(toastString);
			currentToast.setDuration(duration);
			currentToast.show();
			return;
		}	
		currentToast = Toast.makeText(mcontext, toastString, duration);		
		currentToast.show();
	}
}
