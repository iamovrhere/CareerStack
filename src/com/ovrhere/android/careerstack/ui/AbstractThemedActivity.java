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
package com.ovrhere.android.careerstack.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.ConfirmationDialogFragment;
import com.ovrhere.android.careerstack.utils.Utility;

/** The main entry point into the application.
 * @author Jason J.
 * @version 0.1.1-20151008
 */
abstract public class AbstractThemedActivity extends AppCompatActivity {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = AbstractThemedActivity.class.getSimpleName();
	

	/** Extra Key. The theme intent value. Int. */
	final static public String KEY_THEME_INTENT = 
			CLASS_NAME + ".KEY_THEME_INTENT";
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The current theme. Default is -1. */
	private int currThemeId = -1;
	
	/** The current shared preference. */
	private SharedPreferences prefs = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setThemeByIntent();
		super.onCreate(savedInstanceState);
		
		if (PreferenceUtils.isFirstRun(this)){
			PreferenceUtils.setToDefault(this);
		}
		prefs = PreferenceUtils.getPreferences(this);
		//checks and, if necessary, restarts activity for theme
		checkThemePref();
	}

	


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
			
		case R.id.action_toggleTheme:
			if (quickSwitchTheme()){
				toggleDayNightMode();
			} else {
				showChangeThemeDialog();
			}
			return true;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Retrieves the quick switch theme pref. */
	protected boolean quickSwitchTheme() {
		return prefs != null &&
			prefs.getBoolean(
					getString(R.string.careerstack_pref_KEY_QUICK_THEME_SWITCH), 
					false);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Theme Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////

	/* Theme flow explained:
	 * Activity starts and them preferences are checked:
	 * A) 1. If using quick switch mode, set theme. Done.
	 * 
	 * B) 1. If NOT using quick switch, set theme intent and restart
	 *    2. If theme intent is set, set theme. Done.
	 *    (WARNING! Beware of loops from unset themes)
	 * 
	 * ---
	 * 
	 * Changing themes:
	 * A) 1. If using quick switch mode: toggle theme 
	 *      i.  call setTheme()
	 *      ii. set status bar color (Lollipop and up)
	 *    2. Call recreate
	 *    3. See A above. Done
	 * 
	 * B) 1. If not using using quick switch: launch dialog
	 *    2. Accept-> toggle theme
	 *    3. Restart
	 *    4. See B above. Done
	 * 
	 */
	
	
	 /* Created to compensate for a bug. See: 
	  * -https://code.google.com/p/android/issues/detail?id=3793#makechanges
	  * -https://code.google.com/p/android/issues/detail?id=4394
	  * -https://groups.google.com/forum/?fromgroups=#!topic/android-developers/vSZHsVWUCqk
	  */
	/** Sets theme before super.onCreate() via intent. */
	private void setThemeByIntent() {
		currThemeId = getIntent().getIntExtra(KEY_THEME_INTENT, -1);
		if (currThemeId > 0){
			setTheme(currThemeId);
		}
	}
	
	/** If #currThemeId is unset (-1) 
	 * it checks theme preference, and restarts activity for application.
	 * If #currThemeId  is set, it returns early.
	 * <p>
	 * Must be called before {@link #setContentView(int)} but 
	 * after super.onCreate(). </p>*/
	private void checkThemePref(){
		if (currThemeId != -1){ //if it does not equal -1
			return; //we must have our theme already set.
		}
		
		final String dark = getString(R.string.careerstack_pref_VALUE_THEME_DARK);
		//final String light = getString(R.string.careerstack_pref_VALUE_THEME_LIGHT);
		final String currTheme = prefs.getString(
				getString(R.string.careerstack_pref_KEY_THEME_PREF), 
				dark);		
		
		if (currTheme.equals(dark)){
			currThemeId = R.style.AppBaseTheme_Dark;
		} else {
			//light
			currThemeId = R.style.AppBaseTheme_Light;
		}
		
		final boolean quickSwitch = prefs.getBoolean(
				getString(R.string.careerstack_pref_KEY_QUICK_THEME_SWITCH), 
				false);
		if (quickSwitch){ //if we are not in 
			setTheme(currThemeId);
			Utility.setStatusBarColor(this);
		} else {
			resetActivityForTheme(); //reset
		}		
	}
	
	
	/** Toggles day and night mode pref and restarts.
	 * Assumes {@link #checkThemePref()} is called in {@link #onCreate(Bundle)}   */
	@SuppressLint("NewApi")
	protected void toggleDayNightMode(){
		final String key = getString(R.string.careerstack_pref_KEY_THEME_PREF);
		final String dark = getString(R.string.careerstack_pref_VALUE_THEME_DARK);
		final String light = getString(R.string.careerstack_pref_VALUE_THEME_LIGHT);
		
		if (currThemeId == R.style.AppBaseTheme_Dark) {
			currThemeId = R.style.AppBaseTheme_Light;
			prefs.edit().putString(key, light).commit(); //toggle values
        } else {
        	currThemeId = R.style.AppBaseTheme_Dark;
        	prefs.edit().putString(key, dark).commit();
        }
		
		resetActivityForTheme();
	}
		
	/** Restarts/Recreates the activity with the theme set. 
	 * Based upon the value of quick switch preference.  */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void resetActivityForTheme(){
		final boolean quickSwitch = quickSwitchTheme();
		
		//if quick switching and supported
		if (quickSwitch && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ){
			//quick switch
			getIntent().removeExtra(KEY_THEME_INTENT);
			super.recreate();  
			
		} else { 
			//otherwise, we'll restart ourself!
			try{
				Intent intent = getIntent();
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(KEY_THEME_INTENT, currThemeId);
				finish();
				startActivity(intent); //restart same intent			
			} catch (Exception e){
				Log.e(CLASS_NAME, "Error restarting activity: " + e);
			}
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Dialog helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Builds dialog for activity and shows it. */
	protected void showChangeThemeDialog(){
		new ConfirmationDialogFragment.Builder()
			.setTitle(R.string.action_toggleTheme)
			.setMessage(R.string.careerstack_theme_dialogMsg)
			.setPositive(R.string.careerstack_restart_dialogMsg_confirm)
			.setNegative(android.R.string.no)
			.create()
			.show(getSupportFragmentManager(), 
					ConfirmationDialogFragment.class.getName() +
					CLASS_NAME);
	}
	
}
