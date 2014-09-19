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

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


/** 
 * Defines a basic structure of of an Asynchronous Android model.
 * Documentation requires overriding.
 * 
 * @author Jason J.
 * @version 0.1.2-20140919 
 */
abstract public class AsyncModel {
	
	/** Generic error given if request fails. */
	final static public int ERROR_REQUEST_FAILED = 0x4000;
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// End Request & Reponses keys
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** A list of handlers added to the model. */
	final protected List<Handler> mHandlerList = new ArrayList<Handler>();

	/** Closes and cleans up anything the model 
	 * needs to before closing. Default clears the handler list. */ 
	public void dispose() {
		synchronized (mHandlerList) {
			mHandlerList.clear();
		}
	}
	
	/** Sends a request to the model to handle. 
	 * @param what The id of what message has been passed. 
	 * Expects a <code>REQUEST_</code> of some form. 
	 * @param object An object for the model to handle (such as a dao), 
	 * in addition to the message id. Can be <code>null</code>. 
	 * @return -1 if nothing was done (unsupported action), 0 for success,
	 * 1 for failure. */
	abstract public int sendMessage(int what, Object object);
	
	/** Sends a request to the model to handle. 
	 * @param what The id of what message has been passed. 
	 * Expects a <code>REQUEST_</code> of some form. 
	 * @param data A bundle of data for the model to handle. 
	 * @return -1 if nothing was done (unsupported action), 0 for success,
	 * 1 for failure. */
	public int sendMessage(int what, Bundle data){
		return sendMessage(what, (Object) data);
	}
	
	/** Sends a request to the model. Default behaviour is to call 
	 * {@link #sendMessage(int, Object)} with <code>null</code>. 
	 * @param what The id of what type of message has been passed. 
	 * @return -1 if nothing was done (unsupported action), 0 for success,
	 * 1 for failure. */
	public int sendMessage(int what){
		return sendMessage(what, null);
	}
	
	/** <p>Adds a {@link Handler} to the model to respond through.</p>
	 * <p>Responses are sent to the {@link Handler#Callback}
	 * interface. </p>
	 * <p>Such that:
	 * <p><code>
	 * <br />public class SomeFragment extends Fragment implements Handler.Callback {
	 * <br />
	 * <br />&nbsp;&nbsp;&nbsp;...
	 * <br />
	 * <br />&nbsp;&nbsp;&nbsp;@Override
	 * <br />&nbsp;&nbsp;&nbsp;public boolean handleMessage(Message msg) {
	 * <br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;switch(msg.what) {
	 * <br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case 1: 
	 * <br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;doWork(msg.getData());
	 * <br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;break;
	 * <br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}
	 * <br />&nbsp;&nbsp;&nbsp;}
	 * <br />}
	 * </code>
	 * </p> 
	 * </p>
	 * @param handler The handler for the model to send messages to.	 */
	public void addMessageHandler(Handler handler) {
		synchronized (mHandlerList) {
			mHandlerList.add(handler);
		}
	}
	
	/** Removes a message handler from the model.
	 * @param handler The handler to remove.	 */
	public final void removeMessageHandler(Handler handler) {
		synchronized (mHandlerList) {
			mHandlerList.remove(handler);
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	/// Start internal methods  
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** Notifies all handlers of an event, passing an id and relevant data. 
	 * @param what The id to give the message in order to identify it, 
	 * public constant is suggested.
	 * @param data A bundle of data for the handler to process.
	 */
	protected final void notifyHandlers(int what, Bundle data) {
		synchronized (mHandlerList) {
			if (!mHandlerList.isEmpty()) { 
				//We have contents, we have work to do.
				for (Handler handler : mHandlerList) {
					Message msg = Message.obtain(handler, what);
					msg.setData(data);
					msg.sendToTarget();
				}			
			}
		}
	}
	
	/** Notifies all handlers of an event, passing an id and an optional  object. 
	 * Note that if the object is <code>null</code>, an empty message is sent.
	 * @param what The id to give the message in order to identify it, 
	 * public constant is suggested. 
	 * @param obj (Optional) object to pass on to the handler to process. 
	 * Can be <code>null</code>.
	 */
	protected final void notifyHandlers(int what, Object obj) {
		synchronized (mHandlerList) {
			if (!mHandlerList.isEmpty()) { 
				//We have contents, we have work to do.
				if (obj != null){ //check once.
					for (Handler handler : mHandlerList) {
						Message msg = Message.obtain(handler, what, obj);
						msg.sendToTarget();
					}
				} else {
					for (Handler handler : mHandlerList) {
						handler.sendEmptyMessage(what);
					}
				}
			}
		}
	}
	
	/** Notifies all handlers of an event, passing an id, int arg1, int arg2
	 * and an optional object. The is less expensive than bundling in 
	 * {@link #notifyHandlers(int, Bundle)} as suggested by 
	 * {@link Message} documentation. 
	 * @param what The id to give the message in order to identify it, 
	 * public constant is suggested.
	 * @param arg1 The first value.
	 * @param arg2 The second value.
	 * @param obj (Optional) object to pass on to the handler to process. 
	 * Can be <code>null</code>.
	 * @see Message
	 */
	protected final void notifyHandlers(int what, int arg1, int arg2, Object obj) {
		synchronized (mHandlerList) {
			if (!mHandlerList.isEmpty()) { 
				//We have contents, we have work to do.
				for (Handler handler : mHandlerList) {
					Message msg = Message.obtain(handler, what, arg1, arg2, obj);
					msg.sendToTarget(); 
				}
			}
		}
	}
}
