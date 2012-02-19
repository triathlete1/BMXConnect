package com.biomerieux.bmxconnect;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;

import com.google.android.c2dm.C2DMessaging;

public class RegistrationHelper {
    /**
     * Cookie name for authorization.
     */
    private static final String AUTH_COOKIE_NAME = "SACSID";

    /**
     * Tag for logging.
     */
    private static final String TAG = "RegistrationHelper";

    /**
	 * The current context.
	 */
    private final Context mContext;

	private final Activity activity;
	
	public RegistrationHelper(final Activity activity) {
		this.activity = activity;
		this.mContext = activity;
	}
	
    public Context getmContext() {
		return mContext;
	}

	/**
     * Registers for C2DM messaging with the given account name.
     * 
     * @param accountName a String containing a Google account name
     */
    public void register(final String accountName, final AuthenticationCallback callback) {
        // Store the account name in shared preferences
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Util.ACCOUNT_NAME, accountName);
//        editor.remove(Util.AUTH_COOKIE);
//        editor.remove(Util.AUTH_COOKIE_RAW);
        editor.remove(Util.DEVICE_REGISTRATION_ID);
        editor.commit();

        // Obtain an auth token and register
        refreshAuthenticationToken(callback);

        //TODO: remove old code
//        final AccountManager mgr = AccountManager.get(mContext);
//        Account[] accts = mgr.getAccountsByType("com.google");
//        for (Account acct : accts) {
//            final Account account = acct;
//            if (account.name.equals(accountName)) {
//                if (Util.isDebug(mContext)) {
//                    // Use a fake cookie for the dev mode app engine server
//                    // The cookie has the form email:isAdmin:userId
//                    // We set the userId to be the same as the email
//                    String authCookie = accountName + ":false:" + accountName;
//                    prefs.edit().putString(Util.AUTH_COOKIE, "dev_appserver_login=" + authCookie).commit();
//                    prefs.edit().putString(Util.AUTH_COOKIE_RAW, authCookie).commit();
//                    C2DMessaging.register(mContext, Setup.SENDER_ID);
//                } else {
//                    // Get the auth token from the AccountManager and convert
//                    // it into a cookie for the appengine server
////                    final Activity activity = this;
//                    mgr.getAuthToken(account, "ah", null, activity, new AccountManagerCallback<Bundle>() {
//                        public void run(AccountManagerFuture<Bundle> future) {
//                            String authToken = getAuthToken(future);
//                            // Ensure the token is not expired by invalidating it and
//                            // obtaining a new one
//                            mgr.invalidateAuthToken(account.type, authToken);
//                            mgr.getAuthToken(account, "ah", null, activity, new AccountManagerCallback<Bundle>() {
//                                public void run(AccountManagerFuture<Bundle> future) {
//                                    String authToken = getAuthToken(future);
//                                    // Convert the token into a cookie for future use
//                                    String authCookie = getAuthCookie(authToken);
//                                    Editor editor = prefs.edit();
//                                    editor.putString(Util.AUTH_COOKIE, AUTH_COOKIE_NAME + "=" + authCookie);
//                                    editor.putString(Util.AUTH_COOKIE_RAW, authCookie);
//                                    editor.commit();
//                                    C2DMessaging.register(mContext, Setup.SENDER_ID);
//                                }
//                            }, null);
//                        }
//                    }, null);
//                }
//                break;
//            }
//        }
    }

    public void refreshAuthenticationToken(final AuthenticationCallback callback, final Object... params) {
        final SharedPreferences prefs = Util.getSharedPreferences(mContext);
        String accountName = prefs.getString(Util.ACCOUNT_NAME, null);
        if (null == accountName) {
        	Log.w(TAG, "Account name is null");
        	return;
        }

        // Store the tokens in shared preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(Util.AUTH_COOKIE);
        editor.remove(Util.AUTH_COOKIE_RAW);
        editor.commit();

        final AccountManager mgr = AccountManager.get(mContext);
        Account[] accts = mgr.getAccountsByType("com.google");
        for (Account acct : accts) {
            final Account account = acct;
            if (account.name.equals(accountName)) {
                if (Util.isDebug(mContext)) {
                    // Use a fake cookie for the dev mode app engine server
                    // The cookie has the form email:isAdmin:userId
                    // We set the userId to be the same as the email
                    String authCookie = accountName + ":false:" + accountName;
                    prefs.edit().putString(Util.AUTH_COOKIE, "dev_appserver_login=" + authCookie).commit();
                    prefs.edit().putString(Util.AUTH_COOKIE_RAW, authCookie).commit();
                    callback.onAuthenticationComplete(params);
//                    C2DMessaging.register(mContext, Setup.SENDER_ID);
                } else {
                    // Get the auth token from the AccountManager and convert
                    // it into a cookie for the appengine server
//                    final Activity activity = this;
                    mgr.getAuthToken(account, "ah", null, activity, new AccountManagerCallback<Bundle>() {
                        public void run(AccountManagerFuture<Bundle> future) {
                            String authToken = getAuthToken(future);
                            // Ensure the token is not expired by invalidating it and
                            // obtaining a new one
                            mgr.invalidateAuthToken(account.type, authToken);
                            mgr.getAuthToken(account, "ah", null, activity, new AccountManagerCallback<Bundle>() {
                                public void run(AccountManagerFuture<Bundle> future) {
                                    String authToken = getAuthToken(future);
                                    // Convert the token into a cookie for future use
                                    String authCookie = getAuthCookie(authToken);
                                    Editor editor = prefs.edit();
                                    editor.putString(Util.AUTH_COOKIE, AUTH_COOKIE_NAME + "=" + authCookie);
                                    editor.putString(Util.AUTH_COOKIE_RAW, authCookie);
                                    editor.commit();
//                                    C2DMessaging.register(mContext, Setup.SENDER_ID);
                                    callback.onAuthenticationComplete(params);
                                }
                            }, null);
                        }
                    }, null);
                }
                break;
            }
        }
    }
    
    private String getAuthToken(AccountManagerFuture<Bundle> future) {
        try {
            Bundle authTokenBundle = future.getResult();
            String authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();
            return authToken;
        } catch (Exception e) {
            Log.w(TAG, "Got Exception " + e);
            return null;
        }
    }

    // Utility Methods

    /**
     * Retrieves the authorization cookie associated with the given token. This
     * method should only be used when running against a production appengine
     * backend (as opposed to a dev mode server).
     */
    private String getAuthCookie(String authToken) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            // Get SACSID cookie
            httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
            String uri = Setup.PROD_URL + "/_ah/login?continue=http://localhost/&auth=" + authToken;
            HttpGet method = new HttpGet(uri);

            HttpResponse res = httpClient.execute(method);
            StatusLine statusLine = res.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            Header[] headers = res.getHeaders("Set-Cookie");
            if (statusCode != 302 || headers.length == 0) {
                return null;
            }

            for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
                if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Got IOException " + e);
            Log.w(TAG, Log.getStackTraceString(e));
        } finally {
            httpClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
        }

        return null;
    }
}