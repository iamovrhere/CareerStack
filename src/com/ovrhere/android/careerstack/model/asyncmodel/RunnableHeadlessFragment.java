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
package com.ovrhere.android.careerstack.model.asyncmodel;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/** Headless fragment to assist in the use of the {@link Runnable}s.
 * In doing so, the request can be done more readily with an activity even during 
 * rotations. 
 * @author Jason J.
 * @version 0.1.0-20140905
 */
public class RunnableHeadlessFragment extends Fragment {
	/** The local reference to the request. */
	private Runnable mRunnable = null;
	/** The thread to run on. */
	private Thread mThread = null;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// 
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Sets the runnable to run.. 
	 * @param runnable  */
	public void setRunable(Runnable runnable){
		this.mRunnable  = runnable;
	}
	/** Returns the currently set runnable.
	 * @return Runnable or <code>null</code> if unset.
	 */
	public Runnable getRunnable() {
		return mRunnable;
	}
	/** Starts the runnable in a thread. */
	public boolean startThread() {
		mThread = new Thread(mRunnable);
		mThread.start();
		return true;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End accessors and mutators
	////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return null;
	}
}
