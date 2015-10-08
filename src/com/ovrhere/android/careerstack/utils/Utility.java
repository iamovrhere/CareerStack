package com.ovrhere.android.careerstack.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.ovrhere.android.careerstack.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

/** A collection of helpful methods for this project. 
 * @version 0.2.0-20151008 */
public class Utility {
	
	/**
	 * Sets the status bar color for theme switching.
	 */
	@SuppressLint("NewApi") 
	public static void setStatusBarColor(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		    Window window = activity.getWindow();
		    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		    TypedValue typedValue = new TypedValue();
			activity.getTheme().resolveAttribute(R.attr.colorPrimaryDark, typedValue, true);
		    window.setStatusBarColor(typedValue.data);
		}
	}

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
