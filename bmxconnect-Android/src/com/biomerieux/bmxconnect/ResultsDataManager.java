package com.biomerieux.bmxconnect;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.biomerieux.bmxconnect.shared.rest.Result;
import com.biomerieux.bmxconnect.shared.rest.ResultList;
import com.biomerieux.bmxconnect.shared.util.DateFormattingUtil;

public class ResultsDataManager {
	private static final String RESULT_DATA_PREFERENCES_KEY = "jsonResults";
	private static final String LAST_UPDATED_TIME_PREFERENCES_KEY = "lastUpdatedTime";

	/**
     * Tag for logging.
     */
    private static final String TAG = "ResultsDataManager";

	private ObjectMapper objectMapper;
	private final DateFormattingUtil dateFormattingUtil;

	public ResultsDataManager() {
        objectMapper = new ObjectMapper();
    	objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	dateFormattingUtil = new DateFormattingUtil();
	}

//  public void clearResultData(final SharedPreferences prefs) {
//		Editor editor = prefs.edit();
//		editor.putString(RESULT_DATA_PREFERENCES_KEY, null);
//		editor.commit();
//  }
	
	public String readLastUpdatedTime(final SharedPreferences prefs) {
		return prefs.getString(LAST_UPDATED_TIME_PREFERENCES_KEY, null);
	}

	private void storeJsonResults(final SharedPreferences prefs, String jsonString) {
		Editor editor = prefs.edit();
		editor.putString(RESULT_DATA_PREFERENCES_KEY, jsonString);
		editor.putString(LAST_UPDATED_TIME_PREFERENCES_KEY, dateFormattingUtil.formatDateTime(new Date()));
		editor.commit();
	}

	public void addNewResult(final SharedPreferences prefs, Result result) {
		ResultList results = readSavedResults(prefs);
		results.getResults().add(0, result);
		String jsonString = createJsonResultString(results);
		storeJsonResults(prefs, jsonString);
	}
	
	public String storeJsonDataInUserPrefs(final SharedPreferences prefs, InputStream instream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(instream, writer, "UTF-8");
		String jsonString = writer.toString();
		
		//TODO: check for html = auth failure
		
		storeJsonResults(prefs, jsonString);
		return jsonString;
	}
	
	public ResultList readSavedResults(final SharedPreferences prefs) {
		String resultString = prefs.getString(RESULT_DATA_PREFERENCES_KEY, null);
		ResultList results = new ResultList();
  	if (null != resultString) {
  		results = parseJsonResults(resultString);
  	}
		return results;
	}

	public ResultList parseJsonResults(String resultString) {
		try {
			ResultList results = objectMapper.readValue(resultString, ResultList.class);
			return results;
		} catch (Exception e) {
			Log.e(TAG, "Unexpected exception reading JSON result data: " + Util.getStackTrace(e));
		}
		return new ResultList();
	}

	public String createJsonResultString(ResultList results) {
		try {
			String resultString = objectMapper.writeValueAsString(results);
			return resultString;
		} catch (Exception e) {
			Log.e(TAG, "Unexpected exception reading JSON result data: " + Util.getStackTrace(e));
		}
		return null;
	}
}