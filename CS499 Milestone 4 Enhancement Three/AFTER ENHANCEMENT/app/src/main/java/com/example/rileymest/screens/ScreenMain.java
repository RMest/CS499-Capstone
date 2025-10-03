package com.example.rileymest.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rileymest.data.remote.MongoHelper;
import com.example.rileymest.ui.AdapterInventory;
import com.example.rileymest.data.model.InventoryItem;
import com.example.rileymest.R;

import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ScreenMain extends AppCompatActivity {

    private RecyclerView rv;
    private GridLayoutManager glm;
    private AdapterInventory adapter;
    private final List<InventoryItem> items = new ArrayList<>();
    private ActivityResultLauncher<Intent> addItemLauncher;
    private static final String PREFS = "ui_prefs";
    private static final String KEY_SPAN = "grid_span";
    private static final int DEFAULT_SPAN = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // ran when running java file
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_main);

        addItemLauncher = registerForActivityResult( // launcher for add item popup
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("item");
                        int qty = result.getData().getIntExtra("qty", 0);

                        MongoHelper.get().insertItem(name, qty, (id, err) -> runOnUiThread(() -> {
                            if (err != null) {
                                Toast.makeText(ScreenMain.this, "Item failed to insert", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }));
                    }
                }
        );

        adapter = new AdapterInventory( // adapter for deleting and changing quantities in database & list
            items,
            item -> {
                MongoHelper.get().delete(item.idHex, "items", (ok, err) -> {
                    runOnUiThread(() -> {
                        if (ok != null && ok) {
                            int pos = indexOfId(items, item.idHex);
                            if (pos >= 0) {
                                items.remove(pos);
                                adapter.notifyItemRemoved(pos);
                            }
                        } else {
                            Log.e("Mongo", "Delete failed", err);
                        }
                    });
                });
            },
            (item, newQty) -> {
                MongoHelper.get().updateQty(item.idHex, newQty, (ok, err) -> {
                    runOnUiThread(() -> {
                        if (ok != null && ok) {
                            item.qty = newQty;
                            int pos = indexOfId(items, item.idHex);
                            if (pos >= 0) adapter.notifyItemChanged(pos);
                        } else {
                            Log.e("Mongo", "Update failed", err);
                        }
                    });
                });
            }
        );

        int span = getSharedPreferences(PREFS, MODE_PRIVATE).getInt(KEY_SPAN, DEFAULT_SPAN);
        glm = new GridLayoutManager(this, span);
        rv = findViewById(R.id.recyclerViewInventory);
        rv.setLayoutManager(glm);
        rv.setAdapter(adapter);

    }

    @Override
    protected void onResume() { // runs when coming back from different screen
        super.onResume();
        loadFromDb();
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

    private int indexOfId(List<InventoryItem> list, String idHex) { // gets current location of database item in list array
        for (int i = 0; i < list.size(); i++) {
            if (idHex.equals(list.get(i).idHex)) {
                return i;
            }
        }
        return -1;
    }

    public void buttonAddItem(View view) { // runs when user clicks button to add item.
        addItemLauncher.launch(new Intent(this, ScreenAddItem.class));
    }

    public void buttonOpenSettings(View view) { // runs when user clicks button to go to settings.
        startActivity(new Intent(this, ScreenSettings.class));
    }

     private void loadFromDb() { // loads the database information from MongoDB
         MongoHelper.get().listItems((docs, err) -> runOnUiThread(() -> {
             if (err != null) {
                 Log.e("Mongo", "list failed", err);
                 return;
             }
             items.clear();
         for (Document d : docs) {
             InventoryItem it = new InventoryItem();
             it.idHex = d.getObjectId("_id").toHexString();
             it.name  = d.getString("name");
             it.qty   = d.getInteger("qty", 0);
             items.add(it);
         }
         adapter.notifyDataSetChanged();
     }));
    }
}
