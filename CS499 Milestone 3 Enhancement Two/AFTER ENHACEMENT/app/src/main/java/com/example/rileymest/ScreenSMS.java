package com.example.rileymest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ScreenSMS extends AppCompatActivity {

    // initializations
    private static final int REQ_SEND_SMS = 1001;

    private static final String PREFS = "app_prefs";
    private static final String KEY_SMS_SETUP_DONE = "sms_setup_done";
    Button buttonEnableSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_sms);

        buttonEnableSMS = findViewById(R.id.buttonEnableSMS);
    }

    public void buttonEnableSMS(View view) { // ran when user clicks enable sms button
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQ_SEND_SMS);
        }
    }

    public void buttonSkip(View view) { // // ran when user clicks the skip button

        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_SMS_SETUP_DONE, true).apply();
        startActivity(new Intent(this, ScreenMain.class));
    }

    private void sendSms(String phone) { // sends user a test sms
        try {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) { // if user does not have sms features
                Toast.makeText(this, "Cannot send SMS", Toast.LENGTH_LONG).show();
                return;
            }

            SmsManager smsManager;
            smsManager = getSystemService(SmsManager.class);

            String message = "Inventory changes will now be sent through SMS.";
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(this, "SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_LONG).show();
        }
    }

    // determines user input for sms permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { // if user enables sms
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(KEY_SMS_SETUP_DONE, true).apply();
                String phone = "5551234567";
                sendSms(phone);
                startActivity(new Intent(this, ScreenMain.class));
            } else { // else if user does not allow sms
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

