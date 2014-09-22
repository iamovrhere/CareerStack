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

import com.ovrhere.android.careerstack.R;

import android.content.SharedPreferences;
import android.content.res.Resources;

/** Used abundantly throughout the application so figured do it once.
 * Only for use within CareerStack.
 * @author Jason J.
 * @version 0.1.0-20140922
 */
public class UnitCheck {
	
	/** Returns strings for either 
	 * <code>R.string.careerstack_formatString_distanceValue_km</code>
	 * or <code> R.string.careerstack_formatString_distanceValue_miles</code>
	 * based on bool value of pref 
	 * <code>R.string.careerstack_pref_KEY_USE_METRIC</code>
	 * @param prefs Preference handle
	 * @param r Resources handle
	 * @param formatArgs The arguments to insert into 
	 * {@link Resources#getString(int, Object...)}
	 * @return string values	 */
	static public String units(SharedPreferences prefs, Resources r, 
			Object... formatArgs){
		return r.getString(
				unitsId(prefs, r), 
				formatArgs);
	}
	
	/** Returns strings for either 
	 * <code>R.string.careerstack_formatString_distanceValue_km</code> or
	 * <code> R.string.careerstack_formatString_distanceValue_milesShort</code>
	 * based on bool value of pref 
	 * <code>R.string.careerstack_pref_KEY_USE_METRIC</code>
	 * @param prefs Preference handle
	 * @param r Resources handle
	 * @param formatArgs The arguments to insert into 
	 * {@link Resources#getString(int, Object...)}
	 * @return String values	 */
	static public String unitsShort(SharedPreferences prefs, Resources r,
			Object... formatArgs){
		return r.getString(
				unitsIdShort(prefs, r), 
				formatArgs);
	}
	
	/** Returns either 
	 * <code>R.string.careerstack_formatString_distanceValue_km</code>
	 * or <code> R.string.careerstack_formatString_distanceValue_miles</code>
	 * based on bool value of pref
	 *  <code>R.string.careerstack_pref_KEY_USE_METRIC</code>
	 * @param prefs Preference handle
	 * @param r Resources handle
	 * @return <code>R.string.careerstack_formatString_distanceValue_km</code>
	 * or <code> R.string.careerstack_formatString_distanceValue_miles</code>	 */
	static public int unitsId(SharedPreferences prefs, Resources r){
		if (useMetric(prefs, r)){
			//if using metric
			return R.string.careerstack_formatString_distanceValue_km;
		}
		return R.string.careerstack_formatString_distanceValue_miles;
	}
	
	
	/** Returns either 
	 * <code>R.string.careerstack_formatString_distanceValue_km</code>
	 * or <code> R.string.careerstack_formatString_distanceValue_milesShort</code>
	 * based on bool value of pref 
	 * <code>R.string.careerstack_pref_KEY_USE_METRIC</code>
	 * @param prefs Preference handle
	 * @param r Resources handle
	 * @return <code>R.string.careerstack_formatString_distanceValue_km</code>
	 * or <code> R.string.careerstack_formatString_distanceValue_milesShort</code>	 */
	static public int unitsIdShort(SharedPreferences prefs, Resources r){
		if (useMetric(prefs, r)){
			//if using metric
			return R.string.careerstack_formatString_distanceValue_km;
		}
		return R.string.careerstack_formatString_distanceValue_milesShort;
	}
	
	/** Returns <code>true</code> if set to metric, <code>false</code> if set
	 * to imperial.
	 * @param prefs
	 * @param r	 */
	public static boolean useMetric(SharedPreferences prefs, Resources r) {
		return prefs.getBoolean(
				r.getString(R.string.careerstack_pref_KEY_USE_MILES), 
						false) == false;
	}
}
