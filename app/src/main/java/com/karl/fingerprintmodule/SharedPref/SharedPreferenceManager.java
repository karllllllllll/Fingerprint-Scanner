package com.karl.fingerprintmodule.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

import com.karl.fingerprintmodule.Session;

public class SharedPreferenceManager {

    private static final String SHARED_PREF_NAME = "kiosk_shared_preferences";

    private static final String SHARED_TIMEKEEPER_LOGGED_IN = "is_logged_in";
    private static final String KEY_SELECTED_LOCATION_ID = "selected_location_id";
    private static final String KEY_LOCATIONS = "locations_json_array";
    private static final String KEY_LINK = "link";
    private static final String KEY_DB = "db";
    private static final String KEY_TBL = "tbl";
    private static final String KEY_API_TOKEN = "api_token";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_PENDING_UPDATES = "pending_updates";

    private static SharedPreferenceManager mInstance;
    private Context mCtx;

    private SharedPreferenceManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPreferenceManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferenceManager(context);
        }
        return mInstance;
    }

    public void timekeeperLogin(Session session) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_LINK, session.getLink());
        editor.putString(KEY_API_TOKEN, session.getApi_token());
        editor.putString(KEY_TOKEN, session.getToken());
        editor.putString(KEY_DB, session.getD());
        editor.putString(KEY_TBL, session.getT());
        editor.putString(KEY_LOCATIONS, session.getLocations());
        editor.putString(KEY_SELECTED_LOCATION_ID, session.getSelectedLocationID());
        editor.putString(SHARED_TIMEKEEPER_LOGGED_IN, "1");
        editor.apply();
    }

    public void timekeeperLogout() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_LINK, "");
        editor.putString(KEY_API_TOKEN, "");
        editor.putString(KEY_TOKEN, "");
        editor.putString(KEY_DB, "");
        editor.putString(KEY_TBL, "");
        editor.putString(KEY_LOCATIONS, "");
        editor.putString(KEY_SELECTED_LOCATION_ID, "");
        editor.putString(SHARED_TIMEKEEPER_LOGGED_IN, "0");
        editor.apply();
    }

    public Boolean isTimekeeperLoggedIn(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        try {
            return sharedPreferences.getString(SHARED_TIMEKEEPER_LOGGED_IN, "").equals("1");
        }
        catch (Exception e) {
            return false;
        }
    }

    public Session getSessions() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        return new Session(
                sharedPreferences.getString(KEY_SELECTED_LOCATION_ID, ""),
                sharedPreferences.getString(KEY_LOCATIONS, ""),
                sharedPreferences.getString(KEY_LINK, ""),
                sharedPreferences.getString(KEY_DB, ""),
                sharedPreferences.getString(KEY_TBL, ""),
                sharedPreferences.getString(KEY_API_TOKEN, ""),
                sharedPreferences.getString(KEY_TOKEN, "")
        );
    }

    public void setKeySelectedLocationId(String loc_id) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_SELECTED_LOCATION_ID, loc_id);
        editor.apply();
    }

    public String getPendingUpdates() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        return sharedPreferences.getString(KEY_PENDING_UPDATES, "[]");
    }

    public void setPendingUpdates(String pendingUpdates) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_PENDING_UPDATES, pendingUpdates);
        editor.apply();
    }
}