package com.example.itdamindcarejava;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    // Constructor
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    // Jumlah total halaman/fragment
    @Override
    public int getItemCount() {
        return 4;
    }

    // Membuat fragment untuk setiap posisi
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Beranda();
            case 1:
                return new Riwayat();
            case 2:
                return new Profile();
            case 3:
                return new Pengaturan();
            default:
                return new Beranda(); // Fragment default
        }
    }
}