package com.example.itdamindcarejava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Daftar extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database;

    private EditText etNama;
    private EditText etNim;
    private Spinner spinnerProdi;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnDaftar;
    private TextView tvLogin;

    private String selectedProdi = null;

    // Model data untuk User (Static inner class agar bisa diakses static context jika perlu)
    public static class UserData {
        public String nama;
        public String nim;
        public String prodi;
        public String email;

        // Konstruktor kosong diperlukan untuk Firebase deserialization
        public UserData() {
        }

        public UserData(String nama, String nim, String prodi, String email) {
            this.nama = nama;
            this.nim = nim;
            this.prodi = prodi;
            this.email = email;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Inisialisasi Views
        etNama = findViewById(R.id.etNama);
        etNim = findViewById(R.id.etNim);
        spinnerProdi = findViewById(R.id.spinnerProdi);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnDaftar = findViewById(R.id.daftar);
        tvLogin = findViewById(R.id.btnLogin);

        setupSpinner();

        btnDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = etNama.getText().toString().trim();
                String nim = etNim.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (nama.isEmpty() || nim.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(Daftar.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ambil string prompt dari resources (item pertama di array)
                String promptSpinner = getResources().getStringArray(R.array.program_studi_array)[0];
                if (selectedProdi == null || selectedProdi.equals(promptSpinner)) {
                    Toast.makeText(Daftar.this, "Silakan pilih program studi", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(Daftar.this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnDaftar.setEnabled(false);
                btnDaftar.setText("Mendaftarkan...");

                // Proses Pendaftaran Auth
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Daftar.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                btnDaftar.setEnabled(true);
                                btnDaftar.setText(getString(R.string.daftar_button));

                                if (task.isSuccessful()) {
                                    Log.d("DaftarActivity", "createUserWithEmail:success");
                                    FirebaseUser firebaseUser = auth.getCurrentUser();

                                    if (firebaseUser != null) {
                                        UserData userData = new UserData(nama, nim, selectedProdi, email);

                                        // Simpan data tambahan ke Realtime Database
                                        database.child("users").child(firebaseUser.getUid()).setValue(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(Daftar.this, "Pendaftaran berhasil.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(Daftar.this, Login.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w("DaftarActivity", "Gagal menyimpan data pengguna ke database.", e);
                                                    Toast.makeText(Daftar.this, "Gagal menyimpan data pengguna: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                                    // Hapus akun auth jika gagal simpan data profile (Rollback)
                                                    firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                                                        if (deleteTask.isSuccessful()) {
                                                            Log.d("DaftarActivity", "Akun pengguna di Firebase Auth dihapus setelah gagal menyimpan data.");
                                                        }
                                                    });
                                                });
                                    }
                                } else {
                                    Log.w("DaftarActivity", "createUserWithEmail:failure", task.getException());
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                        Toast.makeText(getBaseContext(), "Email sudah terdaftar.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Terjadi kesalahan";
                                        Toast.makeText(getBaseContext(), "Pendaftaran gagal: " + errorMsg, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Daftar.this, Login.class);
                startActivity(intent);
            }
        });
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.program_studi_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProdi.setAdapter(adapter);

        spinnerProdi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Mendapatkan item yang dipilih.
                // Item pertama (posisi 0) adalah "Pilih Program Studi", jadi kita abaikan/handle di validasi tombol.
                if (position > 0) {
                    selectedProdi = parent.getItemAtPosition(position).toString();
                } else {
                    selectedProdi = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProdi = null;
            }
        });
    }
}