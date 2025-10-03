package com.example.rileymest;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ScreenAddItem extends AppCompatActivity {

    // initializations
    EditText item, qty;
    Button addItem;
    DatabaseInventory db;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_add_item);

        item = findViewById(R.id.editTextItemName);
        qty = findViewById(R.id.editTextQty);
        addItem = findViewById(R.id.buttonAddItem);
        db = new DatabaseInventory(this);

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

        int qtyVal;
        qtyVal = Integer.parseInt(qtyStr);

        long rowId = db.addItem(itemStr, qtyVal);
        if (rowId == -1) {
            Toast.makeText(this, "Item already exists.", Toast.LENGTH_SHORT).show();
            return;
        }

        setResult(RESULT_OK);
        finish();
    }

    public void buttonClose(View view) { // closes add item screen
        setResult(RESULT_CANCELED);
        finish();
    }
}
