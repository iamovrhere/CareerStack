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
package com.ovrhere.android.careerstack.utils;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * A compatibility utility for copying to the clipboard.
 * @author Jason J.
 * @version 0.1.0-20140902
 */
public class CompatClipboard {
	@SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
	/** Copes text to clipboard in a API compatible way.
	 * @param context The current context
	 * @param label The label used in higher APIs (ignored in <11)
	 * @param text The text to be copied.
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
    static public boolean copyToClipboard(Context context, String label, String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText(label, text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
