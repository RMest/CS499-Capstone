package com.example.rileymest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ScreenChangePassword extends AppCompatActivity {

    Button buttonChangePassword;
    TextView textCurrentPassword, textNewPassword, textNewPassword2;
    DatabaseUser db;

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

        db = new DatabaseUser(this);

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
            buttonChangePassword.setEnabled(userInput.length() >= 8 // activates buttons when all fields have values in them
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
        int userId = new SessionManager(this).getCurrentUserId();
        if (!textNewPassword.getText().toString().trim().equals(textNewPassword2.getText().toString().trim())) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textNewPassword.length() < 8 && textNewPassword2.length() < 8) { // password too short
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (textCurrentPassword.getText().toString().trim().equals(textNewPassword.getText().toString().trim())){
            Toast.makeText(this, "New password must not match old password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.updatePassword(userId, textCurrentPassword.getText().toString().trim(), textNewPassword2.getText().toString().trim())) {
            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
        else {
            Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show();
        }
    }

    public void buttonClose(View view) { // returns to previous menu
        setResult(RESULT_CANCELED);
        finish();
    }
}
