package com.ovrhere.android.careerstack.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.text.format.DateUtils;

/** A collection of helpful methods for this project. 
 * @version 0.1.0-20151006 */
public class Utility {
	

	/** The output date format string to use. */
	/* We could use DateUtils like in CareerItemFilterListAdapter, but I favour
	 * this format above all.	 */
	static private final String ISO8601_DATE_FORMAT = "yyyy-MM-dd HH:mm";

	/** Converts date to relative time. */
	public static String getRelativeTime(Context context, Date date){
		long millis = date.getTime();
		String time = DateUtils.getRelativeDateTimeString(context, millis, 
						DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS, 
						0).toString();
		int commaIndex = time.lastIndexOf(",");
		if (commaIndex > 0){
			return time.substring(0, commaIndex);
		} return time;
	}
	
	/** Returns the date in ISO 8601 format. See: 
	 * https://en.wikipedia.org/wiki/ISO_8601 */
	public static String getPreciseDate(Date date) {
		SimpleDateFormat dateFormat = 
				new SimpleDateFormat(ISO8601_DATE_FORMAT, Locale.US);
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}
}
