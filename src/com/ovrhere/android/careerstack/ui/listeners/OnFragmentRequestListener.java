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
import android.support.v4.app.Fragment;

/** To be implemented by activities. Allows the fragment to call back a 
 * request to the {@link Activity}. Note that the activity will have final say 
 * in the matter. 
 * @author Jason J.
 * @version 0.1.0-20140916
 */
public interface OnFragmentRequestListener {
	
	/** The request from the fragment to launch another fragment.
	 * @param fragment The fragment to launch
	 * @param tag The tag to advise giving to Activity
	 * @param backStack <code>true</code> to add to back stack,
	 * <code>false</code> to completely replace.
	 * @return <code>true</code> if Activity granted request, 
	 * <code>false</code> otherwise. 	 */
	public boolean onRequestNewFragment(Fragment fragment, String tag, 
			boolean backStack);
	
	/** The Fragment is requesting that Main hide the actionbar.
	 * @param hide <code>true</code> to request it hidden, 
	 * <code>false</code> if to request it shown. 
	 * @return <code>true</code> if Activity granted request, 
	 * <code>false</code> otherwise. 	 */
	public boolean onRequestHideActionBar(boolean hide);
}
