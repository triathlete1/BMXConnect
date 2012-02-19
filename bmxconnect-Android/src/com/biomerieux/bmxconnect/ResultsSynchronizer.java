package com.biomerieux.bmxconnect;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.biomerieux.bmxconnect.shared.rest.ResultList;

public class ResultsSynchronizer {
	/**
     * Tag for logging.
     */
    private static final String TAG = "ResultsSynchronizer";
    
    private static final String REST_RESULTS_URI = "/rest/results";

    private final RegistrationHelper registrationHelper;
    private final ResultsDataManager resultsDataManager;

	public ResultsSynchronizer(final RegistrationHelper registrationHelper) {
    	this.registrationHelper = registrationHelper;
    	resultsDataManager = new ResultsDataManager();
    }

	public void readResultsViaRestAsync(AuthenticationCallback callback) {

    	// Make sure the auth cookie is current
    	registrationHelper.refreshAuthenticationToken(callback);
	}

	public ResultList readResultsViaRest() {

    	final Context mContext = registrationHelper.getmContext();
    	final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String authCookie = prefs.getString(Util.AUTH_COOKIE_RAW, null);
        if (authCookie == null) {
        	displayErrorResponseMessage("No authorization cookie found.", -1);
        	return null;
        }

        DefaultHttpClient httpClient = new DefaultHttpClient();
        BasicClientCookie c = new BasicClientCookie("SACSID", authCookie);
        try {
            c.setDomain(new URI(Setup.PROD_URL).getHost());
            httpClient.getCookieStore().addCookie(c);
        } catch (URISyntaxException e) {
        }
        
        String REST_URL = Util.getBaseUrl(mContext) + REST_RESULTS_URI;
        HttpGet request = new HttpGet(REST_URL);
        request.setHeader("Content-Type", "application/json;charset=UTF-8");
//        request.setHeader("Cookie", authCookie);
        
        HttpResponse httpResponse;
        int responseCode = -1;
        ResultList results = null;
        try {
            httpResponse = httpClient.execute(request);
            responseCode = httpResponse.getStatusLine().getStatusCode();
            String httpResponseString = "HTTP response: code=" + responseCode + ", reason="  + httpResponse.getStatusLine().getReasonPhrase();
            Log.i(TAG, httpResponseString);
            if (httpResponse.getStatusLine().getStatusCode() == 403) {
            	// Authentication failed, clear the cookie
            	Toast.makeText(mContext, "Authentication failed! Token is probably expired! " + httpResponseString, 10).show();
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putString(Util.ACCOUNT_NAME, accountName);
//                editor.remove(Util.AUTH_COOKIE);
//                editor.remove(Util.DEVICE_REGISTRATION_ID);
//                editor.commit();
//                mContext.startActivity(new Intent(mContext, AccountsActivity.class));
                return null;
            }
            HttpEntity entity = httpResponse.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                String jsonString = resultsDataManager.storeJsonDataInUserPrefs(prefs, instream);
                results = resultsDataManager.parseJsonResults(jsonString);

                // Closing the input stream will trigger connection release
                instream.close();
            }
        } catch (ClientProtocolException e)  {
        	handleHttpException(httpClient, responseCode, e);
        } catch (IOException e) {
        	handleHttpException(httpClient, responseCode, e);
        } catch (Exception e) {
        	String trace = Util.getStackTrace(e);
        	Log.e(TAG, "Unexpected exception: " + trace);
        	throw new RuntimeException(trace);
        }

        return results;
    }
    
    private void handleHttpException(HttpClient httpClient, int responseCode, Exception e) {
    	httpClient.getConnectionManager().shutdown();
    	displayErrorResponseMessage(e.toString(), responseCode);
    }
    
    private void displayErrorResponseMessage(Object message, int responseCode) {
    	Log.e(TAG, "Response code: " + responseCode + ". " + message.toString());
    	throw new RuntimeException("Response code: " + responseCode + ". " + message.toString());
    }
}
