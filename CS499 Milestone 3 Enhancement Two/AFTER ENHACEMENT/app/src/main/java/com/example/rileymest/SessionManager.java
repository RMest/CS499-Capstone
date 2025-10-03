package com.example.rileymest;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "auth_prefs";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context ctx) { // method for SessionManager
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void setCurrentUserId(int userId) { // sets the current logged in user
        prefs.edit().putInt(KEY_CURRENT_USER_ID, userId).apply();
    }

    public int getCurrentUserId() { // gets the current logged in user
        return prefs.getInt(KEY_CURRENT_USER_ID, -1); // -1 means “nobody”
    }

    public void clear() { // clears user logged in data
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply();
    }
}
