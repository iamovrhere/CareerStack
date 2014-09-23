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
import com.ovrhere.android.careerstack.prefs.PreferenceUtils;
import com.ovrhere.android.careerstack.ui.fragments.MainFragment;
import com.ovrhere.android.careerstack.ui.fragments.SettingsFragment;
import com.ovrhere.android.careerstack.ui.listeners.OnFragmentRequestListener;

/** The main entry point into the application.
 * @author Jason J.
 * @version 0.4.0-20140922
 */
public class MainActivity extends ActionBarActivity 
	implements OnFragmentRequestListener, OnBackStackChangedListener {
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
	
	/** The main fragment tag. */
	final static private String TAG_MAIN_FRAG = 
			MainFragment.FRAGTAG;
	
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
	
	/** The current theme. Default is -1. */
	private int currThemeId = -1;
	
	/** The current shared preference. */
	private SharedPreferences prefs = null;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(KEY_FRAG_TAG_TACK, fragTagStack.getArrayList());
		outState.putSerializable(KEY_FRAG_SAVED_STATES, fragSavedStates);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		
		if (PreferenceUtils.isFirstRun(this)){
			PreferenceUtils.setToDefault(this);
		}
		prefs = PreferenceUtils.getPreferences(this);
		checkThemePref();
		
		setContentView(R.layout.activity_main);
		
		if (savedInstanceState == null) {			
			loadFragment( new MainFragment(), TAG_MAIN_FRAG, false);
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
			reattachLastFragment();
		}
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			loadFragment(
					new SettingsFragment(),
					SettingsFragment.class.getName(), 
					true);
			return true;
		case R.id.action_toggleTheme:
			toggleDayNightMode();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onSupportNavigateUp() {
	    //This method is called when the up button is pressed. Just the pop back stack.
	    getSupportFragmentManager().popBackStack();
	    fragTagStack.pop();	    
	    return true;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper method
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks theme preference and sets theme accordingly. 
	 * Should be called before {@link #setContentView(int)} */
	private void checkThemePref(){
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
		setTheme(currThemeId); 
	}
	
	/** Toggles day and night mode pref and restarts.
	 * Assumes {@link #checkThemePref()} is called in {@link #onCreate(Bundle)} */
	@SuppressLint("NewApi")
	private void toggleDayNightMode(){
		final String key = getString(R.string.careerstack_pref_KEY_THEME_PREF);
		final String dark = getString(R.string.careerstack_pref_VALUE_THEME_DARK);
		final String light = getString(R.string.careerstack_pref_VALUE_THEME_LIGHT);
		
		if (currThemeId == R.style.AppBaseTheme_Dark) {
			prefs.edit().putString(key, light).commit(); //toggle values
        } else {
        	prefs.edit().putString(key, dark).commit();
        }
        this.recreate();
	}
		
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void recreate() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			super.recreate();
			//if the method exists in higher apis, use it. 
		} else { //TODO test this on 2.3.3.3
			//otherwise, we'll figure it out!
			try{
				Intent intent = getIntent();
				finish();
				startActivity(intent); //restart same intent
			} catch (Exception e){
				Log.e(CLASS_NAME, "Error restarting activity: " + e);
			}
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Fragment method
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Reattaches the last fragment. And resets back button. */
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

	/** Checks to see whether to enable the action bar back. */
	private void checkHomeButtonBack() {
		boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
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
	@Override
	public boolean onRequestNewFragment(Fragment fragment, String tag,
			boolean backStack) {
		loadFragment(fragment, tag, backStack);
		return true;
	}
	
	@Override
	public boolean onRequestHideActionBar(boolean hide) {
		// TODO Auto-generated method stub
		return false;
	}	
	
	@Override
	public boolean onRequestHoldSavedState(String tag, Bundle savedState) {
		fragSavedStates.put(tag, savedState);
		return true;
	}
	
	@Override
	public Bundle onRequestPopSavedState(String tag) {
		return fragSavedStates.remove(tag);
	}
	

	@Override
	public void onBackStackChanged() {
		checkHomeButtonBack();
	}
		
	
}
