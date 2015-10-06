/*
 * Copyright 2015 Jason J. (iamovrhere)
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

import com.gc.materialdesign.views.Slider;

import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**  Based heavily on {@link SeekBarWrapper} (commit eff62ad) <br/> 
 * A wrapper to easily allow the seek bar to be given:
 * <ul>
 * <li>A minimum value</li>
 * <li>A maximum value</li>
 * <li>A step value</li>
 * </ul>
 * and to easily set these. If additional actions are needed 
 * for {@link com.gc.materialdesign.views.Slider.OnValueChangedListener} please use the 
 * {@link MaterialSliderWrapper#setOnValueChangedListener(MaterialSliderWrapper.OnValueChangedListener)}
 * to attach the listener, as this class will override any previous listeners.
 * <p>
 * <b>Remember to discard of this object when changing context.</b>
 * </p>
 * @author Jason J.
 * @version 0.1.0-20151006
 */
public class MaterialSliderWrapper implements Slider.OnValueChangedListener {
	
	/** Exception for when step <= 0 */
	private static final String DETAILED_EXCEPTION_STEP_NEGATIVE = 
			"Step must be >= 0";
	/** Exception for when max <= min. Expects 2 ints. */
	private static final String DETAILED_EXCEPTION_MAX_SMALLER = 
			"Max (%d) cannot be smaller or equal to min (%d)";
	/** Exception for when step > range. Expects 2 ints. */
	private static final String DETAILED_EXCEPTION_STEP_LARGER = 
			"Range (%d) cannot be smaller than step (%d)";
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End constants
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** The progress listener. Can be null. */
	private OnValueChangedListener mOnValueChangedListener = null;
	
		/** The steps between values. */
	private final int mStep;
	
	/** The slider we are helping. */
	private final Slider mSlider;

	/** The current value of the slider. */
	/* This is necessary as the current version of the Slider uses post when setting values
	 * and can result in an inconsistent state. This helps with value-state consistency.	 */
	private int mValue = 0;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End members
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Builds a helper with a min value of 0, max value of the {@link SeekBar},
	 * and a step of 1.
	 * @param slider The slider to adjust	 
	 * @param step The minimum step between allowed values. Must be positive */
	public MaterialSliderWrapper(Slider slider, int step) {
		this(slider, 0, slider.getMax(), step);
	}
	
	/** Builds a helper with given values.
	 * @param slider The slider to adjust	 
	 * @param min The minimum value (when at "0")
	 * @param max the maximum value (when at "100"). Must be larger than min.
	 * @param step he minimum step between allowed values. Must be positive
	 */
	public MaterialSliderWrapper(Slider slider, int min, int max, int step) {
		this.mSlider = slider;
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
		
		this.mStep = step;
		this.mSlider.setOnValueChangedListener(this);		
		this.mSlider.setMax(max); 
		this.mSlider.setMin(min);
	}
	
	
	/** Sets the value/progress listener which is fired every time the 
	 * {@link OnSeekBarChangeListener#onProgressChanged(SeekBar, int, boolean)}
	 * is fired.
	 * @param onValueChangedListener The listener to receive updated values
	 */
	public void setOnValueChangedListener(
			OnValueChangedListener onValueChangedListener) {
		this.mOnValueChangedListener = onValueChangedListener;
	}
	
	
	/** Sets the value of the seek bar according to min & step values given.
	 * @param value The value to recalculate into progress and set	 */
	public void setProgress(int value){
		mValue = value;
		mSlider.setValue(value);
	}
	
	/** Calculates and returns the seekbar's <i>value</i>
	 * @return The stepped seek bar value (not to be confused with progress) */
	public int getValue() {
		return mValue;
	}
	
	/** Returns the slider as set in constructor.
	 * @return The {@link Slider} for this stepper.	 */
	public Slider getSlider() {
		return mSlider;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Helper methods 
	////////////////////////////////////////////////////////////////////////////////////////////////
	/** Ensures progress is kept to steps. 
	 * @param progress The progress to recalculate.
	 * @return The progress recalculated.
	 */
	private int recalculateProgress(int progress){
		progress /= mStep; 
		progress *= mStep; 
		return progress;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal Listeners
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Informs listeners that the value has changed (after calculations)
	 * @author Jason J.
	 * @version 0.1.0-20151006	 */
	public static interface OnValueChangedListener {
		/** Updates the listener with the new (recalculated) value. 
		 * It is advised you setContentDescription() here.
		 * @param value		 */
		public void onValueUpdate(int value);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Implemented interfaces
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onValueChanged(int value) {
		int progress = recalculateProgress(value);
		if (progress != value) {
			mSlider.setValue(progress);
		} 
		mValue = progress;
		if (mOnValueChangedListener != null){
			mOnValueChangedListener.onValueUpdate(progress);
		}
	}
}
