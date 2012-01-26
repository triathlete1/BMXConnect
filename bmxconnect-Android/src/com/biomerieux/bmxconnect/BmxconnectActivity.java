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

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.biomerieux.bmxconnect.shared.rest.Result;
import com.biomerieux.bmxconnect.shared.rest.ResultList;

/**
 * Main activity - requests "Hello, World" messages from the server and provides
 * a menu item to invoke the accounts activity.
 */
public class BmxconnectActivity extends Activity {
	/**
     * Tag for logging.
     */
    private static final String TAG = "BmxconnectActivity";

    /**
     * The current context.
     */
    private Context mContext = this;

    private ResultsSynchronizer resultsSynchronizer;

    /**
     * A {@link BroadcastReceiver} to receive the response from a register or
     * unregister request, and to update the UI.
     */
    private final BroadcastReceiver mUpdateUIReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String accountName = intent.getStringExtra(DeviceRegistrar.ACCOUNT_NAME_EXTRA);
            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA,
                    DeviceRegistrar.ERROR_STATUS);
            String message = null;
            String connectionStatus = Util.DISCONNECTED;
            if (status == DeviceRegistrar.REGISTERED_STATUS) {
                message = getResources().getString(R.string.registration_succeeded);
                connectionStatus = Util.CONNECTED;
            } else if (status == DeviceRegistrar.UNREGISTERED_STATUS) {
                message = getResources().getString(R.string.unregistration_succeeded);
            } else {
                message = getResources().getString(R.string.registration_error);
            }

            // Set connection status
            SharedPreferences prefs = Util.getSharedPreferences(mContext);
            prefs.edit().putString(Util.CONNECTION_STATUS, connectionStatus).commit();

            // Display a notification
            Util.generateRegistrationNotification(mContext, String.format(message, accountName));
        }
    };

    /**
     * Begins the activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    	resultsSynchronizer = new ResultsSynchronizer();

        // Register a receiver to provide register/unregister notifications
        registerReceiver(mUpdateUIReceiver, new IntentFilter(Util.UPDATE_UI_INTENT));
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String connectionStatus = prefs.getString(Util.CONNECTION_STATUS, Util.DISCONNECTED);
        if (Util.DISCONNECTED.equals(connectionStatus)) {
            startActivity(new Intent(this, AccountsActivity.class));
        }
        setScreenContent(R.layout.hello_world);
    }

    /**
     * Shuts down the activity.
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateUIReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // Invoke the Register activity
        menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
//        menu.getItem(1).setIntent(new Intent(this, ResultsActivity.class));
        return super.onCreateOptionsMenu(menu);//return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();  
//    	ListView lv = (ListView) menuInfo.targetView.getParent();
//    	Context ctx = lv.getContext();

    	switch(item.getItemId()) {
        case R.id.refresh_menu_item :
    //TODO: refresh from server and show wait dialog
//        	final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
        	Toast.makeText(getApplicationContext(), "Refreshing data from the server...", 10).show();
//        	refreshResultsFromServer(helloWorld, null);
        	return true;
        default:
        	return super.onContextItemSelected(item);
    	}
	}

    // Manage UI Screens

	private void setHelloWorldScreenContent() {
        setContentView(R.layout.hello_world);

        final TextView helloWorld = (TextView) findViewById(R.id.hello_world);
        final Button resultsButton = (Button) findViewById(R.id.results_list);
        resultsButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View arg0) {
                Intent myIntent = new Intent(mContext, ResultsActivity.class);
                startActivityForResult(myIntent, 0);
			}
        });
        final Button sayHelloButton = (Button) findViewById(R.id.say_hello);
        sayHelloButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sayHelloButton.setEnabled(false);
                helloWorld.setText(R.string.contacting_server);

                refreshResultsFromServer(helloWorld, sayHelloButton);
            }
        });
    }
	
	private void refreshResultsFromServer(final TextView helloWorld, final Button buttonToEnableAfterRefresh) {
		// Use an AsyncTask to avoid blocking the UI thread
		new AsyncTask<Void, Void, String>() {
			private String message = "Result from Server:";
			
			@Override
			protected String doInBackground(Void... arg0) {
				try {
					ResultList results = resultsSynchronizer.readResultsViaRest(mContext);
					if (results == null) {
						message += "empty";
					}
					//TODO: re-work display to just be the result list - add a sync button to the menu and add results to a content provider
					for (Result result : results.getResults()) {
						message += "\n" + result;
					}
				} catch (Exception e) {
					message += Util.getStackTrace(e);
//                    		Toast.makeText(mContext, "Unexpected exception: " + trace, 15);
				}
				// Old RequestFactory hello world call
//                        MyRequestFactory requestFactory = Util.getRequestFactory(mContext,
//                                MyRequestFactory.class);
//                        final HelloWorldRequest request = requestFactory.helloWorldRequest();
//                        Log.i(TAG, "Sending request to server");
//                        request.getMessage().fire(new Receiver<String>() {
//                            @Override
//                            public void onFailure(ServerFailure error) {
//                                message = "Failure: " + error.getMessage();
//                            }
//
//                            @Override
//                            public void onSuccess(String result) {
//                                message = result;
//                            }
//                        });
				return message;
			}
			
			@Override
			protected void onPostExecute(String result) {
				if (null == result) {
					helloWorld.setText(result);
				} else {
					helloWorld.setText(result);
				}
				if (buttonToEnableAfterRefresh != null) {
					buttonToEnableAfterRefresh.setEnabled(true);
				}
			}
		}.execute();
	}

    /**
     * Sets the screen content based on the screen id.
     */
    private void setScreenContent(int screenId) {
        setContentView(screenId);
        switch (screenId) {
            case R.layout.hello_world:
                setHelloWorldScreenContent();
                break;
        }
    }
}
