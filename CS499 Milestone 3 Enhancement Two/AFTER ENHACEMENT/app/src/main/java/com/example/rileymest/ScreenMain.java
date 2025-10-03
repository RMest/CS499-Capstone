package com.example.rileymest;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScreenMain extends AppCompatActivity {

    private RecyclerView rv;
    private GridLayoutManager glm;
    private AdapterInventory adapter;
    private final List<InventoryItem> items = new ArrayList<>();
    private DatabaseInventory db;
    private ActivityResultLauncher<Intent> addItemLauncher;
    private static final String PREFS = "ui_prefs";
    private static final String KEY_SPAN = "grid_span";
    private static final int DEFAULT_SPAN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_main);

        db = new DatabaseInventory(this);

        addItemLauncher = registerForActivityResult( // popup for adding items
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if(result.getData() != null) {
                            String name = result.getData().getStringExtra("item");
                            int qty = result.getData().getIntExtra("qty", 0);
                            long rowId = db.addItem(name, qty);

                            if (rowId == -1) {
                                Toast.makeText(this, "Item already exists.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        loadFromDb();
                    }
                });

        adapter = new AdapterInventory(
                items,
                item -> { db.deleteById(item.id); loadFromDb(); },
                (item, newQty) -> { db.updateQuantity(item.id, newQty); }
        );

        int span = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SPAN, DEFAULT_SPAN);
        glm = new GridLayoutManager(this, span);
        rv = findViewById(R.id.recyclerViewInventory);
        rv.setLayoutManager(glm);
        rv.setAdapter(adapter);

        loadFromDb();
    }

    @Override
    protected void onResume() { // runs when coming back from different screen
        super.onResume();
        applySpanFromPrefs();
    }

    private void applySpanFromPrefs() { // applies new user grid settings
        int saved = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SPAN, DEFAULT_SPAN);
        int span = Math.max(1, saved);
        if (glm.getSpanCount() != span) {
            glm.setSpanCount(span);
            rv.requestLayout();
        }
    }

    public void buttonAddItem(View view) { // runs when user clicks button to add item.
        addItemLauncher.launch(new Intent(this, ScreenAddItem.class));
    }

    public void buttonOpenSettings(View view) { // runs when user clicks button to go to settings.
        startActivity(new Intent(this, ScreenSettings.class));
    }

    private void loadFromDb() { // loads the database information
        items.clear();
        try (Cursor c = db.getAll()) {
            int iId  = c.getColumnIndexOrThrow("_id");
            int iItm = c.getColumnIndexOrThrow("item");
            int iQty = c.getColumnIndexOrThrow("qty");
            while (c.moveToNext()) {
                long id = c.getLong(iId);
                String itm = c.getString(iItm);
                int qty = c.getInt(iQty);
                items.add(new InventoryItem(id, itm, qty));
            }
        }
        adapter.notifyDataSetChanged();
    }
}
