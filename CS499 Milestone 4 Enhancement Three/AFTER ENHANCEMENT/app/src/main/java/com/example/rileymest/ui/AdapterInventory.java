package com.example.rileymest.ui;

import static java.lang.Integer.parseInt;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rileymest.R;
import com.example.rileymest.data.model.InventoryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdapterInventory extends RecyclerView.Adapter<AdapterInventory.VH> {

    public interface OnDelete { void delete(InventoryItem item); }
    public interface OnChangeQty { void change(InventoryItem item, int newQty); }
    private final List<InventoryItem> data;
    private final OnDelete onDelete;
    private final OnChangeQty onChangeQty;

    public AdapterInventory(List<InventoryItem> data, OnDelete onDelete, OnChangeQty onChangeQty) {
        this.data = data;
        this.onDelete = onDelete;
        this.onChangeQty = onChangeQty;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // sets the layout that should be displayed per item
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_inventory_list, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) { // responsible for all item box functionality
        InventoryItem item = data.get(position);
        h.boundItem = item;

        h.textItemName.setText(item.name);

        if (h.qtyWatcher != null) h.textQtyAmount.removeTextChangedListener(h.qtyWatcher);

        String qtyText = String.valueOf(item.qty);
        if (!qtyText.equals(h.textQtyAmount.getText().toString())) {
            h.textQtyAmount.setText(qtyText);
            h.textQtyAmount.setSelection(qtyText.length());
        }

        h.textQtyAmount.setOnFocusChangeListener((v, hasFocus) -> { // checks if user doesn't have a box focused
            if (!hasFocus && h.boundItem != null) {
                int newQty;
                try { newQty = parseInt(h.textQtyAmount.getText().toString()); }
                catch (NumberFormatException e) { newQty = 0; }
                if (newQty != h.boundItem.qty) onChangeQty.change(h.boundItem, newQty);
            }
        });

        h.qtyWatcher = new SimpleWatcher() { // text watcher for quantity amount
            @Override public void afterTextChanged(Editable s) {
                if (h.boundItem == null) return;
                if (!h.textQtyAmount.hasFocus()) return;
            }
        };

        h.textQtyAmount.addTextChangedListener(h.qtyWatcher);

        h.textQtyAmount.setOnFocusChangeListener((v, hasFocus) -> { // checks if user has a box focused
            if (hasFocus || h.boundItem == null) return;
            int newQty = parseInt(((EditText) v).getText().toString());
            if (newQty != h.boundItem.qty) onChangeQty.change(h.boundItem, newQty);
        });

        h.buttonDelete.setOnClickListener(v -> { // runs if user clicks delete button
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            onDelete.delete(data.get(pos));
        });
    }

    @Override public int getItemCount() { // gets total items in list
        return data.size();
    }

    @Override
    public void onViewRecycled(@NonNull VH h) {
        super.onViewRecycled(h);
        if (h.qtyWatcher != null) h.textQtyAmount.removeTextChangedListener(h.qtyWatcher);
        h.boundItem = null;
    }

    public static class VH extends RecyclerView.ViewHolder {

        TextView textItemName;
        EditText textQtyAmount;
        FloatingActionButton buttonDelete;
        TextWatcher qtyWatcher;
        InventoryItem boundItem;

        VH(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            textQtyAmount = itemView.findViewById(R.id.textQtyAmount);
            buttonDelete  = itemView.findViewById(R.id.buttonDelete);
        }
    }

    static abstract class SimpleWatcher implements TextWatcher { // unused but required TextWatchers
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
    }
}