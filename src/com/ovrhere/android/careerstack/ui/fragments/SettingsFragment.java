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
package com.ovrhere.android.careerstack.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;
import android.util.Log;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.ConfirmationDialogFragment;
import com.ovrhere.android.careerstack.utils.ToastManager;

/** 
 * Fragment for displaying settings and version number.
 * 
 * Requires:
 * <a href="https://github.com/kolavar/android-support-v4-preferencefragment" 
 * target="_blank">android-support-v4-preferencefragment</a>
 * 
 * Requires {@link OnFragmentInteractionListener} to be implemented by Activity.
 * @author Jason J.
 * @version 0.5.0-20141031
 */
public class SettingsFragment extends PreferenceFragment 
 implements OnPreferenceClickListener {
	/** Class name for debugging purposes. */
	final static private String LOGTAG = SettingsFragment.class
			.getSimpleName();
	/** Basic debugging bool. */
	final static private boolean DEBUG = true;
	
	/** Clear all request. */
	final static private int REQUEST_CLEAR_ALL = 0x101;
	/** The quick theme switch pref. */
	final static private int REQUEST_QUICK_SWITCH = 0x102;
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The toast manager for this frag. */
	private ToastManager tm = null;
	
	/** The quick switching pref. */
	private CheckBoxPreference prefQuickSwitching = null;
	
	/** Listener for activity requests. */
	private OnFragmentInteractionListener mFragInteractionListener = null;
	
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		getPreferenceManager().setSharedPreferencesName(
				getString(R.string.preferenceutil_PREFERENCE_FILE_KEY));
		
		tm = new ToastManager(getActivity());
		
		refreshPreferences();
		
	}
	
	
	@Override
	public void onDestroyView() {	
		super.onDestroyView();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case REQUEST_CLEAR_ALL:
			if (resultCode == Activity.RESULT_OK){
				resetSettings();
			}
			break;
			
		case REQUEST_QUICK_SWITCH:
			//if yes, set result to yes. If no, set it to false.
			if (resultCode == Activity.RESULT_OK){
				mFragInteractionListener.onRestartRequest();
				prefQuickSwitching.setChecked(true);
			} else {
				prefQuickSwitching.setChecked(false);
			}			
			break;
			
		default:
			break;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mFragInteractionListener = 
					(OnFragmentInteractionListener) activity;
		} catch (ClassCastException e){
			Log.e(LOGTAG, "Activity must implement :" +
					OnFragmentInteractionListener.class.getSimpleName());
			throw e;
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes software version & reset settings. */
	private void initNonSettings() {

		getPreferenceManager().findPreference(
				getString(R.string.careerstack_settings_KEY_CLEAR_SETTINGS)
				).setOnPreferenceClickListener(this);
		
		Preference  softwareVersion =
				getPreferenceManager().findPreference(
						getString(R.string.careerstack_settings_KEY_SOFTWARE_VERSION)
						);
		softwareVersion.setSummary(softwareVersionName());
		//softwareVersion.setEnabled(false); //why bother disabling?
	}
	
	/** Sets preference to null before attaching settings. */
	private void refreshPreferences(){
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.settings);
		
		prepareQuickSwitch();
		prepareTabletMode();
		
		initNonSettings();
	}

	/** Prepares quick switch; enabling or hiding based on version support. */
	private void prepareQuickSwitch() {
		final String key = getString(R.string.careerstack_pref_KEY_QUICK_THEME_SWITCH);
		prefQuickSwitching = (CheckBoxPreference) 
				getPreferenceManager().findPreference(key);
		if (Build.VERSION_CODES.HONEYCOMB <= Build.VERSION.SDK_INT ){
			prefQuickSwitching.setOnPreferenceClickListener(this);
			prefQuickSwitching.setEnabled(true);
			prefQuickSwitching.setSummary(
					getString(R.string.careerstack_settings_quickSwitch_summary)
					);
		} else {
			getPreferenceScreen().removePreference(prefQuickSwitching);
		}
	}
	
	/** Prepares tablet mode; enabling or hiding based on size support. */
	private void prepareTabletMode(){
		final String key = getString(R.string.careerstack_pref_KEY_USE_TABLET_LAYOUT);
		final boolean hasTabletMode = 
				getResources().getBoolean(R.bool.careerstack_has_tablet_mode);
		
		if (!hasTabletMode){
			//if not supported, remove preference.
			Preference prefTabletMode = getPreferenceManager().findPreference(key);
			prefTabletMode.setEnabled(false);
			getPreferenceScreen().removePreference(prefTabletMode);
		} 
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helper methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Resets all settings then toasts it. */
	private void resetSettings() {
		PreferenceUtils.getPreferences(getActivity())
						.edit()
						.clear()
						.commit(); //empty all settings
		PreferenceUtils.setToDefault(getActivity()); //reset
		
		refreshPreferences();
		tm.toastLong(getString(R.string.careerstack_toast_clearedSettings));
	}
	
	/** Creates and shows dialog. */
	private void showClearSettingsDialog(){
		new ConfirmationDialogFragment.Builder()
			.setTargetFragment(this, REQUEST_CLEAR_ALL)
			.setTitle(R.string.careerstack_settings_clearSettings_title)
			.setMessage(R.string.careerstack_settings_clearSettings_confirmMsg)
			.setPositive(android.R.string.ok)
			.setNegative(android.R.string.cancel)
			.create()
			.show(getFragmentManager(), 
				ConfirmationDialogFragment.class.getName()
					+SettingsFragment.class.getSimpleName()+".clearSettings");
	}
	
	/** Builds and shows quick switch confirm dialog. */
	private void showQuickSwitchDialog(){
		new ConfirmationDialogFragment.Builder()
			.setTargetFragment(this, REQUEST_QUICK_SWITCH)
			.setTitle(R.string.careerstack_settings_quickSwitch_title)
			.setMessage(R.string.careerstack_settings_quickSwitch_confirmMsg)
			.setPositive(R.string.careerstack_restart_dialogMsg_confirm)
			.setNegative(android.R.string.no)
			.create()
			.show(getFragmentManager(), 
					ConfirmationDialogFragment.class.getName()
						+SettingsFragment.class.getSimpleName()+".resetTheme");
	}
	
	/** Returns the software build version. */
	private String softwareVersionName(){
		try {
			return getActivity()	.getPackageManager()
									.getPackageInfo(getActivity()
									.getPackageName(), 0)
									.versionName;
		} catch (NameNotFoundException e) {
			if (DEBUG){
				e.printStackTrace();
			}
			return "Unavailable";
		}
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String clearSettings = 
				getString(R.string.careerstack_settings_KEY_CLEAR_SETTINGS);
		
		if (clearSettings.equals(preference.getKey())){
			showClearSettingsDialog();
			return true;
		} else if (prefQuickSwitching.equals(preference)){
			//it was checked, we just unchecked it
			if (prefQuickSwitching.isChecked() == false){				
				prefQuickSwitching.setChecked(false); //if checked, just uncheck
			} else {
				showQuickSwitchDialog();
			}
			return true;
		}
		return false;
	}
	

	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * The interaction listener that the activity must implement to handle the 
	 * {@link SettingsFragment}'s requests. 
	 * @author Jason J.
	 * @version 0.1.0-20141003
	 */
	static public interface OnFragmentInteractionListener {		
		/** Sends activity a request to restart. */
		public boolean onRestartRequest();
	}

	
}
