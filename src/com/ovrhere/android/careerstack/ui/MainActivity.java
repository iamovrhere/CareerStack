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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.dao.CareerItem;
import com.ovrhere.android.careerstack.ui.fragments.CareerItemFragment;
import com.ovrhere.android.careerstack.ui.fragments.MainFragment;
import com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment;
import com.ovrhere.android.careerstack.ui.fragments.dialogs.SearchBarDialogFragment;
import com.ovrhere.android.careerstack.utils.TabletUtil;

/** The main entry point into the application.
 * @author Jason J.
 * @version 0.11.0-20151005
 */
public class MainActivity extends AbstractThemedActivity 
	implements OnBackStackChangedListener, DialogInterface.OnClickListener,
	MainFragment.OnFragmentInteractionListener, 
	CareerItemFragment.OnFragmentInteractionListener,
	SearchResultsFragment.OnFragmentInteractionListener,
	SearchBarDialogFragment.OnDialogResultsListener{
	
	/** Class name for debugging purposes. */
	final static private String CLASS_NAME = MainActivity.class.getSimpleName();
	/** Whether or not to debug. */
	final static private boolean DEBUG = true;
	
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
	
	/** Bundle key. The current search state for the searchbar. Bundle. */
	final static private String KEY_CURRENT_SEARCH_BAR_STATE = 
			CLASS_NAME + ".KEY_CURRENT_SEARCH";
		
		
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Frag tags
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The main fragment tag. */
	final static private String TAG_MAIN_FRAG = 
			MainFragment.FRAGTAG;
	
	/** The tag for the search resultsfrag. */
	final static private String TAG_SEARCH_RESULTS_FRAG = 
			SearchResultsFragment.class.getName();
	
	/** The tag for the career item frag. */
	final static private String TAG_CAREER_ITEM_FRAG = 
			CareerItem.class.getName();
	
	
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
	
	/** The current shared preference. */
	private SharedPreferences prefs = null;
	
	/** The current menu as built by activity.
	 *  Initialized in {@link #onCreateOptionsMenu(Menu)} */
	private Menu menu = null;
	
	/** The tablet container message for tablet mode. */
	private TextView tabletMessage = null;
	
	
	/** The current search of the application set according to the keys of
	 * {@link SearchBarDialogFragment}.
	 * Set in {@link #onSearchRequest(Bundle)} & {@link #onSearch(DialogFragment, Bundle)} */ 
	private Bundle currSearchBarState = new Bundle();

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(KEY_FRAG_TAG_TACK, fragTagStack.getArrayList());
		outState.putSerializable(KEY_FRAG_SAVED_STATES, fragSavedStates);
		outState.putString(KEY_ACTIONBAR_TITLE, actionBarTitle);
		
		outState.putBundle(KEY_CURRENT_SEARCH_BAR_STATE, currSearchBarState);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportFragmentManager().addOnBackStackChangedListener(this);
		
		setContentView(R.layout.activity_main);
		
		tabletMessage = (TextView) findViewById(R.id.careerstack_main_text_job_description_message);
		
		if (savedInstanceState == null) {			
			loadFragment( new MainFragment(), TAG_MAIN_FRAG, false);
			actionBarTitle = getString(R.string.app_name);
			
		} else {
			try {
				currSearchBarState = 
						savedInstanceState.getBundle(KEY_CURRENT_SEARCH_BAR_STATE);
				
				if (savedInstanceState.getStringArrayList(KEY_FRAG_TAG_TACK) != null){
					fragTagStack.addAll(
							savedInstanceState.getStringArrayList(KEY_FRAG_TAG_TACK));
				}
				
				if (savedInstanceState.getString(KEY_ACTIONBAR_TITLE) != null){
					actionBarTitle = 
							savedInstanceState.getString(KEY_ACTIONBAR_TITLE);
				}
				reattachLastFragment();
				
				if (savedInstanceState.getSerializable(KEY_FRAG_SAVED_STATES) != null){
					try {
				fragSavedStates.putAll((Map<? extends String, ? extends Bundle>) 
						savedInstanceState.getSerializable(KEY_FRAG_SAVED_STATES));
					} catch (ClassCastException e){}
				}
				
			} catch (Exception e){
				if (DEBUG){
					Log.e(CLASS_NAME, "Should not be having an error here: " + e);
					e.printStackTrace();
				}
			}
		}
		
		getSupportActionBar().setTitle(actionBarTitle);
		
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		checkMenus();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
			
			return true;
		
		case R.id.action_refresh:
			refreshSearchRequest();
			return true;
			
		case R.id.action_search:
			showSearchBarDialog();
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
	    
	    if (TAG_CAREER_ITEM_FRAG.equals(fragTagStack.peek()) && inTabletMode()){
	    	//if we popped back to have the career item & tablet mode
	    	reattachLastFragment(); 
	    	return true;
	    }
	  
	    //show settings when not viewing settings
	    checkMenus();
	    checkTabletFrag();
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
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Functionality helpers
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Checks that the device is currently in tablet mode based on:
	 * <ol><li>Screen size</li><li>Ads enabled</li><li>Tablet mode enabled</li></ol>
	 * @return <code>true</code> if currently in tablet mode, <code>false</code>
	 * otherwise.	 */
	private boolean inTabletMode(){
		return TabletUtil.inTabletMode(getResources(), prefs);		
	}
	
	/** Sends the search request off the SearchResultsFragment;
	 * if the fragment is at the front it sends it directly, otherwise, it 
	 * clears the backstack and re-attaches.
	 * @param args The argments to forward onto {@link SearchResultsFragment}
	 */
	private void sendSearchRequest(Bundle args) {
		boolean requestSent = false;
		
		resetTabletContainerMessage();
		
		try {
			//get fragment then send results.
			Fragment frag = getSupportFragmentManager()
						.findFragmentByTag(TAG_SEARCH_RESULTS_FRAG);
			
			//check to see if we are already in the search frag
			if (frag != null){
				((SearchResultsFragment) frag).sendRequest(args);
				requestSent = true;
				
				//if not at front, clear stack and re-add
				if (!TAG_SEARCH_RESULTS_FRAG.equals(fragTagStack.peek())){
					loadFragment(frag, TAG_SEARCH_RESULTS_FRAG, false);	
				}				
			}
			
		} catch (ClassCastException e){
			Log.e(CLASS_NAME, "Something abnormal has occurred! : "+ e);
			if (DEBUG){
				throw e;
			}
		}
		
		/* If the request has not been sent yet, either due to an error or
		 * the frag the fragment is not in the stack yet. */
		if (!requestSent) {
			//launch fragment with no backstack
			loadFragment(
					SearchResultsFragment.newInstance(args), 
					TAG_SEARCH_RESULTS_FRAG, 
					false);
		}
	}
	

	
	/** Refreshes the search results page if available. If not
	 * available nothing happens. 	 */
	private void refreshSearchRequest(){
		//if we are already in the search frag
		if (TAG_SEARCH_RESULTS_FRAG.equals(fragTagStack.peek())){
			try {
				//get fragment then send results.
				SearchResultsFragment frag = (SearchResultsFragment)
						getSupportFragmentManager()
							.findFragmentByTag(TAG_SEARCH_RESULTS_FRAG);
				
				frag.retryRequest();		
				
				
			} catch (ClassCastException e){
				Log.e(CLASS_NAME, "Something abnormal has occurred! : "+ e);
				if (DEBUG){
					throw e;
				}
			}			
		} else if (DEBUG){
			Log.d(CLASS_NAME, "Why are we refreshing without search results?");
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Dialog helpers
	////////////////////////////////////////////////////////////////////////////////////////////////

	
	/** Initializes and shows the search bar dialog using the current
	 * search state. 	 */
	private void showSearchBarDialog(){
		SearchBarDialogFragment.newInstance(currSearchBarState)
			.show(	getSupportFragmentManager(),
					SearchBarDialogFragment.class.getName());
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Fragment methods
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * Tablet mode explanation:
	 * 
	 * 1. Whenever NOT in tablet mode OR in tablet mode viewing Settings
	 * or Main Frags, set R.id.tablet_container as view GONE.
	 * 
	 * 2. Whenever shifting TO tablet mode, CareerItemFragment is re-attached
	 * to tablet_container and popped, so that R.id.container is SearchResultsFragment.  
	 * 
	 * 3. Whenever shifting FROM tablet mode, CareerItemFragment is re-attached
	 * to R.id.container and tablet_container is HIDDEN. 
	 * 
	 * 4. Whenever attaching a new CareerItemFragment, tabletmode is checked.
	 * In tablet mode, it is attached to tablet_container, in regular R.id.container.
	 * 
	 * 5. Whenever searching, if in tablet mode, the message is reset in the tablet_container.
	 * 
	 */
	
	/** Re-attaches the last fragment. And resets back button. */
	private void reattachLastFragment() {
		//first, always check tablet fragment.
		checkTabletFrag();
		
		String currentTag = fragTagStack.peek();
		FragmentManager fm = getSupportFragmentManager();
		Log.d("Main", "currentTag: "+ currentTag);
		
		if (currentTag != null){
			if (inTabletMode()){
				//TODO re-arrange fragments accordingly
				
				Fragment careerItem = fm.findFragmentByTag(TAG_CAREER_ITEM_FRAG);
				//if the regular mode item is a career item
				if (TAG_CAREER_ITEM_FRAG.equals(currentTag)){
					
					fm.beginTransaction().remove(careerItem).commit();
					
					//pop to search result fragment
					onSupportNavigateUp();
					fm.executePendingTransactions();
					loadTabletFragment(careerItem, TAG_CAREER_ITEM_FRAG);
					
				} else if (careerItem == null){
					resetTabletContainerMessage();
				}
				
			} else if (TAG_SEARCH_RESULTS_FRAG.equals(currentTag)){ 
				//if we are in search mode, the we may have a lingering careeritem
				
				//TODO re-arrange fragments accordingly
				Fragment careerItem = fm.findFragmentByTag(TAG_CAREER_ITEM_FRAG);
				//re-attach careeritem
				if (careerItem != null){
					fm.beginTransaction().remove(careerItem).commit();
					fm.executePendingTransactions();
					loadFragment(careerItem, TAG_CAREER_ITEM_FRAG, true);
				}
			} else {
				Fragment frag = fm.findFragmentByTag(currentTag);
				fm.beginTransaction().attach(frag).commit();
			}
		}
		
		checkHomeButtonBack();
	}
	
	/** Checks to see if the current fragment is the settings fragment.
	 * If so deactivate menu, if not, re-enable it. Must be called after
	 * {@link #onCreateOptionsMenu(Menu)} 
	 */
	private void checkMenus(){
		if (menu == null){
			return;
		}
		final String currTag = fragTagStack.peek(); 
		
		menu.setGroupVisible(0, true);
		
		boolean showSearch = false;
		boolean showRefresh = false;
		
		if (TAG_MAIN_FRAG.equals(currTag)){
			showSearch = false;
			showRefresh = false;
		} else if (TAG_SEARCH_RESULTS_FRAG.equals(currTag)){
			showSearch = true;
			showRefresh = true;
		} else if (TAG_CAREER_ITEM_FRAG.equals(currTag)){
			 //FIXME the search results frag needs to "back search" before search can be enabled from items
			showSearch = false;
			showRefresh = false;
		}

		menu.findItem(R.id.action_search)
			.setVisible(showSearch)
			.setEnabled(showSearch);
		menu.findItem(R.id.action_refresh)
			.setVisible(showRefresh)
			.setEnabled(showRefresh);
				
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
	

	/** Shows and hides visibility based. */
	private void checkTabletFrag(){
		boolean show = false;
		if (inTabletMode()){
			final String tag = fragTagStack.peek();
			if (!(TAG_MAIN_FRAG.equals(tag))){
				//if not in main, do not show tablet container.
				show = true;
				getSupportFragmentManager().executePendingTransactions();
			}
		}
		findViewById(R.id.tablet_container)
			.setVisibility(show ? View.VISIBLE : View.GONE);
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
			//clear the entire backstack
			fragManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			fragManager.beginTransaction()
					.replace(R.id.container, fragment, tag)
					.commit();
			fragTagStack.clear();
			fragSavedStates.clear();
		}
		
		fragTagStack.push(tag);
		
		checkTabletFrag();
		checkMenus();
		checkHomeButtonBack();		
	}
	
	/** Loads fragment into tablet container. 
	 * @param fragment The fragment to add
	 * @param tag The tag to give the fragment */
	private void loadTabletFragment(Fragment fragment, String tag){
		FragmentManager fragManager = getSupportFragmentManager();
		checkTabletFrag();
		
		tabletMessage.setVisibility(View.GONE);
		fragManager.beginTransaction()
				.replace(R.id.tablet_container, fragment, tag)
				.commit();				
	}
	
	/** Resets the tablet container message by clearing it and inflating view stub. */
	private void resetTabletContainerMessage(){
		tabletMessage.setVisibility(View.VISIBLE);
		Fragment frag = getSupportFragmentManager()
				.findFragmentByTag(TAG_CAREER_ITEM_FRAG);
		if (frag != null){
			getSupportFragmentManager().beginTransaction()
				.remove(frag).commit();
		}
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
		currSearchBarState = new Bundle();
		
		args.putString(SearchResultsFragment.KEY_KEYWORD_TEXT, 
				bundle.getString(MainFragment.KEY_KEYWORD_TEXT));
		currSearchBarState.putString(SearchBarDialogFragment.KEY_KEYWORD_TEXT, 
				bundle.getString(MainFragment.KEY_KEYWORD_TEXT));
		
		args.putString(SearchResultsFragment.KEY_LOCATION_TEXT, 
				bundle.getString(MainFragment.KEY_LOCATION_TEXT));
		currSearchBarState.putString(SearchBarDialogFragment.KEY_LOCATION_TEXT, 
				bundle.getString(MainFragment.KEY_LOCATION_TEXT));
		
		args.putBoolean(SearchResultsFragment.KEY_RELOCATE_OFFER, 
				bundle.getBoolean(MainFragment.KEY_RELOCATE_OFFER));
		currSearchBarState.putBoolean(SearchBarDialogFragment.KEY_RELOCATE_OFFER, 
				bundle.getBoolean(MainFragment.KEY_RELOCATE_OFFER));
		
		args.putBoolean(SearchResultsFragment.KEY_REMOTE_ALLOWED, 
				bundle.getBoolean(MainFragment.KEY_REMOTE_ALLOWED));
		currSearchBarState.putBoolean(SearchBarDialogFragment.KEY_REMOTE_ALLOWED, 
				bundle.getBoolean(MainFragment.KEY_REMOTE_ALLOWED));
		
		int distance = bundle.getInt(MainFragment.KEY_DISTANCE, -1);
		if (distance > 0){
			args.putInt(SearchResultsFragment.KEY_DISTANCE, distance);
			currSearchBarState.putInt(SearchBarDialogFragment.KEY_DISTANCE, distance);
		}
		
		
		sendSearchRequest(args);
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
			if (inTabletMode()){
				loadTabletFragment(
						CareerItemFragment.newInstance(item), 
						TAG_CAREER_ITEM_FRAG);
			} else {
				loadFragment(
						CareerItemFragment.newInstance(item), 
						TAG_CAREER_ITEM_FRAG, 
						true);
			}
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment.OnFragmentInteractionListener#onHoldSavedStateRequest(android.os.Bundle)
	 */
	@Override
	public boolean onHoldSavedStateRequest(Bundle savedState) {
		fragSavedStates.put(TAG_SEARCH_RESULTS_FRAG, savedState);
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SearchResultsFragment.OnFragmentInteractionListener#onPopSavedStateRequest()
	 */
	@Override
	public Bundle onPopSavedStateRequest() {
		return fragSavedStates.remove(TAG_SEARCH_RESULTS_FRAG);
	}
	
	//end SearchResultsFragment listeners
	
	
	//start CareerItemFragment listeners
	@Override
	public boolean onTagClick(String tag) {
		Bundle searchArgs = new Bundle(); //prepare search args for tag
		searchArgs.putString(SearchResultsFragment.KEY_TAG_TEXT, tag);
		
		currSearchBarState = new Bundle(); //replace search with tag only
		currSearchBarState.putString(SearchBarDialogFragment.KEY_KEYWORD_TEXT,  tag);
		
		//remove any previous states
		fragSavedStates.remove(TAG_SEARCH_RESULTS_FRAG); 
		//launch request
		sendSearchRequest(searchArgs);
		return true;
	}
	//end CareerItemFragment listeners
	
	
	//start SearchBarDialogFragment
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.dialogs.SearchBarDialogFragment.OnDialogResultsListener#onSearch(android.support.v4.app.DialogFragment, android.os.Bundle)
	 */
	@Override
	public void onSearch(DialogFragment dialog, Bundle searchParams) {
		currSearchBarState = null;
		currSearchBarState = searchParams; //replace search
		//unpack request from dialog
		Bundle args = new Bundle();
		args.putString(SearchResultsFragment.KEY_KEYWORD_TEXT, 
				searchParams.getString(SearchBarDialogFragment.KEY_KEYWORD_TEXT));
		args.putString(SearchResultsFragment.KEY_LOCATION_TEXT, 
				searchParams.getString(SearchBarDialogFragment.KEY_LOCATION_TEXT));
		
		args.putBoolean(SearchResultsFragment.KEY_RELOCATE_OFFER, 
				searchParams.getBoolean(SearchBarDialogFragment.KEY_RELOCATE_OFFER));
		args.putBoolean(SearchResultsFragment.KEY_REMOTE_ALLOWED, 
				searchParams.getBoolean(SearchBarDialogFragment.KEY_REMOTE_ALLOWED));
		
		int distance = searchParams.getInt(SearchBarDialogFragment.KEY_DISTANCE, -1);
		if (distance > 0){
			args.putInt(SearchResultsFragment.KEY_DISTANCE, distance);
		}
		
		//remove any previous states
		fragSavedStates.remove(TAG_SEARCH_RESULTS_FRAG); 
		//launch request
		sendSearchRequest(args);
	}


	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.dialogs.SearchBarDialogFragment.OnDialogResultsListener#onCancel(android.support.v4.app.DialogFragment)
	 */
	@Override
	public void onCancel(DialogFragment dialog) {
		//do nothing		
	}
	
	//end SearchBarDialogFragment
	

	@Override
	public void onBackStackChanged() {
		checkHomeButtonBack();
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			toggleDayNightMode();
			break;

		default:
			break;
		}
		
	}
		
	
}
