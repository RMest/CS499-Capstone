package com.example.rileymest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rileymest.data.remote.MongoHelper;
import com.example.rileymest.screens.ScreenMain;
import com.example.rileymest.screens.ScreenLogin;
import com.example.rileymest.util.SessionManager;

public class Launcher extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userId = new SessionManager(this).getCurrentUserId();

        MongoHelper.get().getUsernameFromId(userId, (username, err) -> runOnUiThread(() -> {
            if (err != null) {
                Log.e("Mongo", "No user logged in", err);
                startActivity(new Intent(this, ScreenLogin.class));
            } else {
                Toast.makeText(this, "Logged in as " + username, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, ScreenMain.class));
            }
        }));
    }
}
