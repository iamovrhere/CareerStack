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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.fragments.CareerItemFragment;
import com.ovrhere.android.careerstack.ui.fragments.MainFragment;
import com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment;
import com.ovrhere.android.careerstack.ui.fragments.SettingsFragment;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.ConfirmationDialogFragment;

/** The main entry point into the application.
 * @author Jason J.
 * @version 0.6.0-20141003
 */
public class MainActivity extends ActionBarActivity 
	implements OnBackStackChangedListener, DialogInterface.OnClickListener,
	MainFragment.OnFragmentInteractionListener, 
	SearchResultsFragment.OnFragmentInteractionListener,
	SettingsFragment.OnFragmentInteractionListener {
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = MainActivity.class.getSimpleName();
	
	/** Bundle key. The last fragment to be loaded (and so reloaded). 
	 * Array<String> */
	final static private String KEY_FRAG_TAG_TACK = 
			CLASS_NAME + ".KEY_LAST_FRAG_TAG";
	/** Bundle key. The group of saved states to retain.
	 *  Hashmap<String,Bundle>/Serializable. */
	final static private String KEY_FRAG_SAVED_STATES = 
			CLASS_NAME + ".KEY_FRAG_SAVED_STATES";
	/** Bundle key. The actionbar title in #actionBarTitle. String. */
	final static private String KEY_ACTIONBAR_TITLE =
			CLASS_NAME + ".KEY_ACTIONBAR_SUBTITLE";
	
	
	/** Extra Key. The theme intent value. Int. */
	final static private String KEY_THEME_INTENT = 
			CLASS_NAME + ".KEY_THEME_INTENT";
		
	/** The main fragment tag. */
	final static private String TAG_MAIN_FRAG = 
			MainFragment.FRAGTAG;
	/** The settings tag. */
	final static private String TAG_SETTINGS_FRAG = 
			SettingsFragment.class.getName();
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** The list of all fragments in play. */
	final private ArrayListStack<String> fragTagStack = 
			new ArrayListStack<String>();
	/** A map of all back stack fragment states. 
	 * Key: fragment tag (String), Value: savedState (Bundle) */
	final private HashMap<String, Bundle> fragSavedStates =
			new HashMap<String, Bundle>();
	
	/** The current actionbar subtitle. */
	private String actionBarTitle = "";
	
	/** The current theme. Default is -1. */
	private int currThemeId = -1;
	
	/** The current shared preference. */
	private SharedPreferences prefs = null;
	
	/** The current menu as built by activity.
	 *  Initialized in {@link #onCreateOptionsMenu(Menu)} */
	private Menu menu = null;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(KEY_FRAG_TAG_TACK, fragTagStack.getArrayList());
		outState.putSerializable(KEY_FRAG_SAVED_STATES, fragSavedStates);
		outState.putString(KEY_ACTIONBAR_TITLE, actionBarTitle);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setThemeByIntent();
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		
		if (PreferenceUtils.isFirstRun(this)){
			PreferenceUtils.setToDefault(this);
		}
		prefs = PreferenceUtils.getPreferences(this);
		//checks and, if necessary, restarts activity for theme
		checkThemePref(); 
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {			
			loadFragment( new MainFragment(), TAG_MAIN_FRAG, false);
			actionBarTitle = getString(R.string.app_name);
		} else {
			if (savedInstanceState.getStringArrayList(KEY_FRAG_TAG_TACK) != null){
				fragTagStack.addAll(
						savedInstanceState.getStringArrayList(KEY_FRAG_TAG_TACK));
			}
			if (savedInstanceState.getSerializable(KEY_FRAG_SAVED_STATES) != null){
				try {
			fragSavedStates.putAll((Map<? extends String, ? extends Bundle>) 
					savedInstanceState.getSerializable(KEY_FRAG_SAVED_STATES));
				} catch (ClassCastException e){}
			}
			if (savedInstanceState.getString(KEY_ACTIONBAR_TITLE) != null){
				actionBarTitle = 
						savedInstanceState.getString(KEY_ACTIONBAR_TITLE);
			}
			
			reattachLastFragment();
		}
		
		getSupportActionBar().setTitle(actionBarTitle);
		
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		checkSettings();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			if (fragTagStack.peek().equals(TAG_SETTINGS_FRAG) == false){
				loadFragment(
						new SettingsFragment(),
						TAG_SETTINGS_FRAG, 
						true);
				setActionBarTitle(getString(R.string.action_settings));
			}
			//hide settings when viewing settings
			checkSettings();
			return true;
			
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

	@Override
	public void onBackPressed() {
		if (canBack()){
			//do what ever would be done for back actionbar
			onSupportNavigateUp(); 
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onSupportNavigateUp() {
	    //This method is called when the up button is pressed. Just the pop back stack.
		setActionBarTitle(getString(R.string.app_name));
	    getSupportFragmentManager().popBackStack();
	    fragTagStack.pop();	    
	  
	    //show settings when not viewing settings
	    checkSettings();
	    return true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Misc. Helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets actionbar title in {@link #actionBarTitle} & sets title to it. */
	private void setActionBarTitle(String title) {
		actionBarTitle = title;
		getSupportActionBar().setTitle(actionBarTitle);
	}
	
	/** Retreives the quick switch theme pref. */
	private boolean quickSwitchTheme() {
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
	 * B) 1 If NOT using quick switch, set theme intent and restart
	 *   2 If theme intent is set, set theme. Done.
	 * 
	 * ---
	 * 
	 * Changing themes:
	 * A)1. If using quick switch mode: toggle theme
	 *   2. Call recreate
	 *   3. See A above. Done
	 * 
	 * B)1. If not using using quick switch: launch dialog
	 *   2. Accept-> toggle theme
	 *   3. Restart
	 *   4. See B above. Done
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
		
		final boolean quickSwitch =prefs.getBoolean(
				getString(R.string.careerstack_pref_KEY_QUICK_THEME_SWITCH), 
				false);
		if (quickSwitch){ //if we are not in 
			setTheme(currThemeId);
		} else {
			resetActivityForTheme(); //reset
		}
		
	}
	
	/** Toggles day and night mode pref and restarts.
	 * Assumes {@link #checkThemePref()} is called in {@link #onCreate(Bundle)}   */
	@SuppressLint("NewApi")
	private void toggleDayNightMode(){
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
			super.recreate();  
			
		} else { 
			//otherwise, we'll restart!
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
	private void showChangeThemeDialog(){
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
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Fragment method
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Re-attaches the last fragment. And resets back button. */
	private void reattachLastFragment() {
		String currentTag = fragTagStack.peek();
		if (currentTag != null){
			Fragment frag = getSupportFragmentManager()
					.findFragmentByTag(currentTag);
			getSupportFragmentManager().beginTransaction()
					.attach(frag).commit();
		}
		
		checkHomeButtonBack();
	}
	
	/** Checks to see if the current fragment is the settings fragment.
	 * If so deactivate menu, if not, re-enable it. Must be called after
	 * {@link #onCreateOptionsMenu(Menu)} 
	 */
	private void checkSettings(){
		if (menu == null){
			return;
		}
		if (TAG_SETTINGS_FRAG.equals(fragTagStack.peek())){
			menu.setGroupVisible(0, false);
		} else {
			menu.setGroupVisible(0, true);
		}
	}
	
	/** Returns whether or not there is a backstack. */
	private boolean canBack() {
		return getSupportFragmentManager().getBackStackEntryCount() > 0;
	}

	/** Checks to see whether to enable the action bar back. */
	private void checkHomeButtonBack() {
		boolean canback = canBack();
		//boolean canback = fragTagStack.size() > 0;
		getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
	}
	
	
	
	/** Loads a fragment either by adding or replacing and then adds it to
	 * the fragTagList.
	 * @param fragment The fragment to add
	 * @param tag The tag to give the fragment
	 * @param backStack <code>true</code> to add to backstack, 
	 * <code>false</code> to not.
	 */
	private void loadFragment(Fragment fragment, String tag, 
			boolean backStack){
		FragmentManager fragManager = getSupportFragmentManager();
		if (backStack){
			String prevTag = fragTagStack.peek();
			fragManager.beginTransaction()
				.addToBackStack(prevTag)
				.replace(R.id.container, fragment, tag).commit();
		} else {
			fragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragManager.beginTransaction()
					.replace(R.id.container, fragment, tag)
					.commit();
			fragTagStack.clear();
			fragSavedStates.clear();
		}
		checkHomeButtonBack();
		fragTagStack.push(tag);		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal classes
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Simple wrapper to manage array list as a stack. */
	@SuppressWarnings("unused")
	private static class ArrayListStack <Obj>{
		final private ArrayList<Obj> listStack = new ArrayList<Obj>();
		
		/** Returns stack size. */
		public int size(){ return listStack.size(); }
		
		/** Clears all elements */
		public void clear(){ listStack.clear(); }
		
		/** Adds all elements to stack. */
		public void addAll(ArrayList<Obj> arrayList){	
			listStack.addAll(arrayList); }
		
		/** Returns all elements to as {@link ArrayList}. */
		public ArrayList<Obj> getArrayList(){
			return this.listStack;	}
		
		/** Pushes object onto "stack" */
		public boolean push(Obj object){	return listStack.add(object);	}
		
		/** Pops the last element and returns it or <code>null</code>. */
		public Obj pop(){
			if (listStack.size() > 0){
				return listStack.remove(listStack.size()-1);
			}
			return null;
		}
		/** Displays last element without removing it. (or null) */
		public Obj peek(){
			if (listStack.size() > 0){
				return listStack.get(listStack.size()-1);
			}
			return null;
		}
		
		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/* MainFragments listener
	 * (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.MainFragment.OnFragmentInteractionListener#onSearchRequest(android.os.Bundle)
	 */
	@Override
	public boolean onSearchRequest(Bundle bundle) {
		Bundle args = new Bundle();
		args.putString(SearchResultsFragment.KEY_KEYWORD_TEXT, 
				bundle.getString(MainFragment.KEY_KEYWORD_TEXT));
		args.putString(SearchResultsFragment.KEY_LOCATION_TEXT, 
				bundle.getString(MainFragment.KEY_LOCATION_TEXT));
		
		args.putBoolean(SearchResultsFragment.KEY_RELOCATE_OFFER, 
				bundle.getBoolean(MainFragment.KEY_RELOCATE_OFFER));
		args.putBoolean(SearchResultsFragment.KEY_REMOTE_ALLOWED, 
				bundle.getBoolean(MainFragment.KEY_REMOTE_ALLOWED));
		
		int distance = bundle.getInt(MainFragment.KEY_DISTANCE, -1);
		if (distance > 0){
			args.putInt(SearchResultsFragment.KEY_DISTANCE, distance);
		}
		//launch fragment with no backstack
		loadFragment(
				SearchResultsFragment.newInstance(args), 
				SearchResultsFragment.class.getName(), 
				false);
		return true;
	}
	
	//end MainFragments listeners
	
	
	//start SearchResultsFragment listeners
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment.OnFragmentInteractionListener#onCareerItemRequest(com.ovrhere.android.careerstack.dao.CareerItem)
	 */
	@Override
	public boolean onCareerItemRequest(CareerItem item) {
		if (item != null){
			loadFragment(
					CareerItemFragment.newInstance(item), 
					CareerItemFragment.class.getName(), 
					true);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment.OnFragmentInteractionListener#onHoldSavedStateRequest(android.os.Bundle)
	 */
	@Override
	public boolean onHoldSavedStateRequest(Bundle savedState) {
		final String tag = SearchResultsFragment.class.getName();
		fragSavedStates.put(tag, savedState);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment.OnFragmentInteractionListener#onPopSavedStateRequest()
	 */
	@Override
	public Bundle onPopSavedStateRequest() {
		final String tag = SearchResultsFragment.class.getName();
		return fragSavedStates.remove(tag);
	}
	
	//end SearchResultsFragment listeners
	
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SettingsFragment.OnFragmentInteractionListener#onRestartRequest()
	 */
	@Override
	public boolean onRestartRequest() {
		//pure restart
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		finish();
		startActivity(intent);
		return true;
	}
	
	//end SettingsFragment
	

	@Override
	public void onBackStackChanged() {
		checkHomeButtonBack();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			dialog.dismiss(); //dismiss first for safety?
			toggleDayNightMode();
			break;

		default:
			break;
		}
		
	}
		
	
}
