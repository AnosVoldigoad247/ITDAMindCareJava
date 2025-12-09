package com.example.itdamindcarejava;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Riwayat extends Fragment {

    private RecyclerView rvRiwayat;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private RiwayatAdapter riwayatAdapter;
    // Inisialisasi ArrayList untuk menghindari NullPointerException saat adapter dipanggil
    private List<GameHistory> historyList = new ArrayList<>();

    // Get Firestore and Auth instances
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration historyListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_riwayat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase Auth & Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi semua View dari layout fragment
        rvRiwayat = view.findViewById(R.id.rvRiwayat);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Pastikan RiwayatAdapter menerima List<GameHistory> di konstruktornya
        riwayatAdapter = new RiwayatAdapter(historyList);
        rvRiwayat.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRiwayat.setAdapter(riwayatAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForHistoryUpdates(); // Panggil metode listener yang baru
    }
    @Override
    public void onStop() {
        super.onStop();
        if (historyListener != null) {
            historyListener.remove(); // Sangat penting!
        }
    }

    private void listenForHistoryUpdates() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvRiwayat.setVisibility(View.GONE);

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            tvEmpty.setText("Anda belum login.\nMainkan game untuk melihat riwayat.");
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        // Ambil data dari sub-koleksi milik pengguna yang sedang login
        db.collection("users").document(user.getUid())
                .collection("MindCareScores")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot result) {
                        // Cek apakah fragment masih terpasang agar tidak crash saat update UI
                        if (!isAdded()) return;

                        historyList.clear();
                        if (result.isEmpty()) {
                            tvEmpty.setText("Belum ada riwayat permainan.");
                            tvEmpty.setVisibility(View.VISIBLE);
                        } else {
                            // Konversi setiap dokumen ke objek GameHistory
                            for (DocumentSnapshot document : result) {
                                GameHistory historyItem = document.toObject(GameHistory.class);
                                if (historyItem != null) {
                                    historyList.add(historyItem);
                                }
                            }
                            riwayatAdapter.notifyDataSetChanged();
                            rvRiwayat.setVisibility(View.VISIBLE);
                        }
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Cek apakah fragment masih terpasang
                        if (!isAdded()) return;

                        progressBar.setVisibility(View.GONE);
                        tvEmpty.setText("Gagal memuat riwayat.");
                        tvEmpty.setVisibility(View.VISIBLE);
                        Log.w("RiwayatFragment", "Error getting documents for user " + user.getUid(), exception);
                    }
                });
    }
}