package com.example.itdamindcarejava;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.itdamindcarejava.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends Fragment {

    private FragmentProfileBinding binding;
    private final FirebaseDatabase realtimeDatabase = FirebaseDatabase.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        namaPengguna();
        prodiPengguna();
        nimPengguna();
        emailPengguna();
    }

    private void namaPengguna() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = realtimeDatabase.getReference("users").child(uid).child("nama");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null) {
                        String nama = snapshot.getValue(String.class);
                        if (nama != null && !nama.isEmpty()) {
                            binding.tNama.setText(nama);
                        } else {
                            binding.tNama.setText("Pengguna");
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
            if (binding != null) {
                binding.tNama.setText("Belum login");
            }
        }
    }

    private void prodiPengguna() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = realtimeDatabase.getReference("users").child(uid).child("prodi");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null) {
                        String prodi = snapshot.getValue(String.class);
                        if (prodi != null && !prodi.isEmpty()) {
                            binding.tProdi.setText(prodi);
                        } else {
                            binding.tProdi.setText("Pengguna");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Gagal memuat prodi: " + error.getMessage(), error.toException());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Gagal memuat prodi pengguna", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (binding != null) {
                binding.tProdi.setText("Belum login");
            }
        }
    }

    private void nimPengguna() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = realtimeDatabase.getReference("users").child(uid).child("nim");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null) {
                        String nim = snapshot.getValue(String.class);
                        if (nim != null && !nim.isEmpty()) {
                            binding.tNim.setText(nim);
                        } else {
                            binding.tNim.setText("Pengguna");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Gagal memuat NIM: " + error.getMessage(), error.toException());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Gagal memuat NIM pengguna", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (binding != null) {
                // Catatan: Di kode asli Kotlin Anda menggunakan tProdi di sini (bagian else),
                // saya sesuaikan agar logis ke tNim, atau kembalikan ke tProdi jika memang disengaja.
                // Asumsi saya ini typo di kode asli, jadi saya ubah ke tNim.
                binding.tNim.setText("Belum login");
            }
        }
    }

    private void emailPengguna() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = realtimeDatabase.getReference("users").child(uid).child("email");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding != null) {
                        String email = snapshot.getValue(String.class);
                        if (email != null && !email.isEmpty()) {
                            binding.tEmail.setText(email);
                        } else {
                            binding.tEmail.setText("Pengguna");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseError", "Gagal memuat Email: " + error.getMessage(), error.toException());
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Gagal memuat Email pengguna", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (binding != null) {
                // Di kode Kotlin, bagian ini juga menargetkan tProdi, saya asumsikan typo
                // dan saya ubah ke tEmail agar sesuai konteks fungsi.
                binding.tEmail.setText("Belum login");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Sangat penting di Fragment Java untuk menghindari Memory Leak
        binding = null;
    }
}