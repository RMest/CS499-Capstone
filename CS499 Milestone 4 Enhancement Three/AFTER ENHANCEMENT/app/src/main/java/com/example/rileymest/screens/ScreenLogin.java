package com.example.rileymest.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.rileymest.R;
import com.example.rileymest.data.remote.MongoHelper;
import com.example.rileymest.util.SessionManager;

public class ScreenLogin extends AppCompatActivity {

    // initializations
    private static final String PREFS = "app_prefs";
    private static final String KEY_SMS_SETUP_DONE = "sms_setup_done";

    Button buttonLogin, buttonCreateAcc;
    EditText textUsername, textPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_login);

        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAcc = findViewById(R.id.buttonCreateAccount);

        textUsername = findViewById(R.id.editTextUsername);
        textPassword = findViewById(R.id.editTextPassword);

        textUsername.addTextChangedListener(textWatcher);
        textPassword.addTextChangedListener(textWatcher);

        buttonLogin.setEnabled(false);
        buttonCreateAcc.setEnabled(false);

    }

    private final TextWatcher textWatcher = new TextWatcher() { // always watches for text changes
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String userInput = textUsername.getText().toString().trim();
            String passInput = textPassword.getText().toString().trim();
            buttonLogin.setEnabled(!userInput.isEmpty() && passInput.length() >= 8);
            buttonCreateAcc.setEnabled(!userInput.isEmpty() && passInput.length() >= 8);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void buttonLogin(View view) { // attempts to log in user when button is clicked
        String user = textUsername.getText().toString().trim();
        String pass = textPassword.getText().toString().trim();

        boolean smsSetupDone = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getBoolean(KEY_SMS_SETUP_DONE, false);

        MongoHelper.get().validateUser(user, pass, (exists, err) -> runOnUiThread(() -> {
            if (err != null) {
                Toast.makeText(this, "Error checking database", Toast.LENGTH_SHORT).show();
                return;
            }
            if (exists != null && exists) {
                    MongoHelper.get().getUserId(user, (userId, err2) -> runOnUiThread(() -> {
                        if (err2 != null) {
                            Log.e("Mongo", "Error getting user id", err2);
                            return;
                        }
                        if (userId == null) {
                            Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                        } else {
                            SessionManager sm = new SessionManager(this);
                            sm.clear();
                            sm.setCurrentUserId(userId);

                            if (hasSmsPermission() || smsSetupDone) {
                                Toast.makeText(this, "Logging in as " + user, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ScreenMain.class));
                            } else {
                                Toast.makeText(this, "SMS permission setup required", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ScreenSMS.class));
                            }
                        }
                    }));
                } else {
                Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void buttonCreateAcc(View view) { // creates account for user
        String user = textUsername.getText().toString().trim();
        String pass = textPassword.getText().toString().trim();

        MongoHelper.get().stringExists(user, "users", (exists, err) -> runOnUiThread(() -> {
            if (err != null) {
                Toast.makeText(this, "Error checking database", Toast.LENGTH_SHORT).show();
                return;
            }
            if (exists != null && exists) {
                Toast.makeText(this, "Username taken.", Toast.LENGTH_SHORT).show();
            }
            if (pass.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            }
            else {
                MongoHelper.get().insertUser(user, pass, (id, insertErr) -> runOnUiThread(() -> {
                    if (insertErr != null) {
                        Toast.makeText(this, "Could not create account.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        }));
    }

    private boolean hasSmsPermission() { // checks for sms notifications
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
}
