package com.example.itdamindcarejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder> {

    private List<GameHistory> riwayatList;

    // Konstruktor
    public RiwayatAdapter(List<GameHistory> riwayatList) {
        this.riwayatList = riwayatList;
    }

    // ViewHolder sebagai static inner class untuk menghindari memory leaks
    public static class RiwayatViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTgl;
        public TextView tvLevel;
        public TextView tvHasil;

        public RiwayatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTgl = itemView.findViewById(R.id.tvTanggalLevelDicapai);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvHasil = itemView.findViewById(R.id.tvHasilLevelDicapai);
        }
    }

    @NonNull
    @Override
    public RiwayatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_riwayat_adapter, parent, false);
        return new RiwayatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatViewHolder holder, int position) {
        GameHistory riwayat = riwayatList.get(position);

        // Set data ke TextView
        // Penting: Gunakan String.valueOf() untuk mengonversi angka (long/int) ke String.
        // Jika langsung setText(angka), Android akan mengira itu adalah Resource ID dan menyebabkan crash.
        holder.tvLevel.setText(String.valueOf(riwayat.getScore()));

        holder.tvHasil.setText(riwayat.getFeedback());

        // Format timestamp
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            Date date = new Date(riwayat.getTimestamp());
            holder.tvTgl.setText(sdf.format(date));
        } catch (Exception e) {
            holder.tvTgl.setText("Tanggal tidak valid");
        }
    }

    @Override
    public int getItemCount() {
        return riwayatList.size();
    }
}