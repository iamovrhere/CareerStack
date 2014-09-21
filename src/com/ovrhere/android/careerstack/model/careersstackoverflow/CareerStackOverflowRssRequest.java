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
package com.ovrhere.android.careerstack.model.careersstackoverflow;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.util.Log;

import com.ovrhere.android.careerstack.model.requests.AbstractSimpleHttpRequest;


/** Builds & makes requests to the careers.stackoverflow.com rss feed at:
 * <code>http://careers.stackoverflow.com/jobs/feed?...</code>
 * Uses {@link OnRequestEventListener} to provide feedback.
 * @author Jason J.
 * @version 0.1.0-20140921
 */
public class CareerStackOverflowRssRequest extends AbstractSimpleHttpRequest {
	/** The logtag for debugging. */
	final static private String LOGTAG = CareerStackOverflowRssRequest.class
			.getSimpleName();
	/*  Whether or not to output debugging logs. */ 
	//final static private boolean DEBUG = false;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The request to run/return in {@link #getPreparedRequest()} */
	protected String preparedRequest = "";
	
	protected CareerStackOverflowRssRequest() {}
		
	@Override
	protected String getPreparedRequest() {
		return this.preparedRequest;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Builder to help build requests according to the form:
	 * <code>http://careers.stackoverflow.com/jobs/feed
	 * ?searchTerm=java+android
	 * &location=US
	 * &range=100&distanceUnits=Miles
	 * &allowsremote=True
	 * &offersrelocation=True</code>
	 * @author Jason J.
	 * @version 0.1.0-20140914	 */
	public static class Builder {
		/** Prepared string; accepts integer. */
		final static private String DETAILED_EXCEPTION_NEGATIVE_VALUE = 
				"Cannot have a negative value (%d) .";
		/** The blank query stub. */
		final static private String QUERY_URL_STUB = 
				"http://careers.stackoverflow.com/jobs/feed";
		
		/** Query stub for tags (not to be confused with search terms. */
		final static private String QUERY_TAGS = "tags=";		
		/** The query stub for search terms. */
		final static private String QUERY_SEARCH_TERM = "searchTerm=";
		/** Query stub for location. */
		final static private String QUERY_LOCATION = "location=";
		/** Prepared Query stub for range, takes number followed by units. 
		 * (Use String.format()) */
		final static private String QUERY_PREPARDED_RANGE = 
				"range=%d&distanceUnits=%s";
		/** Fully prepared stub for allow remote. */
		final static private String QUERY_ALLOWS_REMOTE = 
				"allowsremote=True";
		/** Fully prepared stub for offersrelocation */
		final static private String QUERY_OFFER_RELOCATE = 
				"offersrelocation=True";
		
		/** The miles query value for #QUERY_DIST_UNITS. */
		final static private String VALUE_UNITS_MILES = "Miles";
		/** The kilometer query value for #QUERY_DIST_UNITS. */
		final static private String VALUE_UNITS_KM = "Km";
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// End constants
		////////////////////////////////////////////////////////////////////////////////////////////////
		
		private CareerStackOverflowRssRequest request = new CareerStackOverflowRssRequest();
		private String queryString = "";
		
		/** Sets the tag term portion of the request
		 * @param tags Cannot be null.
		 * @return The builder for chaining.		 */
		public Builder setTags(String tags){
			if (tags == null){
				throw new NullPointerException();
			}
			if (!tags.isEmpty()){ //why add, if empty?
				appendToQuery(QUERY_TAGS+urlEncode(tags));			
			}
			return this;
		}
		
		/** Sets the search term portion of the request
		 * @param searchTerms Cannot be null.
		 * @return The builder for chaining.		 */
		public Builder setSearchTerm(String searchTerms){
			if (searchTerms == null){
				throw new NullPointerException();
			}
			if (!searchTerms.isEmpty()){
				appendToQuery(QUERY_SEARCH_TERM+urlEncode(searchTerms));
			}
			return this;
		}
		
		/** Sets the location portion of the request
		 * @param searchTerms Cannot be null.
		 * @return The builder for chaining.		 */
		public Builder setLocation(String location){
			if (location == null){
				throw new NullPointerException();
			}
			if (!location.isEmpty()){
				appendToQuery(QUERY_LOCATION+urlEncode(location));
			}
			return this;
		}
		
		/** Sets the range & units portion of query.
		 * @param distance Cannot be negative.
		 * @param metric <code>true</code> for Km, <code>false</code> for Miles.
		 * @return The builder for chaining.		 */
		public Builder setDistance(int distance, boolean metric) {
			if (distance < 0){
				throw new IllegalArgumentException(
						String.format(DETAILED_EXCEPTION_NEGATIVE_VALUE, distance)
						);
			}
			appendToQuery(
					String.format(
							QUERY_PREPARDED_RANGE, 
							distance,
							(metric) ? VALUE_UNITS_KM : VALUE_UNITS_MILES)
						);
			return this;
		}
		
		/** Sets the allow remote portion of query. 
		 * @param offersRemote <code>true</code> to offer remote, 
		 * <code>false</code> to omit (default)
		 * @return The builder for chaining.		 */
		public Builder setRemote(boolean offersRemote){
			if (offersRemote){
				appendToQuery(QUERY_ALLOWS_REMOTE);
			}
			return this;
		}
		
		/** Sets the allow offer relocation portion of query. 
		 * @param offersRelocation <code>true</code> to offer, 
		 * <code>false</code> to omit (default)
		 * @return The builder for chaining.		 */
		public Builder setRelocation(boolean offersRelocation){
			if (offersRelocation){
				appendToQuery(QUERY_OFFER_RELOCATE);
			}
			return this;
		}
		/** Sets the timeout. 
		 * @param timeout Non-negative
		 * @return The builder for chaining.	 */
		public Builder setTimeout(int timeout){
			request.setRequestTimeout(timeout);
			return this;
		}
		
		/** Creates the prepared request ready to be sent.
		 * @return The {@link CareerStackOverflowRssRequest}		 */
		public CareerStackOverflowRssRequest create(){
			request.preparedRequest = QUERY_URL_STUB + queryString;			
			return request;
		}
		
		
		/////////////////////////////////////////////////////////////////////////////////////////////////
		/// Helper methods
		////////////////////////////////////////////////////////////////////////////////////////////////
		/** Appends the query to the query, ensuring either ? or & are used,
		 * accordingly.
		 * @param query The query text to add such as <code>x=1</code>	 */
		private void appendToQuery(String query){
			if (queryString.isEmpty()){
				queryString+="?"+query;
			} else {
				queryString+="&"+query;
			}
		}		
		/** Encodes the given string.
		 * @return The encodes string on success or default string on failure. */
		static private String urlEncode(String string) {
			try {
				return URLEncoder.encode(string, "utf-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(LOGTAG, "Should never happen: "+e);
			} 
			return string;
		}
	}
	
}
