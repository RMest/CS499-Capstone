package com.example.rileymest.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rileymest.R;
import com.example.rileymest.data.remote.MongoHelper;
import com.example.rileymest.util.SessionManager;

public class ScreenChangePassword extends AppCompatActivity {

    Button buttonChangePassword;
    TextView textCurrentPassword, textNewPassword, textNewPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_change_password);

        buttonChangePassword = findViewById(R.id.buttonChangePassword);

        textCurrentPassword = findViewById(R.id.editTextCurrentPassword);
        textNewPassword = findViewById(R.id.editTextNewPassword);
        textNewPassword2 = findViewById(R.id.editTextNewPassword2);

        textCurrentPassword.addTextChangedListener(textWatcher);
        textNewPassword.addTextChangedListener(textWatcher);
        textNewPassword2.addTextChangedListener(textWatcher);

        buttonChangePassword.setEnabled(false);

    }

    private final TextWatcher textWatcher = new TextWatcher() { // always watches for text changes
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String userInput = textCurrentPassword.getText().toString().trim();
            String passInput = textNewPassword.getText().toString().trim();
            String passInput2 = textNewPassword2.getText().toString().trim();
            buttonChangePassword.setEnabled(userInput.length() >= 8
                    && passInput.length() >= 8
                    && passInput2.length() >= 8);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void buttonChangePassword(View view) { // button for changing the password
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    changePassword();
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

    public void changePassword() { // changes the password of current logged in user
        String idHex = new SessionManager(this).getCurrentUserId();
        String currentPass = textCurrentPassword.getText().toString().trim();
        String newPass = textNewPassword.getText().toString().trim();
        String newPass2 = textNewPassword2.getText().toString().trim();
        if (!newPass.equals(newPass2)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPass.equals(newPass)) {
            Toast.makeText(this, "New password must not match old password", Toast.LENGTH_SHORT).show();
            return;
        }
        MongoHelper.get().getUsernameFromId(idHex, (username, err) -> runOnUiThread(() -> {
            if (err != null || username == null) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }
            MongoHelper.get().validateUser(username, currentPass, (valid, err2) -> runOnUiThread(() -> {
                if (err2 != null || valid == null || !valid) {
                    Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }
                MongoHelper.get().updatePassword(idHex, newPass, (ok, err3) -> runOnUiThread(() -> {
                    if (err3 != null || ok == null || !ok) {
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }));
            }));
        }));
    }

    public void buttonClose(View view) { // returns to previous menu
        setResult(RESULT_CANCELED);
        finish();
    }
}
