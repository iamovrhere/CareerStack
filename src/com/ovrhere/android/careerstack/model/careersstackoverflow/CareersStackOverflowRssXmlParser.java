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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.model.parsers.AbstractXmlParser;

/** The xml parser for the careers.stackoverflow.com rss feed.
 * Found at: <code>http://careers.stackoverflow.com/jobs/feed?...</code>.
 * <p>Remember that this is thread blocking; sleeping during {@link #pause()}
 * and yielding every {@value #YIELD_TAG_THROTTLE} tags (for throttling). </p>
 * @author Jason J.
 * @version 0.1.3-20141008
 * @see CareerStackOverflowRssRequest */
public class CareersStackOverflowRssXmlParser 
	extends AbstractXmlParser<List<CareerItem>> {	
	/** The tag for debugging purposes. */
	final static private String LOGTAG = CareersStackOverflowRssXmlParser.class
			.getSimpleName();
	/** Boolean for debugging. */
	final static private boolean DEBUG = false;
	/** The number of tags to yield after. */
	final static private int YIELD_TAG_THROTTLE = 250;
	
	/** The root tag of the rss feed. */
	final static private String TAG_RSS_ROOT = "rss";
	/** The outer tag of channel. */
	final static private String TAG_CHANNEL = "channel";
	/** Outer tag for career item. */
	final static private String TAG_CAREER_ITEM = "item";
	/** Tag for for job titles. Expects one per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_TITLE = "title";
	/** Tag for for job descriptions. Expects one per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_DESCRIPTION = "description";
	/** Tag for for job url. Expects one per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_URL = "link";
	/** Tag for for publish date. Expects one per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_PDATE = "pubDate";
	/** Tag for for updated date. Expects one per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_UDATE = "a10:updated";
	/** Tag for categories. Expect multiple per {@link #TAG_CAREER_ITEM} */
	final static private String TAG_CAREER_CATEGORY = "category";
	
	
	/** The at to find last of and replace, used in {@link #splitOnAt(String)}. */
	final static private String REPLACE_AT = " at ";
	
	/** The update date format used in {@link #parseDate(String)}. */ //2014-08-21T13:05:02Z
	final static private String UPDATE_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	/** The publish date format. */ //Thu, 21 Aug 2014 13:05:02 Z
	final static private String PUBLISH_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss 'Z'";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes parser.
	 * @throws XmlPullParserException  if parser or factory fails 
	 * to be created. */
	public CareersStackOverflowRssXmlParser() throws XmlPullParserException {
		super();
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Overridden methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	protected List<CareerItem> parseXmlToReturnData()
			throws XmlPullParserException, IOException {
		List<CareerItem> results = new ArrayList<CareerItem>();
		pullParser.nextTag();
		pullParser.require(XmlPullParser.START_TAG, null, TAG_RSS_ROOT);
		pullParser.nextTag();
		pullParser.require(XmlPullParser.START_TAG, null, TAG_CHANNEL);
		//Used to determine when to yield.
		int tagCountSinceYield = 0;		
		while (pullParser.next() != XmlPullParser.END_TAG) {
			if (pullParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
	        }
			String name = pullParser.getName();
			if (name.equalsIgnoreCase(TAG_CAREER_ITEM)){
				CareerItem item = parseCareerItem();
				if (item == null){
					if (DEBUG){
						Log.w(LOGTAG, "Parsed a null item? Skipping.");
					}
				} else{
					results.add(item);
				}
				checkPause();
			} else {
				skipTag(); //skip all other tags
			}
			//yields after tag throttle 
			if (tagCountSinceYield++ >= YIELD_TAG_THROTTLE){
				if (DEBUG){
					Log.d(LOGTAG, "Yielding");
				}
				tagCountSinceYield = 0; //reset
				Thread.yield();
			}
		}
		
		return results;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Parses an item block to produce a career item.
	 * @return The fully formed {@link CareerItem} or <code>null</code>.
	 * @throws XmlPullParserException re-thrown
	 * @throws IOException re-thrown
	 */
	private CareerItem parseCareerItem() throws XmlPullParserException, IOException{
		//required values
		String title = "";
		String description = "";
		String url = "";
		Date publishDate = null;
		Date updateDate = null; 
		List<String> categories = new ArrayList<String>();
		while (pullParser.next() != XmlPullParser.END_TAG) {
			if (pullParser.getEventType() != XmlPullParser.START_TAG) {
				continue;
	        }
			String name = pullParser.getName();
			if (name.equalsIgnoreCase(TAG_CAREER_TITLE)){
				title = readText();	
			} else if (name.equalsIgnoreCase(TAG_CAREER_DESCRIPTION)){
				//we need to remove the &gt; and &lt; to < and >
				description = readText(); 	
			} else if (name.equalsIgnoreCase(TAG_CAREER_URL)){
				url = readText();	
			} else if (name.equalsIgnoreCase(TAG_CAREER_CATEGORY)){
				categories.add(readText());
			} else if (name.equalsIgnoreCase(TAG_CAREER_PDATE)){
				publishDate = parsePubDateFromXml();
			} else if (name.equalsIgnoreCase(TAG_CAREER_UDATE)){
				updateDate = parseUpdateDateFromXml();
			} else {
				skipTag(); //skip all other tags at this level.
			}
		}
		return buildCareerItem(title, description, url, publishDate,
				updateDate, categories);
	}
	
	/** Parses the update date from xml, handling reading, parsing and try-catching.
	 * Tries using update form first, if failing tries publish form.
	 * @return The date or <code>null</code>.
	 * @throws XmlPullParserException re-thrown
	 * @throws IOException re-thrown
	 */
	private Date parseUpdateDateFromXml() throws IOException, XmlPullParserException{
		String datestamp = readText();		
		try {
			return parseDate(UPDATE_DATE_FORMAT, datestamp);
		} catch (ParseException e) {
			if (DEBUG){
				e.printStackTrace();
			}
			try {
				return parseDate(PUBLISH_DATE_FORMAT, datestamp);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		} 
		return null;
	}
	
	/** Parses the publish date from xml, handling reading, parsing and try-catching.
	 * Tries using publish form first, if failing tries update form.
	 * @return The date or <code>null</code>.
	 * @throws XmlPullParserException re-thrown
	 * @throws IOException re-thrown
	 */
	private Date parsePubDateFromXml() throws IOException, XmlPullParserException{
		String datestamp = readText();		
		try {
			return parseDate(PUBLISH_DATE_FORMAT, datestamp);
		} catch (ParseException e) {
			if (DEBUG){
				e.printStackTrace();
			}
			try {
				return parseDate(UPDATE_DATE_FORMAT, datestamp);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		} 
		return null;
	}
	
	/** Builds the career item in a try block. 
	 * @param title Cannot be empty.
	 * @param description Cannot be empty.
	 * @param url Cannot be empty. 
	 * @param publishDate Cannot be null.
	 * @param updateDate <b>Can</b> be null.
	 * @param categories Cannot be null.
	 * @return The career item on success or <code>null</code>
	 */
	private CareerItem buildCareerItem(String title, String description,
			String url, Date publishDate, Date updateDate,
			List<String> categories) {
		CareerItem item = null;
		String[] titlePieces = splitOnAt(title);
		String companyLocationEtc = "";
		if (titlePieces.length > 1){
			title = titlePieces[0];
			companyLocationEtc = titlePieces[titlePieces.length -1];
		}
		if (updateDate == null){
			updateDate = publishDate;
		}
		try {
			item = new CareerItem.Builder()
					.setDetails(title, companyLocationEtc, description, 
								url, publishDate)
					.setUpdateDate(updateDate)
					.setCategories(categories)
					.create();
		} catch (IllegalArgumentException e){
			if (DEBUG){
				Log.e(LOGTAG, "Likely empty string: " + e);
			}
		} catch (NullPointerException e){
			if (DEBUG){
				Log.e(LOGTAG, "Likely a Null Date: " + e);
			}
		}
		return item;
	}

	/** Splitting job title on the last instance of "at".
	 * @return Array with either the single element or 2 split on "at".
	 * Title, company */
	private String[] splitOnAt(String title) {
		int atIndex = title.lastIndexOf(REPLACE_AT);
		if (atIndex < 0){
			return new String[]{title};
		}
		return new String[]{
					title.substring(0, atIndex), //title 
					title.substring(atIndex + REPLACE_AT.length() - 1) 
				};
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Utility methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Parses the stackoverflow date given by the form: "2014-09-08T01:21:51Z"
	 * @param dateFormat Supports two forms:
	 * <ul><li>"2014-09-08T01:21:51Z"</li>
	 * <li>"Mon, 08 Sep 2014 01:21:51 Z"</li></ul>
	 * @param datestamp The timestamp to be parsed in the form 
	 * <code>yyyy-MM-dd'T'HH:mm:ss'Z'</code>
	 * @return The date on success or <code>null</code> on failure
	 * @throws ParseException When the date is not parsed correctly.
	 */
	static private Date parseDate(String dateFormat, String datestamp) 
			throws ParseException{
		SimpleDateFormat sdf = 
				new SimpleDateFormat(dateFormat, Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); //all times are in GMT/UTC
		return sdf.parse(datestamp);
	}
}
