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

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.ovrhere.android.careerstack.R;

/** Simple util used by this project.
 * @author Jason J.
 * @version 0.1.0-20141031
 */
public class TabletUtil {
	
	/** Checks that the device is currently in tablet mode based on:
	 * <ol><li>Screen size</li><li>Ads enabled</li><li>Tablet mode enabled</li></ol>
	 * @return <code>true</code> if currently in tablet mode, <code>false</code>
	 * otherwise.	 */
	static public boolean inTabletMode(Resources r, SharedPreferences prefs){
		return r.getBoolean(R.bool.careerstack_in_tablet_mode) &&
		prefs.getBoolean(r.getString(R.string.careerstack_pref_KEY_ADS_AND_FEATURES), false) &&
		prefs.getBoolean(r.getString(R.string.careerstack_pref_KEY_USE_TABLET_LAYOUT), false);
		
	}
}
