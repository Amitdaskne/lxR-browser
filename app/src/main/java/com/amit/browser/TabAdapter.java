package com.amit.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TabAdapter extends RecyclerView.Adapter<TabAdapter.VH> {
    private List<String> tabs;
    private int selectedTab;
    private OnTabAction listener;

    public interface OnTabAction {
        void onSelect(int position);
        void onClose(int position);
    }

    public TabAdapter(List<String> tabs, int selectedTab, OnTabAction listener) {
        this.tabs = tabs;
        this.selectedTab = selectedTab;
        this.listener = listener;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        String url = tabs.get(pos);
        h.tvTitle.setText(url.equals("https://www.google.com") ? "New Tab" : url);
        
        if (pos == selectedTab) h.itemView.setAlpha(1f);
        else h.itemView.setAlpha(0.6f);

        h.itemView.setOnClickListener(v -> listener.onSelect(pos));
        h.btnClose.setOnClickListener(v -> listener.onClose(pos));
    }

    @Override public int getItemCount() { return tabs.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView btnClose;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTabTitle);
            btnClose = itemView.findViewById(R.id.btnCloseTab);
        }
    }
}
