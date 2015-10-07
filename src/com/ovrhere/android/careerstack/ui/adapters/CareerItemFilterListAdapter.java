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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.utils.Utility;

/** The career item filter list adapter.
 * <p><b>Filtering will be done at a later date.</b></p>
 * @author Jason J.
 * @version 0.5.1-20151006
 */
@Deprecated
public class CareerItemFilterListAdapter extends BaseAdapter implements
		Filterable {
	/* For debugging purposes. 
	//final static private String LOGTAG = CareerItemFilterListAdapter.class
			.getSimpleName(); */
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The current inflater in use. */
	private LayoutInflater inflater = null;
	/** The local context. */
	private Context mContext = null;
	
	/** The list of all career items. */
	private List<CareerItem> careerItems = new ArrayList<CareerItem>();
	/**The layout resource, default is 
	 * <code>android.R.layout.simple_list_item_2</code> */
	private int layoutResource = R.layout.row_my_simple_list_item; 
	
	/** The first row message. */
	private String firstRowMessage = "";	
	/** The empty message. */
	private String emptyMessage = "";
	/** The sub text message. */
	private String subTitleMessage = "";
	
	/** Builds the adapter using the layout:
	 * <code>android.R.layout.simple_list_item_2</code>
	 * @param context The current context. 	 */
	public CareerItemFilterListAdapter(Context context) {
		this.inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mContext = context;
		this.emptyMessage = 
				mContext.getString(R.string.careerstack_careerlist_noResults);
		
	}
	
	/** Resets the career items to be new set. 
	 * @param careerItems The career items to reset to.
	 */
	public void setCareerItems(List<CareerItem> careerItems) {
		this.careerItems.clear();
		this.careerItems.addAll(careerItems);
		notifyDataSetChanged();
	}
	
	/** Sets the search terms to give in the first row	 */
	public void setSearchTerms(String keyword, String location, 
			boolean remote, boolean relocation) {
		emptyMessage = "";
		firstRowMessage = "";
		subTitleMessage = "";
		
		//set keyword
		if (keyword.isEmpty()){
			emptyMessage = 
					mContext.getString(R.string.careerstack_careerlist_noResults);
			firstRowMessage = 
					mContext.getString(R.string.careerstack_careerlist_matchingResults);
		} else {
			emptyMessage = 
					mContext.getString(R.string.careerstack_careerlist_noResults_stub,
							keyword);
			firstRowMessage = 
					mContext.getString(R.string.careerstack_careerlist_matchingResults_stub,
							keyword);
		}
		
		String nearLocation = "";
		//set location		
		if (!location.isEmpty()){			
			nearLocation = mContext.getString(R.string.careerstack_careerlist_resultsNear,
					location);
		}
		emptyMessage += nearLocation;
		firstRowMessage += nearLocation;
		
		
		//set subtitle
		if (remote){
			subTitleMessage = 
					mContext.getString(R.string.careerstack_main_check_workRemotely);
		}
		if (relocation){
			if (!subTitleMessage.isEmpty()) {
				subTitleMessage += " + ";
			}
			subTitleMessage += 
					mContext.getString(R.string.careerstack_main_check_offersRelocation);
		}
		if (subTitleMessage.isEmpty() == false){
			subTitleMessage = "( "+subTitleMessage+" )";
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Overridden functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean isEmpty() {
		return careerItems.size() <= 0;
	}
		
	/**
	 * {@inheritDoc}
	 * @return The count of items + 1 (empty is 1).
	 * @see #isEmpty()
	 */
	@Override
	public int getCount() {
		return careerItems.size() + 1;
	}

	/** 
	 * {@inheritDoc}
	 * @return The data at the specified position or <code>null</code> when empty.
	 */
	@Override
	public CareerItem getItem(int position) {
		return position > 0 ? careerItems.get(position - 1) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null){
			convertView = inflater.inflate(layoutResource, parent, false);
			
			holder = new Holder();
			holder.jobTitle = (TextView) 
					convertView.findViewById(android.R.id.text1);
			holder.companyLocationEtc_andDate = (TextView) 
					convertView.findViewById(android.R.id.text2);
			
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		if (position == 0){
			if (careerItems.size() <= 0){
				//if no results, return no results message.
				holder.jobTitle.setText(emptyMessage);
			} else {
				//give top row message of : n Matching x
				holder.jobTitle.setText(careerItems.size() +" " + firstRowMessage);
			}			
			holder.jobTitle.setGravity(Gravity.CENTER);			
			holder.jobTitle.setTypeface(null, Typeface.ITALIC);
			
			if (subTitleMessage.isEmpty()){
				//if no message, why show it?
				holder.companyLocationEtc_andDate.setVisibility(View.GONE);
			} else {
				holder.companyLocationEtc_andDate.setVisibility(View.VISIBLE);
				holder.companyLocationEtc_andDate.setText(subTitleMessage); 
				holder.companyLocationEtc_andDate.setGravity(Gravity.CENTER);
				holder.companyLocationEtc_andDate.setTypeface(null, Typeface.ITALIC);
			}			
			
			return convertView;
			
		} else {
			position--; //reduce index to match
			holder.jobTitle.setGravity(Gravity.LEFT); //TODO use for Gravity.START in api 14+
			holder.jobTitle.setTypeface(null, Typeface.BOLD);
			
			holder.companyLocationEtc_andDate.setGravity(Gravity.LEFT);
			holder.companyLocationEtc_andDate.setVisibility(View.VISIBLE);	
			holder.companyLocationEtc_andDate.setTypeface(null, Typeface.NORMAL);
		}
		
		CareerItem item = careerItems.get(position);
		holder.jobTitle.setText(item.getTitle());
		holder.companyLocationEtc_andDate.setText(
				item.getCompanyLocationEtc() + " - " + 
				getRelativeTime(convertView.getContext(),item.getUpdateDate())
				);
		
		return convertView;
	}
	
	/** Holder pattern. */
	private static class Holder {
		public TextView jobTitle = null;
		public TextView companyLocationEtc_andDate = null;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Converts date to relative time. */
	private static String getRelativeTime(Context context, Date date){
		return Utility.getRelativeTime(context, date);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Not yet implemented. */
	@Override
	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

}
