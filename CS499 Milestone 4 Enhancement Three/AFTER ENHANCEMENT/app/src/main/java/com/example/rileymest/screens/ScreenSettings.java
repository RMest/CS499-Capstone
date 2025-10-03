package com.example.rileymest.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rileymest.R;
import com.example.rileymest.data.remote.MongoHelper;
import com.example.rileymest.util.SessionManager;

public class ScreenSettings extends AppCompatActivity {

    private static final String UI_PREFS = "ui_prefs";
    private static final String KEY_GRID_SPAN = "grid_span";

    TextView textUsername, textGridNumber;
    Button buttonLogOut;
    SeekBar seekBar;
    private ActivityResultLauncher<Intent> changePasswordLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_settings); // sets content view to settings screen

        textUsername = findViewById(R.id.textUsernameSettings);
        textGridNumber = findViewById(R.id.textGridNumber);
        buttonLogOut = findViewById(R.id.buttonLogOut);
        seekBar = findViewById(R.id.seekBarGridAmount);

        String userId = new SessionManager(this).getCurrentUserId(); // gets current user logged in

        MongoHelper.get().getUsernameFromId(userId, (username, err) -> runOnUiThread(() -> {
            if (err != null) {
                Log.e("Mongo", "No user logged in", err);
            } else {
                textUsername.setText(username);
            }
        }));

        SharedPreferences prefs = getSharedPreferences(UI_PREFS, MODE_PRIVATE);

        int savedSpan = prefs.getInt(KEY_GRID_SPAN, 3); // gets info from shared prefs on grid data
        savedSpan = Math.max(1, Math.min(5, savedSpan));
        seekBar.setMax(5);
        seekBar.setProgress(savedSpan);

        textGridNumber.setText(String.valueOf(savedSpan));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() { // slider for grid column amount
            @Override
            public void onProgressChanged(SeekBar s, int value, boolean fromUser) {
                int span = Math.max(1, value);
                textGridNumber.setText(String.valueOf(span));

                prefs.edit().putInt(KEY_GRID_SPAN, span).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {
            }
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        changePasswordLauncher = registerForActivityResult( // popup for changing password
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        boolean updated = result.getData().getBooleanExtra("password_updated", false);
                        Toast.makeText(this, updated ? "Password updated" : "No change", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    public void buttonGoToSettings(View view) { // button for going into device settings
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(intent);
    }

    public void buttonCloseSettings(View view) { // button for going back to main screen
        finish();
    } // button for closing the settings menu

    public void buttonChangePassword(View view) { // button for changing password
        changePasswordLauncher.launch(new Intent(this, ScreenChangePassword.class));
    }

    public void buttonLogOut(View view) { // button for logging the user out

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        logout();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void logout(){ // used to log user out of the application
        new SessionManager(this).clear();
        Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, ScreenLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

}

