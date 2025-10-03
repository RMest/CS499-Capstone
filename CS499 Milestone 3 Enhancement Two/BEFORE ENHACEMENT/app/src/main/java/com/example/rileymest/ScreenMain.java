package com.example.rileymest;

import android.content.Intent;
import android.database.Cursor;
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

public class ScreenName extends AppCompatActivity {

    // initializations
    private RecyclerView rv;
    private AdapaterInventory adapter;
    private final List<InventoryItem> items = new ArrayList<>();

    private DatabaseInventory db;

    private ActivityResultLauncher<Intent> addItemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_main);

        db = new DatabaseInventory(this);

        addItemLauncher = registerForActivityResult(
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

        rv = findViewById(R.id.recyclerViewInventory);
        rv.setLayoutManager(new GridLayoutManager(this, 3));
        rv.setHasFixedSize(true);

        adapter = new AdapaterInventory(
                items,
                item -> { db.deleteById(item.id); loadFromDb(); },
                (item, newQty) -> { db.updateQuantity(item.id, newQty); }
        );
        rv.setAdapter(adapter);

        loadFromDb();
    }

    public void buttonAddItem(View view) { // runs when user clicks button to add item.
        addItemLauncher.launch(new Intent(this, ScreenAddItem.class));
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
