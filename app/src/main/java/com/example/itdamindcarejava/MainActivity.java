package com.example.itdamindcarejava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.itdamindcarejava.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";

    // Launcher untuk izin notifikasi
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted.");
                    Toast.makeText(MainActivity.this, "Izin notifikasi diberikan.", Toast.LENGTH_SHORT).show();
                    storeTokenToFirestore();
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Izin notifikasi ditolak. Beberapa fitur mungkin tidak berfungsi.",
                            Toast.LENGTH_LONG
                    ).show();
                    Log.w(TAG, "Notification permission denied by user.");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cek Intent extras (Debugging)
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup ViewPager2 dengan adapter (Pastikan ViewPagerAdapter kompatibel dengan Java)
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        askNotificationPermission();
        logRegToken();
        storeTokenToFirestore();

        // Sinkronisasi BottomNavigationView dengan ViewPager2 saat item di klik
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int position;
            int itemId = item.getItemId();

            if (itemId == R.id.beranda) {
                position = 0;
            } else if (itemId == R.id.riwayat) {
                position = 1;
            } else if (itemId == R.id.profil) {
                position = 2;
            } else if (itemId == R.id.pengaturan) {
                position = 3;
            } else {
                position = 0;
            }

            // Pindah halaman tanpa animasi geser
            binding.viewPager.setCurrentItem(position, false);
            return true;
        });

        // Sinkronisasi ViewPager2 dengan BottomNavigationView saat halaman digeser
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private void storeTokenToFirestore() {
        // Mengambil token FCM
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Dapatkan token FCM baru
                String token = task.getResult();
                Log.d(TAG, "FCM Token fetched in storeTokenToFirestore: " + token);

                // Siapkan data untuk Firestore
                Map<String, Object> deviceToken = new HashMap<>();
                deviceToken.put("token", token);
                deviceToken.put("timestamp", FieldValue.serverTimestamp());

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                if (currentUser != null) {
                    String userId = currentUser.getUid();

                    // Simpan ke Firestore
                    FirebaseFirestore.getInstance().collection("fcmTokens").document(userId)
                            .set(deviceToken)
                            .addOnSuccessListener(aVoid ->
                                    Log.d(TAG, "FCM token for user " + userId + " stored/updated successfully in Firestore.")
                            )
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error storing FCM token for user " + userId + " in Firestore", e);
                                Toast.makeText(MainActivity.this, "Gagal menyimpan token FCM ke Firestore.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.w(TAG, "User not logged in. FCM token for specific user NOT stored in Firestore.");
                }
            }
        });
    }

    public void runtimeEnableAutoInit() {
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        Log.d(TAG, "FCM auto-init enabled.");
    }

    public void subscribeTopics() {
        FirebaseMessaging.getInstance().subscribeToTopic("weather")
                .addOnCompleteListener(task -> {
                    String msg = task.isSuccessful() ?
                            "Berhasil subscribe ke topik 'weather'" :
                            "Gagal subscribe ke topik 'weather'";
                    Log.d(TAG, msg);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                });
    }

    public void logRegToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }
            // Get new FCM registration token
            String token = task.getResult();
            String msg = "FCM Registration token (for logging): " + token;
            Log.d(TAG, msg);
        });
    }

    private void askNotificationPermission() {
        // Fitur ini hanya untuk Android 13 (Tiramisu) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission already granted.");
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                new AlertDialog.Builder(this)
                        .setTitle("Izin Notifikasi Diperlukan")
                        .setMessage("Aplikasi ini memerlukan izin notifikasi untuk memberi Anda pembaruan penting. Izinkan?")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        )
                        .setNegativeButton("Lain Kali", (dialog, which) -> {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "Izin notifikasi tidak diberikan saat ini.", Toast.LENGTH_SHORT).show();
                        })
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            Log.d(TAG, "Notification permission not required for this API level or granted by default.");
        }
    }
}