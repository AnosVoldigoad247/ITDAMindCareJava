package com.example.itdamindcarejava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference database; // Referensi database
    private EditText etNimLogin;
    private EditText etPasswordLogin;
    private Button btnLogin;
    private TextView tvDaftar;

    // Interface untuk Callback hasil query NIM
    interface EmailQueryCallback {
        void onCallback(String email);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference().child("users"); // Referensi ke node "users"

        // Cek apakah user sudah login
        if (auth.getCurrentUser() != null) {
            navigateToMainActivity();
            return;
        }

        // Inisialisasi Views
        etNimLogin = findViewById(R.id.etNim);
        etPasswordLogin = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.login);
        tvDaftar = findViewById(R.id.btnDaftar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nimInput = etNimLogin.getText().toString().trim();
                String passwordInput = etPasswordLogin.getText().toString().trim();

                if (nimInput.isEmpty() || passwordInput.isEmpty()) {
                    Toast.makeText(Login.this, "NIM dan Password harus diisi", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnLogin.setEnabled(false);
                btnLogin.setText("Mencari pengguna...");

                // Langkah 1: Cari email berdasarkan NIM di Realtime Database
                queryEmailByNim(nimInput, new EmailQueryCallback() {
                    @Override
                    public void onCallback(String emailFromDb) {
                        if (emailFromDb != null) {
                            btnLogin.setText("Logging in...");
                            // Langkah 2: Lakukan sign-in dengan email yang ditemukan
                            auth.signInWithEmailAndPassword(emailFromDb, passwordInput)
                                    .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            btnLogin.setEnabled(true);
                                            btnLogin.setText(getString(R.string.login_button)); // Pastikan string resource ada

                                            if (task.isSuccessful()) {
                                                Log.d("LoginActivity", "Login dengan Email:Sukses");
                                                Toast.makeText(getBaseContext(), "Login berhasil.", Toast.LENGTH_SHORT).show();
                                                navigateToMainActivity();
                                            } else {
                                                Log.w("LoginActivity", "Login dengan Email:Gagal", task.getException());
                                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Error";
                                                Toast.makeText(getBaseContext(), "Login gagal: " + errorMsg, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // NIM tidak ditemukan di database
                            btnLogin.setEnabled(true);
                            btnLogin.setText(getString(R.string.login_button));
                            Toast.makeText(Login.this, "NIM tidak terdaftar atau terjadi kesalahan.", Toast.LENGTH_LONG).show();
                            Log.w("LoginActivity", "NIM tidak ditemukan di database: " + nimInput);
                        }
                    }
                });
            }
        });

        tvDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Daftar.class);
                startActivity(intent);
            }
        });
    }

    private void queryEmailByNim(String nim, final EmailQueryCallback callback) {
        // Query untuk mencari user dengan nim yang cocok
        database.orderByChild("nim").equalTo(nim)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // NIM ditemukan, ambil emailnya
                            // Diasumsikan NIM unik, jadi kita ambil yang pertama
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                // Menggunakan kelas Daftar.UserData yang sudah kita buat sebelumnya
                                Daftar.UserData user = userSnapshot.getValue(Daftar.UserData.class);

                                if (user != null) {
                                    callback.onCallback(user.email);
                                } else {
                                    callback.onCallback(null);
                                }
                                return; // Keluar setelah menemukan yang pertama
                            }
                        } else {
                            // NIM tidak ditemukan
                            callback.onCallback(null);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("LoginActivity", "Database query cancelled or failed.", error.toException());
                        Toast.makeText(getBaseContext(), "Gagal mengambil data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        callback.onCallback(null); // Panggil callback dengan null jika ada error
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}