package com.example.rileymest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Launcher extends AppCompatActivity {

    DatabaseUser db = new DatabaseUser(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int userId = new SessionManager(this).getCurrentUserId();
        if (userId > 0) { // checks if user is logged in or not
            Toast.makeText(this, "Logged in as " + db.getUsernameById(userId), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ScreenMain.class));
        }
        else {
            startActivity(new Intent(this, ScreenLogin.class));
        }
    }
}
