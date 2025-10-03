package com.example.rileymest.screens;

import com.example.rileymest.MyApp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rileymest.R;
import com.example.rileymest.data.remote.MongoHelper;

public class ScreenAddItem extends AppCompatActivity {

    // initializations
    EditText item, qty;
    Button addItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_add_item);

        item = findViewById(R.id.editTextItemName);
        qty = findViewById(R.id.editTextQty);
        addItem = findViewById(R.id.buttonAddItem);

        addItem.setEnabled(false);
        item.addTextChangedListener(textWatcher);
        qty.addTextChangedListener(textWatcher);
    }

    private final TextWatcher textWatcher = new TextWatcher() { // enables add button when both item and qty have values
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            String itemStr = item.getText().toString().trim();
            String qtyStr  = qty.getText().toString().trim();
            addItem.setEnabled(!itemStr.isEmpty() && !qtyStr.isEmpty());
        }
        @Override public void afterTextChanged(Editable s) { }
    };

    public void buttonAddItem(View view) { // adds item to database
        String itemStr = item.getText().toString().trim();
        String qtyStr  = qty.getText().toString().trim();
        MongoHelper.get().insertItem(itemStr, Integer.parseInt(qtyStr), (id, err) ->  {
            runOnUiThread(() -> {
            if (err != null) {
                Log.d("Mongo", itemStr + " added to database");
            } else {
                Log.d("Mongo", itemStr + " failed to added to database");
            }
        });
    });
        setResult(RESULT_OK);
        finish();
    }

    public void buttonClose(View view) { // closes add item screen
        setResult(RESULT_CANCELED);
        finish();
    }
}
