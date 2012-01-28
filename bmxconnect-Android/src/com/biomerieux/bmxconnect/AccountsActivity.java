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

import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.c2dm.C2DMessaging;

/**
 * Account selections activity - handles device registration and unregistration.
 */
public class AccountsActivity extends Activity implements AuthenticationCallback {

    /**
     * Tag for logging.
     */
//    private static final String TAG = "AccountsActivity";

    /**
	 * The selected position in the ListView of accounts.
	 */
    private int mAccountSelectedPosition;
	
	/**
	 * True if we are waiting for App Engine authorization.
	 */
	private boolean mPendingAuth;

	/**
	 * The current context.
	 */
	private final Context mContext = this;

    private RegistrationHelper registrationHelper = new RegistrationHelper(this);

	/**
     * Begins the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String deviceRegistrationID = prefs.getString(Util.DEVICE_REGISTRATION_ID, null);
        if (deviceRegistrationID == null) {
            // Show the 'connect' screen if we are not connected
            setScreenContent(R.layout.connect);
        } else {
            // Show the 'disconnect' screen if we are connected
            setScreenContent(R.layout.disconnect);
        }
    }

    /**
     * Resumes the activity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (mPendingAuth) {
            mPendingAuth = false;
            String regId = C2DMessaging.getRegistrationId(mContext);
            if (regId != null && !"".equals(regId)) {
                DeviceRegistrar.registerOrUnregister(mContext, regId, true);
            } else {
                C2DMessaging.register(mContext, Setup.SENDER_ID);
            }
        }
    }

    // Manage UI Screens

    /**
     * Sets up the 'connect' screen content.
     */
    private void setConnectScreenContent() {
        List<String> accounts = getGoogleAccounts();
        if (accounts.size() == 0) {
            // Show a dialog and invoke the "Add Account" activity if requested
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.needs_account);
            builder.setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_ADD_ACCOUNT));
                }
            });
            builder.setNegativeButton(R.string.skip, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setIcon(android.R.drawable.stat_sys_warning);
            builder.setTitle(R.string.attention);
            builder.show();
        } else {
            final ListView listView = (ListView) findViewById(R.id.select_account);
            listView.setAdapter(new ArrayAdapter<String>(mContext, R.layout.account, accounts));
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(mAccountSelectedPosition, true);

            final Button connectButton = (Button) findViewById(R.id.connect);
            connectButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // Set "connecting" status
                    SharedPreferences prefs = Util.getSharedPreferences(mContext);
                    prefs.edit().putString(Util.CONNECTION_STATUS, Util.CONNECTING).commit();
                    // Get account name
                    mAccountSelectedPosition = listView.getCheckedItemPosition();
                    TextView account = (TextView) listView.getChildAt(mAccountSelectedPosition);
                    // Register
                    registrationHelper.register((String) account.getText(), (AuthenticationCallback)mContext);
                    finish();
                }
            });
        }
    }

    public void onAuthenticationComplete() {
    	C2DMessaging.register(mContext, Setup.SENDER_ID);
    }
    
    /**
     * Sets up the 'disconnected' screen.
     */
    private void setDisconnectScreenContent() {
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String accountName = prefs.getString(Util.ACCOUNT_NAME, "Unknown");

        // Format the disconnect message with the currently connected account
        // name
        TextView disconnectText = (TextView) findViewById(R.id.disconnect_text);
        String message = getResources().getString(R.string.disconnect_text);
        String formatted = String.format(message, accountName);
        disconnectText.setText(formatted);

        final Button disconnectButton = (Button) findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Unregister
                C2DMessaging.unregister(mContext);
                finish();
            }
        });
    }

    /**
     * Sets the screen content based on the screen id.
     */
    private void setScreenContent(int screenId) {
        setContentView(screenId);
        switch (screenId) {
            case R.layout.disconnect:
                setDisconnectScreenContent();
                break;
            case R.layout.connect:
                setConnectScreenContent();
                break;
        }
    }

    /**
     * Returns a list of registered Google account names. If no Google accounts
     * are registered on the device, a zero-length list is returned.
     */
    private List<String> getGoogleAccounts() {
        ArrayList<String> result = new ArrayList<String>();
        Account[] accounts = AccountManager.get(mContext).getAccounts();
        for (Account account : accounts) {
            if (account.type.equals("com.google")) {
                result.add(account.name);
            }
        }

        return result;
    }
}
