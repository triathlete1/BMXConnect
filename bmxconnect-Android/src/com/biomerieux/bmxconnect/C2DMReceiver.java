/*
 * Copyright 2010 Google Inc.
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
package com.biomerieux.bmxconnect;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.biomerieux.bmxconnect.shared.rest.Result;
import com.google.android.c2dm.C2DMBaseReceiver;

/**
 * Receive a push message from the Cloud to Device Messaging (C2DM) service.
 * This class should be modified to include functionality specific to your
 * application. This class must have a no-arg constructor and pass the sender id
 * to the superclass constructor.
 */
public class C2DMReceiver extends C2DMBaseReceiver {

    /**
     * Tag for logging.
     */
    private static final String TAG = "C2DMReceiver";

	ResultsSynchronizer resultsSynchronizer;
	
    public C2DMReceiver() {
        super(Setup.SENDER_ID);
        
        resultsSynchronizer = new ResultsSynchronizer();
    }

    /**
     * Called when a registration token has been received.
     * 
     * @param context the Context
     * @param registrationId the registration id as a String
     * @throws IOException if registration cannot be performed
     */
    @Override
    public void onRegistered(Context context, String registration) {
        DeviceRegistrar.registerOrUnregister(context, registration, true);
    }

    /**
     * Called when the device has been unregistered.
     * 
     * @param context the Context
     */
    @Override
    public void onUnregistered(Context context) {
        SharedPreferences prefs = Util.getSharedPreferences(context);
        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        DeviceRegistrar.registerOrUnregister(context, deviceRegistrationID, false);
    }

    /**
     * Called on registration error. This is called in the context of a Service
     * - no dialog or UI.
     * 
     * @param context the Context
     * @param errorId an error message, defined in {@link C2DMBaseReceiver}
     */
    @Override
    public void onError(Context context, String errorId) {
        context.sendBroadcast(new Intent(Util.UPDATE_UI_INTENT));
    }

    /**
     * Called when a cloud message has been received.
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
        	return;

        // Extract the message data
        String sender = (String) extras.get("sender");
        String message = (String) extras.get("message");
        String date = (String) extras.get("datetime");
        
        // Store the new result
        final SharedPreferences prefs = Util.getSharedPreferences(this);
        Result result = new Result();
        result.setAccountName(sender);
        result.setResult(message);
		result.setResultDateString(date);
        resultsSynchronizer.addNewResult(prefs, result);

        // TODO: display unacknowledged result count with a link to an activity that shows a list - user should be able to ack items on the list to clear them
//        MessageDisplay.displayMessage(context, intent);

        // Show a result notification
        Util.generateResultNotification(context, message, date);
    }
}
