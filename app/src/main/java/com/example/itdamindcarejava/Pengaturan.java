package com.example.itdamindcarejava;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.itdamindcarejava.databinding.FragmentPengaturanBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Pengaturan extends Fragment {

    private FragmentPengaturanBinding binding;
    private FirebaseAuth auth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout menggunakan View Binding
        binding = FragmentPengaturanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Listener tombol Bantuan
        binding.bantuan.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Pusat Bantuan diklik (belum diimplementasikan)", Toast.LENGTH_SHORT).show();
            // Contoh implementasi di masa depan:
            // Intent intent = new Intent(getActivity(), PusatBantuanActivity.class);
            // startActivity(intent);
        });

        // Listener tombol Tentang
        binding.tentang.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tentang Aplikasi", Toast.LENGTH_SHORT).show();
            showTentangAplikasiDialog();
        });

        // Listener tombol Update Akun
        binding.uAkun.setOnClickListener(v -> {
            // Meminta Activity induk untuk memulai Activity baru
            // Asumsi: Anda memiliki kelas Update.java
            Intent intent = new Intent(requireActivity(), Update.class);
            startActivity(intent);
        });

        // Listener tombol Logout
        binding.btnLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    private void showTentangAplikasiDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tentang Aplikasi")
                .setMessage("ITDA MindCare\nVersi Aplikasi: Alpha 1.0 \n\nDikembangkan oleh: \n\nDeveloper:\n[Adhitya Maulana Zada]\n\nIde & Konsep:\n[Rochfis Subiantoro]")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performLogout() {
        auth.signOut();

        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            // Mengatur flag menggunakan Bitwise OR (|)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finishAffinity();
        }
        Toast.makeText(getContext(), "Anda telah logout.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Mencegah memory leak dengan menghapus referensi binding saat view dihancurkan
        binding = null;
    }
}