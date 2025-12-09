package com.example.itdamindcarejava;

import com.google.firebase.firestore.IgnoreExtraProperties;

// Anotasi untuk memberi tahu Firestore agar mengabaikan field yang tidak cocok
@IgnoreExtraProperties
public class GameHistory {

    private long score;
    private String feedback;
    private long timestamp;

    // 1. Konstruktor Kosong (No-Argument Constructor)
    // SANGAT PENTING: Firebase memerlukan ini untuk deserialisasi data dari dokumen ke objek Java.
    public GameHistory() {
        // Nilai default sesuai kode Kotlin sebelumnya
        this.score = 0;
        this.feedback = "";
        this.timestamp = 0;
    }

    // 2. Konstruktor Lengkap (Opsional, tapi berguna untuk membuat objek baru)
    public GameHistory(long score, String feedback, long timestamp) {
        this.score = score;
        this.feedback = feedback;
        this.timestamp = timestamp;
    }

    // 3. Getter dan Setter (Wajib untuk akses properti)

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}