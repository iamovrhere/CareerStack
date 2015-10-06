package com.ovrhere.android.careerstack.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.ovrhere.android.careerstack.R;
import com.ovrhere.android.careerstack.RestartReceiver;
import com.ovrhere.android.careerstack.ui.fragments.SettingsFragment;

/**
 * @author Jason J.
 * @version 0.1.0-20151005
 */
public class SettingsActivity extends AbstractThemedActivity implements 
	SettingsFragment.OnFragmentInteractionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP|ActionBar.DISPLAY_SHOW_TITLE);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		//if needed, dual panel settings can be handled here
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
				.add(R.id.container, new SettingsFragment())
				.commit();
		}
	}
	
	@Override
	public boolean onSupportNavigateUp() {
	    //This method is called when the up button is pressed. Just the pop back stack.
		finish();
	    return true;
	}
	
	/* (non-Javadoc)
	 * @see com.ovrhere.android.careerstack.ui.fragments.SettingsFragment.OnFragmentInteractionListener#onRestartRequest()
	 */
	@Override
	public boolean onRestartRequest() {
		RestartReceiver.sendBroadcast(this);
		return true;
	}
}
