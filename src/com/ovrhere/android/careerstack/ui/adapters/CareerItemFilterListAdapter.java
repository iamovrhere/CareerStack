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
import android.text.format.DateUtils;
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

/** The career item filter list adapter.
 * <p><b>Filtering will be done at a later date.</b></p>
 * @author Jason J.
 * @version 0.2.0-20140916
 */
public class CareerItemFilterListAdapter extends BaseAdapter implements
		Filterable {
	/* For debugging purposes. 
	//final static private String LOGTAG = CareerItemFilterListAdapter.class
			.getSimpleName(); */
	/** Approx. Month in millis. */
	final static private long MONTH_IN_MILLIS = DateUtils.WEEK_IN_MILLIS * 5;
	/** The date format in {@link #getRelativeTime(Context, Date)}. */
	@SuppressWarnings("unused")
	final static private String DATE_FORMAT = "yyyy/MM/dd";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The current inflater in use. */
	private LayoutInflater inflater = null;
	
	/** The list of all career items. */
	private List<CareerItem> careerItems = new ArrayList<CareerItem>();
	/**The layout resource, default is 
	 * <code>android.R.layout.simple_list_item_2</code> */
	private int layoutResource = android.R.layout.simple_list_item_2; 
	
	/** Builds the adapter using the layout:
	 * <code>android.R.layout.simple_list_item_2</code>
	 * @param context The current context. 	 */
	public CareerItemFilterListAdapter(Context context) {
		this.inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}
	/** Resets the career items to be new set. 
	 * @param careerItems The career items to reset to.
	 */
	public void setCareerItems(List<CareerItem> careerItems) {
		this.careerItems.clear();
		this.careerItems.addAll(careerItems);
		notifyDataSetChanged();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Overriden functions
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean isEmpty() {
		return careerItems.size() <= 0;
	}
		
	/**
	 * {@inheritDoc}
	 * @return The count of items or 1 when empty
	 * @see #isEmpty()
	 */
	@Override
	public int getCount() {
		return careerItems.size() > 0 ? careerItems.size()  : 1 ;
	}

	/** 
	 * {@inheritDoc}
	 * @return The data at the specified position or <code>null</code> when empty.
	 */
	@Override
	public CareerItem getItem(int position) {
		return careerItems.size() > 0 ? careerItems.get(position) : null;
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
		//if no results, return no results message.
		if (careerItems.size() <= 0){
			holder.jobTitle.setText(R.string.careerstack_careerlist_noResults);
			holder.jobTitle.setGravity(Gravity.CENTER);
			return convertView;
		} else {
			holder.jobTitle.setGravity(Gravity.LEFT);
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
	static private String getRelativeTime(Context context, Date date){
		long millis = date.getTime();
		//We should never be exceeding a month anyway
		/*if (MONTH_IN_MILLIS < new Date().getTime() - millis){
			return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(millis);
		}*/
		String time = DateUtils.getRelativeDateTimeString(context, millis, 
				DateUtils.MINUTE_IN_MILLIS, MONTH_IN_MILLIS, 
				DateUtils.FORMAT_NO_MONTH_DAY).toString();
		int commaIndex = time.lastIndexOf(",");
		if (commaIndex > 0){
			return time.substring(0, commaIndex);
		} return time;
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
