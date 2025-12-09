package com.example.itdamindcarejava;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.itdamindcarejava.databinding.FragmentBerandaBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Model Class untuk Quote (sebelumnya data class di Kotlin)
class Quote {
    private String text = "";

    // Constructor kosong wajib untuk Firebase
    public Quote() {
    }

    public Quote(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

public class Beranda extends Fragment {

    private FragmentBerandaBinding binding;
    private List<Quote> dailyQuotesList = new ArrayList<>();
    private FirebaseDatabase realtimeDatabase = FirebaseDatabase.getInstance();
    private ValueEventListener quotesListener;
    private Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private final long AUTO_SCROLL_DELAY_MS = 6000L;
    private int currentPosition = 0;
    private SlowScrollLinearLayoutManager slowVerticalLayoutManager;
    private Runnable autoScrollRunnable;
    private QuotesAdapter quotesAdapter; // Pastikan Anda sudah mengonversi QuotesAdapter ke Java

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBerandaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        slowVerticalLayoutManager = new SlowScrollLinearLayoutManager(
                requireContext(),
                RecyclerView.VERTICAL,
                false
        );

        if (binding.quotes != null) {
            binding.quotes.setLayoutManager(slowVerticalLayoutManager);

            quotesAdapter = new QuotesAdapter(dailyQuotesList);
            binding.quotes.setAdapter(quotesAdapter);

            PagerSnapHelper snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(binding.quotes);
        }

        quotes();
        namaPengguna();
        setupClickListeners();
    }

    private void setupClickListeners() {
        if (binding.btnMulai != null) {
            binding.btnMulai.setOnClickListener(v -> {
                // Pastikan class Game sudah dikonversi ke Java
                Intent intent = new Intent(requireActivity(), Game.class);
                startActivity(intent);
            });
        }

        // Listener untuk Floating Action Button (FAB)
        if (binding.fabWhatsapp != null) {
            binding.fabWhatsapp.setOnClickListener(v -> {
                String phoneNumberWithCountryCode = "+6285190000924";
                String message = "Halo, saya ingin berkonsultasi mengenai layanan ITDA MindCare.";

                try {
                    String url = "https://api.whatsapp.com/send?phone=" + phoneNumberWithCountryCode +
                            "&text=" + URLEncoder.encode(message, "UTF-8");

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));

                    startActivity(intent);
                } catch (UnsupportedEncodingException e) {
                    Log.e("BerandaFragment", "Encoding error", e);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "WhatsApp tidak terpasang di perangkat Anda.", Toast.LENGTH_LONG).show();
                    Log.e("BerandaFragment", "Gagal membuka WhatsApp", e);
                }
            });
        }

        // Listener untuk CardView "portal"
        if (binding.portal != null) {
            binding.portal.setOnClickListener(v -> {
                String url = "https://www.itda.ac.id/";
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    if (getContext() != null) {
                        Toast.makeText(requireContext(), "Tidak dapat membuka link.", Toast.LENGTH_SHORT).show();
                    }
                    Log.e("BerandaFragment", "Gagal membuka link browser", e);
                }
            });
        }
    }

    private void namaPengguna() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = realtimeDatabase.getReference("users").child(uid).child("nama");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Cek jika binding masih ada (fragment belum didestroy saat callback kembali)
                    if (binding != null && binding.tvNama != null) {
                        String nama = snapshot.getValue(String.class);
                        if (nama != null && !nama.isEmpty()) {
                            binding.tvNama.setText(nama);
                        } else {
                            binding.tvNama.setText("Pengguna");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Gagal memuat nama: " + error.getMessage(), error.toException());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Gagal memuat nama pengguna", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (binding != null && binding.tvNama != null) {
                binding.tvNama.setText("Belum login");
            }
        }
    }

    private void quotes() {
        DatabaseReference quotesRef = realtimeDatabase.getReference("quotes");

        quotesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dailyQuotesList.clear();
                for (DataSnapshot quoteSnapshot : snapshot.getChildren()) {
                    String text = quoteSnapshot.child("text").getValue(String.class);
                    if (text != null && !text.isEmpty()) {
                        dailyQuotesList.add(new Quote(text));
                    }
                }

                Collections.shuffle(dailyQuotesList);
                if (quotesAdapter != null) {
                    quotesAdapter.notifyDataSetChanged();
                }

                if (!dailyQuotesList.isEmpty()) {
                    setupAutoScroll();
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Gagal mengambil data quotes: " + error.getMessage(), error.toException());
                if (getContext() != null) {
                    Toast.makeText(requireContext(), "Gagal memuat kutipan. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
                }
            }
        };

        quotesRef.addValueEventListener(quotesListener);
    }

    private void setupAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (quotesAdapter == null || quotesAdapter.getItemCount() == 0 || !isAdded() || binding == null) {
                    autoScrollHandler.removeCallbacks(this);
                    return;
                }

                View snapView = null;
                if (binding.quotes != null && binding.quotes.getLayoutManager() != null) {
                    snapView = new PagerSnapHelper().findSnapView(binding.quotes.getLayoutManager());
                }

                if (snapView != null) {
                    currentPosition = slowVerticalLayoutManager.getPosition(snapView);
                }

                currentPosition = (currentPosition + 1) % quotesAdapter.getItemCount();

                if (binding.quotes != null) {
                    binding.quotes.smoothScrollToPosition(currentPosition);
                }
                autoScrollHandler.postDelayed(this, AUTO_SCROLL_DELAY_MS);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!dailyQuotesList.isEmpty() && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
        if (quotesListener != null) {
            realtimeDatabase.getReference("quotes").removeEventListener(quotesListener);
        }
        binding = null;
    }
}

// Helper class untuk scroll lambat (diletakkan dalam file yang sama, non-public)
class SlowScrollLinearLayoutManager extends LinearLayoutManager {

    private static final float MILLISECONDS_PER_INCH = 300f;

    public SlowScrollLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }
}