package com.example.rileymest.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "auth_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) { // method for SessionManager
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void setCurrentUserId(String userIdHex) { // sets the current logged in user
        prefs.edit().putString(KEY_CURRENT_USER_ID, userIdHex).apply();
    }

    public String getCurrentUserId() { // gets the current logged in user
        return prefs.getString(KEY_CURRENT_USER_ID, null);
    }

    public void clear() { // clears user logged in data
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply();
    }
}
