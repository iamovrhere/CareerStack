package com.ovrhere.android.careerstack;


import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.MainActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Restarts the main activity when requested. Requires manifest declaration:
 * <pre>
 *&lt;receiver android:name="com.ovrhere.android.careerstack.RestartReceiver" android:exported="false">
	&lt;intent-filter>
		&lt;action android:name={@value #ACTION_RESTART_APP}/>
	&lt;/intent-filter>
&lt;/receiver>
 * </pre> 
 * @author Jason J.
 * @version 1.0.0-20150828
 */
public class RestartReceiver extends BroadcastReceiver {
	
	/** Clears ALL settings and restarts the app. */
	//Be SURE that this is in the manifest & matches or it will NOT work.
	public static final String ACTION_RESTART_APP = "com.ovrhere.android.careerstack.RestartReceiver.ACTION_RESTART_APP";
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	/*
    	 * WARNING: Beware of couple & loops here.
    	 */
    	final String action = intent.getAction();
        if (ACTION_RESTART_APP.equals(action)) {        	
        	Intent mainIntent = new Intent(context, MainActivity.class);
    		mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    		context.startActivity(mainIntent);
        }
    }
    
    /** Initializes and sends a restart broadcast. */
    public static void sendBroadcast(Activity activity) {
		Intent intent = new Intent(RestartReceiver.ACTION_RESTART_APP);
		intent.setAction(RestartReceiver.ACTION_RESTART_APP);
		activity.finish();
		activity.sendBroadcast(intent);
    }
}
