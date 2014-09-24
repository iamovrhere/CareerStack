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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.preference.PreferenceFragment;

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
 * @author Jason J.
 * @version 0.2.0-20140923
 */
public class SettingsFragment extends PreferenceFragment 
 implements OnPreferenceClickListener {
	/** Class name for debugging purposes. */
	@SuppressWarnings("unused")
	final static private String CLASS_NAME = SettingsFragment.class
			.getSimpleName();
	/** Basic debugging bool. */
	final static private boolean DEBUG = true;
	
	/** Clear all request. */
	final static private int REQUEST_CLEAR_ALL = 0x101;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The toast manager for this frag. */
	private ToastManager tm = null;
	
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		getPreferenceManager().setSharedPreferencesName(
				getString(R.string.careerstack_PREFERENCE_FILE_KEY));
		
		tm = new ToastManager(getActivity());
		
		refreshPreferences();
		initClearSettingsDialog();
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

		default:
			break;
		}
	}

	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Initialization helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Initializes software version & reset settings. */
	public void initNonSettings() {

		getPreferenceManager().findPreference(
				getString(R.string.careerstack_settings_KEY_CLEAR_SETTINGS)
				).setOnPreferenceClickListener(this);
		
		Preference  softwareVersion =
				getPreferenceManager().findPreference(
						getString(R.string.careerstack_settings_KEY_SOFTWARE_VERSION)
						);
		softwareVersion.setSummary(softwareVersionName());
		softwareVersion.setEnabled(false);
	}
	
	/** Sets preference to null before attaching settings. */
	public void refreshPreferences(){
		setPreferenceScreen(null);
		addPreferencesFromResource(R.xml.settings);
		initNonSettings();
	}
	
	/** Builds clear setting dialog. */
	@Deprecated
	private void initClearSettingsDialog(){
		 new AlertDialog.Builder(getActivity())
			.setTitle(R.string.careerstack_settings_clearSettings_title)
			.setMessage(R.string.careerstack_settings_clearSettings_confirmMsg)
			.setIcon(android.R.drawable.ic_dialog_alert)
			//.setPositiveButton(android.R.string.ok, this)
			//.setNegativeButton(android.R.string.cancel, this)
			.create();
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
					+SettingsFragment.class.getSimpleName());
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
	/// Internal interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/* Fragment interactions/requests go through this interface.	 * 
	 * @author Jason J.
	 * @version 0.1.0-20140922	 */
	//public interface OnFragmentInteractionListener{}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String clearSettings = 
				getString(R.string.careerstack_settings_KEY_CLEAR_SETTINGS);
		if (clearSettings.equals(preference.getKey())){
			showClearSettingsDialog();
		}
		return false;
	}
	

	
}
