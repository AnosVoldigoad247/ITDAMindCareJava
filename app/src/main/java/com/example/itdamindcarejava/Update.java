package com.example.itdamindcarejava;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.itdamindcarejava.databinding.ActivityUpdateBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Update extends AppCompatActivity {

    private ActivityUpdateBinding binding;
    private FirebaseAuth auth;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("users");

        // Mengatur visibilitas CardView (sesuai kode asli)
        binding.cardView.setVisibility(View.GONE);

        loadUserData();

        binding.btnSimpanPerubahan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfileData();
            }
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            binding.etEmailPengguna.setText(user.getEmail());

            database.child(user.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String nama = dataSnapshot.child("nama").getValue(String.class);
                                String nim = dataSnapshot.child("nim").getValue(String.class);
                                String prodi = dataSnapshot.child("prodi").getValue(String.class);

                                // Set teks, hindari null pointer dengan cek sederhana atau string kosong
                                binding.etNamaPengguna.setText(nama != null ? nama : "");
                                binding.etNimPengguna.setText(nim != null ? nim : "");
                                binding.etProdiPengguna.setText(prodi != null ? prodi : "");
                            } else {
                                Toast.makeText(Update.this, "Data pengguna tidak ditemukan di database.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Update.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateProfileData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Sesi berakhir, silakan login kembali.", Toast.LENGTH_SHORT).show();
            return;
        }

        String newUsername = binding.etNamaPengguna.getText().toString().trim();
        String newNim = binding.etNimPengguna.getText().toString().trim();
        String newProdi = binding.etProdiPengguna.getText().toString().trim();
        String newEmail = binding.etEmailPengguna.getText().toString().trim();

        if (newUsername.isEmpty() || newNim.isEmpty() || newProdi.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi.", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBarUpdate.setVisibility(View.VISIBLE);
        binding.btnSimpanPerubahan.setEnabled(false);

        // Langkah 1: Update Email di Firebase Auth
        user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> emailTask) {
                if (emailTask.isSuccessful()) {

                    // Langkah 2: Update Data Profil di Realtime Database
                    Map<String, Object> userUpdates = new HashMap<>();
                    userUpdates.put("nama", newUsername);
                    userUpdates.put("nim", newNim);
                    userUpdates.put("prodi", newProdi);

                    database.child(user.getUid()).updateChildren(userUpdates)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    binding.progressBarUpdate.setVisibility(View.GONE);
                                    binding.btnSimpanPerubahan.setEnabled(true);
                                    Toast.makeText(Update.this, "Profil berhasil diperbarui.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.progressBarUpdate.setVisibility(View.GONE);
                                    binding.btnSimpanPerubahan.setEnabled(true);
                                    Toast.makeText(Update.this, "Gagal memperbarui data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    binding.progressBarUpdate.setVisibility(View.GONE);
                    binding.btnSimpanPerubahan.setEnabled(true);
                    String errorMsg = emailTask.getException() != null ? emailTask.getException().getMessage() : "Error";
                    Toast.makeText(Update.this, "Gagal memperbarui email: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}