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
package com.ovrhere.android.careerstack.ui.adapters;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.utils.TabletUtil;

/** Creates ad views for the list. 
 * @author Jason J.
 * @version 0.1.1-20141031
 */
class AdViewUtil {
	
	private AdViewUtil() {}
	
	/** Creates ad view to view in list. */
	public static AdView createAdView(Activity activity){
		AdView adView = createAdViewBase(activity);

        adView.loadAd(createAdRequest(activity, false));
        return adView;
	}

	
	/** Creates ad view to view in list for testing. */
	public static AdView createTestAdView(Activity activity){
		AdView adView = createAdViewBase(activity);

        adView.loadAd(createAdRequest(activity, true));
        return adView;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Creates the base for the adview, with correct sizing. */
	private static AdView createAdViewBase(Activity activity) {
		AdView adView = new AdView(activity);
		
		//cannot use smart banner for varying widths
		adView.setAdUnitId(activity.getResources()
				.getString(R.string.careerstack_ad_unit_id));
		
		final boolean inTabletMode = TabletUtil.inTabletMode(activity);
		
		//use smart banner when possible, otherwise use BANNER
		//NOTE: will only work if there's 320dp to display ad in
		adView.setAdSize(inTabletMode ? AdSize.BANNER : AdSize.SMART_BANNER);  
		return adView;
	}
	
	/** Creates an adrequest. If set to test mode it will add the test devices. */
	private static AdRequest createAdRequest(Context context, boolean testMode) {
	  AdRequest.Builder adRequest = new AdRequest.Builder();
	  if (testMode) {
		  adRequest.addTestDevice(context.getResources()
				  .getString(R.string.careerstack_test_devices));
	  }
	  return adRequest.build();
  	}

}
