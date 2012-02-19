package com.biomerieux.bmxconnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.biomerieux.bmxconnect.shared.rest.Result;
import com.biomerieux.bmxconnect.shared.rest.ResultList;
import com.biomerieux.bmxconnect.shared.util.DateFormattingUtil;

/**
 * Class refreshes data from the server.
 */
public class ResultsActivity extends ListActivity implements ResultSyncCallback {

    private Context mContext = this;

    private ObjectMapper objectMapper;

    private ResultsSynchronizer resultsSynchronizer;
    private ResultsDataManager resultsDataManager;
    private RegistrationHelper registrationHelper;
    private DateFormattingUtil dateFormattingUtil;
    private ProgressDialog progressDialog;
    private List<Map<String, String>> resultTextList;

    private TextView lastUpdatedTime;
    
    /**
     * A {@link BroadcastReceiver} to receive the response from a register or
     * unregister request, and to update the UI.
     */
    private final BroadcastReceiver mUpdateUIAfterRegistrationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String accountName = intent.getStringExtra(DeviceRegistrar.ACCOUNT_NAME_EXTRA);
            int status = intent.getIntExtra(DeviceRegistrar.STATUS_EXTRA, DeviceRegistrar.ERROR_STATUS);
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
            
            // Pull the current data if just registered
            if (connectionStatus == Util.CONNECTED) {
    			refreshDataFromServerAsync();            	
            }
        }
    };

    private final BroadcastReceiver mUpdateUIAfterMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	refreshDisplayData();
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dateFormattingUtil = new DateFormattingUtil();
		registrationHelper = new RegistrationHelper(this);
		resultsSynchronizer = new ResultsSynchronizer(registrationHelper);
	    resultsDataManager = new ResultsDataManager();
	    
        objectMapper = new ObjectMapper();
    	objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		setContentView(R.layout.result_list);
		
		lastUpdatedTime = (TextView) findViewById(R.id.lastUpdateTextView);

		//Make a data list
		resultTextList = new ArrayList<Map<String, String>>();
		refreshDisplayData();
		
		// Register a receiver to provide register/unregister notifications
		registerReceiver(mUpdateUIAfterRegistrationReceiver, new IntentFilter(Util.UPDATE_UI_WITH_REGISTRATION_CHANGE_INTENT));
		registerReceiver(mUpdateUIAfterMessageReceiver, new IntentFilter(Util.UPDATE_UI_ON_MESSAGE_INTENT));
		
		registerForContextMenu(getListView());
		
		//Get the listView ( from: ListActivity )
        final ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView parent, View view, int position, long id) {
        		@SuppressWarnings("unchecked")
				Map<String, String> o = (Map<String, String>) lv.getItemAtPosition(position);
        		Toast.makeText(ResultsActivity.this, "Item with timestamp '" + o.get("timestamp") + "' was clicked.", Toast.LENGTH_LONG).show(); 
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String connectionStatus = prefs.getString(Util.CONNECTION_STATUS, Util.DISCONNECTED);
        if (Util.DISCONNECTED.equals(connectionStatus)) {
            startActivity(new Intent(this, AccountsActivity.class));
        }
	}

    @Override
    public void onDestroy() {
        unregisterReceiver(mUpdateUIAfterRegistrationReceiver);
        super.onDestroy();
    }

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		refreshDisplayData();
		super.onRestoreInstanceState(state);
	}

	private void refreshDisplayData() {
		extractResultDisplayMap();
		
		//Make a new listadapter
        ListAdapter adapter = new SimpleAdapter(this, resultTextList , R.layout.result_list_item,
		                        new String[] { "result", "timestamp" },
		                        new int[] { R.id.list_text_1, R.id.list_text_2 });
		setListAdapter(adapter);

		displayLastUpdatedTime();
	}	

	private List<Map<String, String>> extractResultDisplayMap() {
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        try {
        	ResultList results = resultsDataManager.readSavedResults(prefs);
        	resultTextList.clear(); // clear previous values
			for (Result result : results.getResults()) {
		    	Map<String, String> resultText = new HashMap<String, String>();
				resultText.put("result", result.getResult());
				String dateString = dateFormattingUtil.formatDateTime(result.getResultDate());
				resultText.put("timestamp", dateString);
				resultTextList.add(resultText);
			}
		} catch (Exception e) {
			Toast.makeText(mContext, "Unexpected exception: " + Util.getStackTrace(e), 15).show();
		}
		return resultTextList;
	}

	private void displayLastUpdatedTime() {
		final SharedPreferences prefs = Util.getSharedPreferences(mContext);
		String lastUpdate = resultsDataManager.readLastUpdatedTime(prefs);
		lastUpdatedTime.setText("Last Updated: \n" + lastUpdate);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        // Invoke the Register activity
        menu.getItem(0).setIntent(new Intent(this, AccountsActivity.class));
//        menu.getItem(1).setIntent(new Intent(this, ResultsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
        case R.id.refresh_menu_item :
			refreshDataFromServerAsync();
        	return true;
        default:
//        	Toast.makeText(context, "Some menu item selected: " + item.getTitle(), 10).show();
        	return super.onContextItemSelected(item);
    	}
	}

	private void refreshDataFromServerAsync() {
	    progressDialog = ProgressDialog.show(ResultsActivity.this, "", "Synchronizing. Please wait...", true);

	    // Use an AsyncTask to avoid blocking the UI thread
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... arg0) {
		    	try {
		    		// Register this activity as the authentication callback handler (see onAuthenticationComplete() below)
		    		resultsSynchronizer.readResultsViaRestAsync((ResultSyncCallback)mContext);
				} catch(Exception e) {
		    		Toast.makeText(mContext, "ERROR: refreshing data from the server failed!" + e.getMessage(), 20).show();
					return e.getMessage();
		    	}
				return null;
			}
		}.execute();
	}
	
    @Override
	public void onSynchronizationComplete(String result) {
		if (result != null) {
			Toast.makeText(mContext, "ERROR: refreshing data from the server failed!" + result, 20).show();
		}
		else {
			refreshDisplayData();
		}
		
		// Remove wait cursor
		progressDialog.dismiss();
		
//		if (buttonToEnableAfterRefresh != null) {
//			buttonToEnableAfterRefresh.setEnabled(true);
	}
}
