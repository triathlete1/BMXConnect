package com.biomerieux.bmxconnect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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

/**
 * Class refreshes data from the server.
 */
public class ResultsActivity extends ListActivity {

    private Context mContext = this;

    private ObjectMapper objectMapper;

    private ResultsSynchronizer resultsSynchronizer;
    
    private List<Map<String, String>> resultTextList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resultsSynchronizer = new ResultsSynchronizer();
		
        objectMapper = new ObjectMapper();
    	objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		setContentView(R.layout.result_list);

		//Make a data list
		resultTextList = new ArrayList<Map<String, String>>();
		refreshDisplayData();
		
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
	protected void onRestoreInstanceState(Bundle state) {
		refreshDisplayData();
		super.onRestoreInstanceState(state);
	}

	private void refreshDisplayData() {
		extractResultDisplayMap(resultTextList);
		
		//Make a new listadapter
        ListAdapter adapter = new SimpleAdapter(this, resultTextList , R.layout.result_list_item,
		                        new String[] { "result", "timestamp" },
		                        new int[] { R.id.list_text_1, R.id.list_text_2 });
		setListAdapter(adapter);
	}	

	private List<Map<String, String>> extractResultDisplayMap(List<Map<String, String>> resultTextList) {
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        try {
        	ResultList results = resultsSynchronizer.readSavedResults(prefs);
        	resultTextList.clear(); // clear previous values
			for (Result result : results.getResults()) {
		    	Map<String, String> resultText = new HashMap<String, String>();
				resultText.put("result", result.getResult());
				resultText.put("timestamp", result.getResultDateString());
				resultTextList.add(resultText);
			}
//		} catch (JsonParseException e) {
//			Toast.makeText(mContext, "Unexpected exception: " + Util.getStackTrace(e), 15);
//		} catch (JsonMappingException e) {
//			Toast.makeText(mContext, "Unexpected exception: " + Util.getStackTrace(e), 15);
//		} catch (IOException e) {
//			Toast.makeText(mContext, "Unexpected exception: " + Util.getStackTrace(e), 15);
		} catch (Exception e) {
			Toast.makeText(mContext, "Unexpected exception: " + Util.getStackTrace(e), 15).show();
		}
		return resultTextList;
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
    	Context context = getApplicationContext();
    	
    	switch(item.getItemId()) {
        case R.id.refresh_menu_item :
	    	Toast.makeText(context, "Refreshing data from the server.  This may take a few minutes.", 20).show();
			refreshDataFromServerAsync();
        	return true;
        default:
//        	Toast.makeText(context, "Some menu item selected: " + item.getTitle(), 10).show();
        	return super.onContextItemSelected(item);
    	}
	}

	private void refreshDataFromServerAsync() {
		// Use an AsyncTask to avoid blocking the UI thread
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... arg0) {
				//TODO: refresh from server and show wait dialog - possibly optimize the refresh using the count/date of last entry?
//		    	Toast.makeText(mContext, "Refreshing data from the server.  This may take a few minutes.", 20).show();
		    	try {
		    		resultsSynchronizer.readResultsViaRest(mContext);
				} catch(Exception e) {
//		    		Toast.makeText(mContext, "ERROR: refreshing data from the server failed!" + e.getMessage(), 20).show();
					return e.getMessage();
		    	}
				return null;
			}
			
			@Override
			protected void onPostExecute(String result) {
				//TBD
				if (null != result) {
					Toast.makeText(mContext, "ERROR: refreshing data from the server failed!" + result, 20).show();
				} else {
		    		//Make a data list
		    		SimpleAdapter adapter = (SimpleAdapter)getListAdapter();
		    		resultTextList = new ArrayList<Map<String, String>>();
		    		extractResultDisplayMap(resultTextList);
		    		adapter.notifyDataSetChanged();
//TODO: remove
//        			ListAdapter adapter = new SimpleAdapter(listActivity, resultTextList , R.layout.result_list_item,
//        		    	                    new String[] { "result", "timestamp" },
//        		        	                new int[] { R.id.list_text_1, R.id.list_text_2 });
//        			listActivity.setListAdapter(adapter);
				}
//				if (buttonToEnableAfterRefresh != null) {
//					buttonToEnableAfterRefresh.setEnabled(true);
//				}
			}
		}.execute();
	}
}
