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
package com.ovrhere.android.careerstack.dao;


import java.util.Date;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/** Provides a data access object for individual career items.
 * @author Jason J.
 * @version 0.1.0-20140914
 */
public class CareerItem implements Parcelable{
	/** The job item's title. */
	protected String title = null;
	/** The job's company, location, etc. */
	protected String companyLocationEtc = null; 
	/** The job item's description. */
	protected String description = null;
	/** The job listing's hyper-link. */
	protected String url = null;
	/** The list of categories for the job item. */
	protected String[] categories = new String[0];
	/** The original publish date. UTC time. */
	protected Date publishDate = null;
	/** The last update. UTC time. */
	protected Date updateDate = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initialize. */
	protected CareerItem() {}
	
	@Override
	public String toString() {
		return super.toString() +
				String.format("[title: %s, url: %s]", title, url);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessors
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Returns the job title for the item. 
	 * @return Job title.	 */
	public String getTitle() {
		return title;
	}
	/** Returns the job company, location, etc (typically after at. 
	 * @return The company, location, and extra details (remote, relocation) */
	public String getCompanyLocationEtc() {
		return companyLocationEtc;
	}
	/** Returns the job description for the item.
	 * @return The job description.	 */
	public String getDescription() {
		return description;
	}
	/** Returns the job url for item. 
	 * @return Url.	 */
	public String getUrl() {
		return url;
	}
	/** Gets the list of set categories.
	 * @return The array of categories for this item. Default is empty.	 */
	public String[] getCategories() {
		return categories;
	}
	/** Return the publish date of listing in UTC/GMT time.
	 * @return The original publish date	 */
	public Date getPublishDate() {
		return publishDate;
	}
	/** Returns the last update to the listing in UTC/GMT time.
	 * @return The last update of listing.	 */
	public Date getUpdateDate() {
		return updateDate;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal class builder
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The builder for career items.
	 * @author Jason J.
	 * @version 0.1.1-20140922
	 */
	public static class Builder {
		final static private String DETAILED_EXCEPTION_BUILDER_IS_NOT_PREPARED = 
				"Insufficient details to build CareerItem.";
		final static private String DETAILED_EXCEPTION_TITLE_EMPTY = 
				"Title cannot be empty";
		final static private String DETAILED_EXCEPTION_DESCRIP_EMPTY = 
				"Description cannot be empty";
		final static private String DETAILED_EXCEPTION_URL_EMPTY = 
				"Url cannot be empty";
		final static private String DETAILED_EXCEPTION_UPDATE_BEFORE_PUBLISH = 
				"Cannot update before publishing";
		
		/** Whether we are ready to build or to throw. */
		private boolean readyToBuild = false;
		/** The item to build. */
		private CareerItem careerItem = new CareerItem();
		
		/** Required. Sets the basic details of a {@link CareerItem}. 
		 * @param title The job title. Cannot be empty.
		 * @param companyLocationEtc The company, location and extra details.
		 * Can be empty.
		 * @param description The job description. Cannot be empty.
		 * @param url The job link. Cannot be empty.
		 * @param publishDate The publish date. Cannot be null.
		 * @return This builder for chaining.
		 * @throws IllegalArgumentException If the arguments are invalid 
		 * @throws NullPointerException if arguments contain null		 */
		public Builder setDetails(String title, String companyLocationEtc, 
				String description, String url, Date publishDate){
			validateJobData(title, description, url, publishDate);
			careerItem.title = title.trim();
			careerItem.companyLocationEtc = companyLocationEtc.trim();
			careerItem.description = description.trim();
			careerItem.url = url;
			careerItem.publishDate = publishDate;
			
			readyToBuild = true;
			
			return this;
		}
		/** Sets the categories for this item.
		 * @param categories The list of categories to set. 	
		 * @return This builder for chaining.	 */
		public Builder setCategories(List<String> categories){
			careerItem.categories = new String[categories.size()];
			categories.toArray(careerItem.categories);
			return this;
		}
		/** Sets the categories for this item.
		 * @param categories The list of categories to set. Cannot be null.
		 * @return This builder for chaining.  
		 * @throws NullPointerException if arguments contain null
		 * */
		public Builder setCategories(String[] categories){
			if (categories == null){
				throw new NullPointerException();
			}
			careerItem.categories = categories;
			return this;
		}
		
		/** Sets the last update date for this item. If not set
		 * assumes the value of <code>publishDate</code>
		 * @param date The new update date. Cannot be before publish date.
		 * @return This builder for chaining.	
		 * @throws NullPointerException if arguments contain null */
		public Builder setUpdateDate(Date date){
			if (date == null){
				throw new NullPointerException();
			}
			careerItem.updateDate = date;
			return this;
		}
		
		/** Builds the final object. Can be called multiple times.
		 * @return The prepared {@link CareerItem} from this builder. 
		 * @throws IllegalStateException If the method 
		 * {@link #setDetails(String, String, String, Date)} has not been called
		 * or if the <code>updateDate</code> is before the <code>publishDate</code>. */
		public CareerItem create(){
			if (!readyToBuild){
				throw new IllegalStateException(DETAILED_EXCEPTION_BUILDER_IS_NOT_PREPARED);
			}
			if (careerItem.updateDate == null){
				careerItem.updateDate = careerItem.publishDate;
			} else if (careerItem.updateDate.getTime() > 
					careerItem.publishDate.getTime() ){
				throw new IllegalStateException(DETAILED_EXCEPTION_UPDATE_BEFORE_PUBLISH);
			}
			return careerItem;
		}
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// Utility methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		/** Validates job data throwing exceptions if failing.
		 * @param jobTitle Non-empty
		 * @param jobDescription Non-empty
		 * @param jobUrl Non-empty
		 * @param publishDate Non-null
		 * @throws IllegalArgumentException If the arguments are invalid 
		 * @throws NullPointerException if arguments contain null
		 */
		static private void validateJobData(String jobTitle, String jobDescription, 
				String jobUrl, Date publishDate){
			if (jobTitle.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_TITLE_EMPTY);
			}
			if (jobDescription.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_DESCRIP_EMPTY);				
			}
			if (jobUrl.isEmpty()){
				throw new IllegalArgumentException(DETAILED_EXCEPTION_URL_EMPTY);
			}
			if (publishDate == null){
				throw new NullPointerException();
			}
		}
		
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Parcelable details
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		//writing strings
		final int SIZE = 3 + categories.length;
		String[] strings = new String[SIZE];
		//add single strings
		strings[0] = title;
		strings[1] = description;
		strings[2] = url;
		//fill categories
		for(int index = 3; index < SIZE; index++){
			strings[index] = categories[index - 3];
		}
		
		out.writeStringArray(strings);
		
		//writing dates/longs
		long[] dates = new long[2];
		dates[0] = publishDate.getTime();
		dates[1] = updateDate.getTime();
		
		out.writeLongArray(dates);
		
	}
	/** For use with {@link #CREATOR}. 
	 * @see #writeToParcel(Parcel, int)*/
	private CareerItem (Parcel in){
		//unpack strings
		String[] strings = in.createStringArray();
		final int SIZE = strings.length;
		title = strings[0];
		description = strings[1];
		url = strings[2];		
		categories = new String[SIZE - 3];
		for (int index = 3; index < SIZE; index++) {
			categories[index - 3 ] = strings[index];
		}
		
		//unpack dates/longs
		long[] dates = new long[2];
		publishDate = new Date(dates[0]);
		updateDate = new Date(dates[1]);
	}
	
	/** Creator used with parcelable interface. */
	public static final Parcelable.Creator<CareerItem> CREATOR
		    	= new Parcelable.Creator<CareerItem>() {
		@Override
		public CareerItem createFromParcel(Parcel in) {
		    return new CareerItem(in);
		}
		@Override
		public CareerItem[] newArray(int size) {
		    	return new CareerItem[size];
			}
		};

}
