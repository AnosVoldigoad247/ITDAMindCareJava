package com.example.itdamindcarejava;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class RiwayatActivityContainer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mengatur layout activity ke activity_riwayat_container.xml
        setContentView(R.layout.activity_riwayat_container);

        // Cek jika activity baru pertama kali dibuat (bukan karena rotasi layar)
        // Ini mencegah fragment dibuat ulang setiap kali layar diputar.
        if (savedInstanceState == null) {
            // Buat instance dari Riwayat Fragment
            Riwayat riwayatFragment = new Riwayat();

            // Gunakan FragmentManager untuk menempatkan fragment ke dalam container
            // Di Java, kita menggunakan method getter 'getSupportFragmentManager()'
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, riwayatFragment)
                    .commit();
        }
    }
}