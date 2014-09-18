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
package com.ovrhere.android.careerstack.ui.listeners;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/** To be implemented by activities. Allows the fragment to call back a 
 * request to the {@link Activity}. Note that the activity will have final say 
 * in the matter. 
 * @author Jason J.
 * @version 0.2.0-20140918
 */
public interface OnFragmentRequestListener {
	
	/** The request from the fragment to launch another fragment.
	 * Remember that if your <i>current</i> fragment is added to the backstack
	 * the views will not be recreated but Fragment WILL; 
	 * consider using {@link #onRequestHoldSavedState(String, Bundle)}
	 * @param fragment The fragment to launch
	 * @param tag The tag to advise giving to Activity
	 * @param backStack <code>true</code> to add to back stack,
	 * <code>false</code> to completely replace.
	 * @return <code>true</code> if Activity granted request, 
	 * <code>false</code> otherwise. 	 */
	public boolean onRequestNewFragment(Fragment fragment, String tag, 
			boolean backStack);
	
	/** The Fragment is requesting that the activity hide the actionbar.
	 * @param hide <code>true</code> to request it hidden, 
	 * <code>false</code> if to request it shown. 
	 * @return <code>true</code> if Activity granted request, 
	 * <code>false</code> otherwise. 	 */
	public boolean onRequestHideActionBar(boolean hide);
	
	/** Fragment is requesting that the Activity hold its saved state.
	 * @param tag The tag the fragment is giving its state
	 * @param savedState The state bundle 
	 * @return <code>true</code> if Activity granted request, 
	 * <code>false</code> otherwise.  
	 * @see #onRequestPopSavedState(String) */
	public boolean onRequestHoldSavedState(String tag, Bundle savedState);
	
	/**  Fragment is requesting for its saved state to be returned and REMOVED.
	 * @param tag The state tag to look for, return and REMOVE.
	 * @return The saved state or <code>null</code> if not found.	
	 * @see #onRequestHoldSavedState(String, Bundle) */
	public Bundle onRequestPopSavedState(String tag);
}
