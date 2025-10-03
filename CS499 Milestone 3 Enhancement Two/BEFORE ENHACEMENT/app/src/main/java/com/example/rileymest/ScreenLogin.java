package com.example.rileymest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
            buttonLogin.setEnabled(!userInput.isEmpty() && !passInput.isEmpty()); // activates buttons when both fields have values in them
            buttonCreateAcc.setEnabled(!userInput.isEmpty() && !passInput.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    DatabaseUser db = new DatabaseUser(this);

    public void buttonLogin(View view) { // runs when user clicks login button
        String userInput = textUsername.getText().toString().trim();
        String passInput = textPassword.getText().toString().trim();

        boolean smsSetupDone = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getBoolean(KEY_SMS_SETUP_DONE, false);

        if(db.validateLogin(userInput, passInput)) { // checks if user and password is stored in database
            Toast.makeText(this, "Logging In", Toast.LENGTH_SHORT).show();
            if(hasSmsPermission() || smsSetupDone) { // runs if user has either been through sms setup or if sms is already enabled
                startActivity(new Intent(this, ScreenName.class));
            }
            else{ // else go through setup
                startActivity(new Intent(this, ScreenSMS.class));
            }
        }
        else { // else incorrect information
            Toast.makeText(this, "Invalid Username or Password",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonCreateAcc(View view) { // runs when user clicks create account button
        String user = textUsername.getText().toString().trim();
        String pass = textPassword.getText().toString().trim();

        if (db.userExist(user)) { // username already in database
            Toast.makeText(this, "Username taken.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 8) { // password too short
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.addUser(user, pass);
        if (id != -1) { // creates account
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
        } else { // if for some reason account count be created
            Toast.makeText(this, "Could not create account.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasSmsPermission() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }
}
