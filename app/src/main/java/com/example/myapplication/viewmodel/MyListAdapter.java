package com.example.myapplication.viewmodel;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Country;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    public List<Country> mListData;

    // RecyclerView recyclerView;
    public MyListAdapter(List<Country> listData) {
        this.mListData = listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 0) {
            holder.itemView.setBackgroundColor(Color.CYAN);
        }
        final Country country = mListData.get(position);
        holder.textViewName.setText(country.getmName());
        holder.textViewTotalCases.setText(String.valueOf(country.getmTotalCases()));
        holder.textViewDeaths.setText(String.valueOf(country.getmTotalDeaths()));
        holder.textViewRecovered.setText(String.valueOf(country.getmTotalRecovered()));
    }


    @Override
    public int getItemCount() {
        return mListData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewTotalCases;
        public TextView textViewDeaths;
        public TextView textViewRecovered;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            this.textViewTotalCases = (TextView) itemView.findViewById(R.id.textViewTotalCases);
            this.textViewDeaths = (TextView) itemView.findViewById(R.id.textViewDeaths);
            this.textViewRecovered = (TextView) itemView.findViewById(R.id.textViewRecovered);
        }
    }
}
