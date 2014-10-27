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
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.gms.ads.AdView;
import com.ovrhere.android.careerstack.R;

/** Uses decorator pattern to present the list results wrapped with ads dispersed 
 * between.
 * @author Jason J.
 * @version 0.1.1-20141027
 */
public class AdViewListAdapter extends BaseAdapter {
		/** The activity used for building ads. */
	final private Activity activity;
	/** The actual list being shown. */
	final private BaseAdapter delegate;
	
	/** The minimum size required before showing ads. */
	final private int minimumSize;
	/** The position of the first ad. */
	final private int firstAdPosition;
	/** The position of ads throughout. */
	final private int adFrequency;
	
	/** Whether or not the list is currently debugging. */
	private boolean debugging = false;
	
	/** Builds list view wrapper for showing ads. 
	 * @param activity The activity to build ads with
	 * @param delegate The results to actually display */
	public AdViewListAdapter(Activity activity, BaseAdapter delegate) {
		this.activity = activity;
		this.delegate = delegate;
		
		delegate.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				notifyDataSetChanged();
			}
			@Override
			public void onInvalidated() {
				notifyDataSetInvalidated();
			}
		});
		
		this.minimumSize = activity.getResources()
				.getInteger(R.integer.careerstack_listview_ad_minSize);		
		this.firstAdPosition = activity.getResources()
				.getInteger(R.integer.careerstack_listview_ad_start);
		this.adFrequency = activity.getResources()
				.getInteger(R.integer.careerstack_listview_ad_frequency);
		
		this.debugging = activity.getResources()
				.getBoolean(R.bool.careerstack_listview_ad_debugging);
		
		if (minimumSize <= 0 || firstAdPosition <= 0 || adFrequency <= 0){
			throw new IllegalArgumentException("Cannot have resource integers <=0");
		}
	}
		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Overridden methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
		
	@Override
	public int getCount() {
		return delegate.getCount() + getAdTotal();
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public Object getItem(int position) {
		if (isItemAnAd(position)) {
	      return null;
	    }
	    return delegate.getItem(getOffsetPosition(position));
	}
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (isItemAnAd(position)){
			if (convertView instanceof AdView){
				//reuse ads
				return convertView;
			}
			//else, create ads
			
			AdView adview = null;
			if (debugging){ //when debugging, provide debugging. 
				adview = AdViewUtil.createTestAdView(activity);
			} else {
				adview = AdViewUtil.createAdView(activity);
			}
			return adview;									
		} 
		//else
		
		return delegate.getView(getOffsetPosition(position), convertView, parent);
	}
	
	
	@Override
	public int getViewTypeCount() {
		return delegate.getViewTypeCount() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (isItemAnAd(position)) {
			return delegate.getViewTypeCount();
    	}
		
    	return delegate.getItemViewType(getOffsetPosition(position));    	
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {	
		return (!isItemAnAd(position)) && 
				delegate.isEnabled(getOffsetPosition(position));
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Returns if the current row position is an ad or not. 
	 * @param position The position to check
	 * @return <code>true</code> if ad, false otherwise
	 */
	private boolean isItemAnAd(int position){
		if (getAdTotal() <= 0){
			return false;
		}
		//if the first ad position OR
		return position == firstAdPosition ||
				//if the ad is an ad position
				(position != 0 && position % adFrequency == 0);
	}
	
	/** Gives the full total of ads. */
	private int getAdTotal(){
		int itemCount = delegate.getCount();
		int adTotal = 0;
		if (itemCount < minimumSize){
			return adTotal;
		}
		adTotal += 1; //add an add for the first position
		adTotal += itemCount/adFrequency; //add each ad for each position.
		return adTotal;
	}
	
	/** Returns the number of ads shown before the given position. 
	 * @param position The position to check at
	 * @return the number of ads displayed thus far
	 */
	private int adCount(int position){
		if (position < minimumSize){
			return position/adFrequency;
		}
		return position/adFrequency + 1;
	}
	
	/** Returns the offset position if any. */
	private int getOffsetPosition(int position){
		if (delegate.getCount() < minimumSize){
			return position;
		}
		return position - adCount(position);
	}
		
	
}
