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
package com.ovrhere.android.careerstack.ui.wrappers;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**  A wrapper to easily allow the seek bar to be given:
 * <ul>
 * <li>A minimum value</li>
 * <li>A maximum value</li>
 * <li>A step value</li>
 * </ul>
 * and to easily set these. If additional actions are needed 
 * for {@link OnSeekBarChangeListener} please use the 
 * {@link SeekBarWrapper#setOnSeekBarChangeListener(OnSeekBarChangeListener)}
 * to attach the listener, as this class will override any previous listeners.
 * <p>
 * <b>Remember to discard of this object when changing context.</b>
 * </p>
 * @author Jason J.
 * @version 0.1.0-20140916
 */
public class SeekBarWrapper implements OnSeekBarChangeListener {
	/** Exception for when step <= 0 */
	final static private String DETAILED_EXCEPTION_STEP_NEGATIVE = 
			"Step must be >= 0";
	/** Exception for when max <= min. Expects 2 ints. */
	final static private String DETAILED_EXCEPTION_MAX_SMALLER = 
			"Max (%d) cannot be smaller or equal to min (%d)";
	/** Exception for when step > range. Expects 2 ints. */
	final static private String DETAILED_EXCEPTION_STEP_LARGER = 
			"Range (%d) cannot be smaller than step (%d)";
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The progress listener. Can be null. */
	private OnProgressChanged onProgressChangedListener = null;
	/** An optional extra listener for external objects to perform 
	 * their own actions in {@link OnSeekBarChangeListener}. Can be null. */
	private OnSeekBarChangeListener extraListener = null;
	
	/** The minimum value the helper gives the seek bar. */
	final private int min;
	/** The steps between values. */
	final private int step;
	
	/** The seekbar we are helping. */
	final private SeekBar seekBar;	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Builds a helper with a min value of 0, max value of the {@link SeekBar},
	 * and a step of 1.
	 * @param seekBar The seekbar to adjust	 
	 * @param step The minimum step between allowed values. Must be positive */
	public SeekBarWrapper(SeekBar seekBar, int step) {
		this(seekBar, 0, seekBar.getMax(), step);
	}
	/** Builds a helper with given values.
	 * @param seekBar The seekbar to adjust
	 * @param min The minimum value (when at "0")
	 * @param max the maximum value (when at "100"). Must be larger than min.
	 * @param step he minimum step between allowed values. Must be positive
	 */
	public SeekBarWrapper(SeekBar seekBar, int min, int max, int step) {
		this.seekBar = seekBar;
		if (max <= min){
			throw new IllegalArgumentException(
					String.format(DETAILED_EXCEPTION_MAX_SMALLER, max, min)
					);
		}
		if (step <= 0){
			throw 
			new IllegalArgumentException(DETAILED_EXCEPTION_STEP_NEGATIVE);
		}
		int range = max - min;
		if (range < step){
			throw new IllegalArgumentException(
					String.format(DETAILED_EXCEPTION_STEP_LARGER, range, step)
					);
		}
		
		this.min = min;	
		this.step = step;
		this.seekBar.setOnSeekBarChangeListener(this);		
		this.seekBar.setMax(range); 
		this.seekBar.incrementProgressBy(step);
		this.seekBar.incrementSecondaryProgressBy(step);
	}
	
	/** Sets an additional OnSeekBarChangeListener to be called after the
	 * helper performs its actions.
	 * @param extraListener The listener to append to this.	 */
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener extraListener) {
		this.extraListener = extraListener;
	}
	
	/** Sets the progress listener which is fired every time the 
	 * {@link OnSeekBarChangeListener#onProgressChanged(SeekBar, int, boolean)}
	 * is fired.
	 * @param onProgressChangedListener The listener to receive updated values
	 */
	public void setOnProgressChangedListener(
			OnProgressChanged onProgressChangedListener) {
		this.onProgressChangedListener = onProgressChangedListener;
	}
	
	
	/** Sets the value of the seek bar according to min & step values given.
	 * @param value The value to recalculate into progress and set	 */
	public void setProgress(int value){
		int progress = (value - min)/step * step;
		seekBar.setProgress(progress);
	}
	
	/** Calculates and returns the seekbar's <i>value</i>
	 * @return The stepped seek bar value (not to be confused with progress) */
	public int getValue() {
		return seekBar.getProgress() + min;
	}
	
	/** Returns the seekbar as set in constructor.
	 * @return The {@link SeekBar} for this stepper.	 */
	public SeekBar getSeekBar() {
		return seekBar;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Ensures progress is kept to steps. 
	 * @param progress The progress to recalculate.
	 * @return The progress recalculated.
	 */
	private int recalculateProgress(int progress){
		progress /= step; 
		progress *= step; 
		return progress;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Informs listeners that the value has changed (after calculations)
	 * @author Jason J.
	 * @version 0.1.0-20140916	 */
	public static interface OnProgressChanged {
		/** Updates the listener with the new (recalculated) value. 
		 * @param value		 */
		public void onValueUpdate(int value);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		progress = recalculateProgress(progress);
		if (onProgressChangedListener != null){
			onProgressChangedListener.onValueUpdate(progress + min);
		}
		if (extraListener != null){
			extraListener.onProgressChanged(seekBar, progress, fromUser);
		}
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// nothing to be done here
		if (extraListener != null){
			extraListener.onStartTrackingTouch(seekBar);
		}
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// nothing to be done here
		if (extraListener != null){
			extraListener.onStopTrackingTouch(seekBar);
		}
	}
}
