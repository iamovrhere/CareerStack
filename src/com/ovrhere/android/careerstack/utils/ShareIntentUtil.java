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

import java.util.ArrayList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;

/** Collections of methods for sharing on Android. 
 * Provides means to share appropriately via twitter, facebook & generic.
 * Includes a fallback for facebook (if not found) to launch via browser intent.
 * @author Jason J.
 * @version 0.1.0-20140922
 */
public class ShareIntentUtil {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = ShareIntentUtil.class
			.getSimpleName();

	/** The character limit of twitter to warn with. */
	final static private int TWITTER_LIMIT = 140;
	/** Warning message for twitter length; accepts 1 int. */
	final static private String WARNING_TWITTER_LENGTH = 
			"Twitter has a " + TWITTER_LIMIT + " limit; %d characters found - "
					+ "Clipping may occur. ";
	
	/** The standard facebook package name. */
	final static private String FACEBOOK_PACKAGE = "com.facebook.katana";	
	/** Formatted string for facebook share url; expects 1 string. */
	final static private String FACEBOOK_SHARE_URL = 
			"https://www.facebook.com/sharer/sharer.php?u=%s";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The message to share on twitter, clipped to fit. */
	final private String twitterMsg;
	/** General message given to most sites. */
	final private String genericMsg;
	/** The link to share. */
	final private String shareUrl;
	/** The title of the share intent. */
	final private String shareTitle;
	
	/** Creates helper with share link +  message.
	 * Not that messages > {@value #TWITTER_LIMIT} chars will be truncated.
	 * @param title The title of the intent (such as "Share")
	 * @param shareLink The link to share
	 * @param genericMsg The message to accompany it 
	 * @param twitterMsg The message to send for twitter; may be truncated	 */
	public ShareIntentUtil(String title, String shareLink, String genericMsg, 
			String twitterMsg) {
		this.shareTitle = title;
		this.shareUrl = shareLink;
		this.genericMsg = genericMsg;
		if (twitterMsg.length() > TWITTER_LIMIT){
			Log.w(LOGTAG,
					String.format(WARNING_TWITTER_LENGTH, twitterMsg.length())
					);
			twitterMsg = twitterMsg.substring(0, TWITTER_LIMIT);
		}
		this.twitterMsg = twitterMsg;
	}
	
	/** Creates helper with share link + (twitter compatible) message.
	 * Not that messages > {@value #TWITTER_LIMIT} chars will be truncated.
	 * @param title The title of the intent (such as "Share")
	 * @param shareLink The link to share
	 * @param msg The message to accompany it (can be empty); may be truncated.	 */
	public ShareIntentUtil(String title, String shareLink, String msg) {
		this(title, shareLink, msg, msg);
	}
	
	
	@Override
	public String toString() {
		return super.toString() + 
				String.format("[title: %s, url: %s, twitter: %s, generic, %s]", 
						shareTitle, shareUrl, twitterMsg, genericMsg);
	}
	
	
	/*
	 * Reference:
	 * -http://stackoverflow.com/questions/14450867/branching-the-android-share-intent-extras-depending-on-which-method-they-choose
	 * -http://stackoverflow.com/questions/8771333/android-share-intent-for-facebook-share-text-and-link/22994833#22994833
	 */
	
	/** Builds and launches share intent
	 * @param context The context to launch/build with.
	 * @return <code>true</code> on successful launch, <code>false</code> on 
	 * no activities found/other exception encountered
	 */
	public boolean launchShare(Context context){
	    List<Intent> targetShareIntents = new ArrayList<Intent>();
	    
	    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
	    sharingIntent.setType("text/plain");

	    PackageManager pm = context.getPackageManager();
	    List<ResolveInfo> activityList = 
	    		pm.queryIntentActivities(sharingIntent, 0);
	    try {
		    boolean facebookAdded = false;
		    for(final ResolveInfo app : activityList) {
		         String packageName = app.activityInfo.packageName;
		         
		         Intent shareIntent = new Intent(Intent.ACTION_SEND);	         
		         shareIntent.setType("text/plain");
		         shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
		         
		         if(FACEBOOK_PACKAGE.equals(packageName)){
		             shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
		             facebookAdded = true;
		         } else if (packageName.contains(".twitter")){
		             shareIntent.putExtra(Intent.EXTRA_TEXT, twitterMsg);
		         } else {
		        	 shareIntent.putExtra(Intent.EXTRA_TEXT, genericMsg);
		         }
		         shareIntent.setPackage(packageName);
		         targetShareIntents.add(shareIntent);
		    }
		    if (!facebookAdded){
		    	targetShareIntents.add(getFacebookUrlShare());
		    }
	
		    Intent chooserIntent = Intent.createChooser(
		    		targetShareIntents.remove(0), shareTitle);
	
		    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, 
		    		targetShareIntents.toArray(new Parcelable[]{}));
		    context.startActivity(chooserIntent);
		    return true;
		}catch (ActivityNotFoundException e) {
	    	Log.e(LOGTAG, "No activities found");
			return false;
		}
	}
	
	/** Gets the share by url facebook intent. */
	private Intent getFacebookUrlShare(){
        Intent i = new Intent(Intent.ACTION_VIEW, 
        			Uri.parse(String.format(FACEBOOK_SHARE_URL, shareUrl)));
        i.putExtra(Intent.EXTRA_SUBJECT, "Facebook");
        return i;
	}

}
