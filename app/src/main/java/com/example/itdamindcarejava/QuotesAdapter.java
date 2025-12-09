package com.example.itdamindcarejava;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuotesAdapter extends RecyclerView.Adapter<QuotesAdapter.QuoteViewHolder> {

    private List<Quote> quotesList;

    // Constructor
    public QuotesAdapter(List<Quote> quotesList) {
        this.quotesList = quotesList;
    }

    // ViewHolder memegang referensi ke view untuk setiap item
    public static class QuoteViewHolder extends RecyclerView.ViewHolder {
        public TextView quoteTextView;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sesuaikan dengan ID di layout XML Anda
            quoteTextView = itemView.findViewById(R.id.teksQuote);
        }
    }

    // Membuat ViewHolder baru (dipanggil oleh layout manager)
    @NonNull
    @Override
    public QuoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_quotes_adapter, parent, false); // Gunakan layout yang sesuai
        return new QuoteViewHolder(itemView);
    }

    // Mengganti konten view (dipanggil oleh layout manager)
    @Override
    public void onBindViewHolder(@NonNull QuoteViewHolder holder, int position) {
        Quote currentQuote = quotesList.get(position);

        // Menggunakan getter karena di Java properti privat diakses lewat method
        holder.quoteTextView.setText(currentQuote.getText());
    }

    // Mengembalikan ukuran dataset (dipanggil oleh layout manager)
    @Override
    public int getItemCount() {
        return quotesList.size();
    }
}