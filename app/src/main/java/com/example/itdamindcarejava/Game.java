package com.example.itdamindcarejava;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Game extends AppCompatActivity implements View.OnClickListener {

    private TextView tvStatus;
    private Map<Integer, Button> buttons;
    private Button btnBackToHome;
    private Button btnGoToHistory;
    private Button btnStartGame;

    private List<Integer> gameSequence = new ArrayList<>();
    private List<Integer> playerSequence = new ArrayList<>();
    private int level = 0;
    private boolean isPlayerTurn = false;

    // Handler untuk menggantikan Coroutines (MainScope)
    private Handler handler = new Handler(Looper.getMainLooper());

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signInAnonymously();

        tvStatus = findViewById(R.id.tvStatus);
        btnBackToHome = findViewById(R.id.btnBackToHome);
        btnGoToHistory = findViewById(R.id.btnGoToHistory);
        btnStartGame = findViewById(R.id.btnStartGame);

        // Inisialisasi Map tombol
        buttons = new HashMap<>();
        buttons.put(1, findViewById(R.id.btnGreen));
        buttons.put(2, findViewById(R.id.btnRed));
        buttons.put(3, findViewById(R.id.btnYellow));
        buttons.put(4, findViewById(R.id.btnBlue));

        // Set OnClickListener
        for (Button btn : buttons.values()) {
            btn.setOnClickListener(this);
        }
        btnBackToHome.setOnClickListener(this);
        btnGoToHistory.setOnClickListener(this);
        btnStartGame.setOnClickListener(this);

        tvStatus.setText("Tekan Mulai untuk Bermain");

        showTutorialDialog();
    }

    private void showTutorialDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cara Bermain")
                .setMessage("1. Perhatikan dan hafalkan urutan warna yang menyala.\n\n2. Ulangi urutan tersebut dengan menekan tombol warna yang benar.\n\n3. Permainan akan berlanjut ke level berikutnya jika urutan Anda benar.")
                .setPositiveButton("Mengerti", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void signInAnonymously() {
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d("Firebase", "signInAnonymously:success");
                            } else {
                                Log.w("Firebase", "signInAnonymously:failure", task.getException());
                                Toast.makeText(Game.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnBackToHome) {
            finish();
        } else if (id == R.id.btnGoToHistory) {
            // Pastikan RiwayatActivityContainer ada di project Java Anda
            Intent intent = new Intent(this, RiwayatActivityContainer.class);
            startActivity(intent);
        } else if (id == R.id.btnStartGame) {
            startGame();
        } else {
            // Logika tombol game
            if (isPlayerTurn) {
                // Cari key (1-4) berdasarkan value tombol yang diklik
                Integer clickedButtonId = null;
                for (Map.Entry<Integer, Button> entry : buttons.entrySet()) {
                    if (entry.getValue() == v) {
                        clickedButtonId = entry.getKey();
                        break;
                    }
                }

                if (clickedButtonId != null) {
                    playerSequence.add(clickedButtonId);
                    highlightButton(clickedButtonId, 150); // Feedback visual cepat
                    checkPlayerInput();
                }
            }
        }
    }

    private void startGame() {
        btnStartGame.setVisibility(View.GONE);
        level = 1;
        gameSequence.clear();
        playerSequence.clear();
        nextRound();
    }

    private void nextRound() {
        isPlayerTurn = false;
        playerSequence.clear();
        tvStatus.setText("Level: " + level);

        // Random 1 sampai 4
        gameSequence.add(new Random().nextInt(4) + 1);

        // Matikan tombol agar user tidak klik saat animasi berjalan
        setButtonsClickable(false);

        // Mulai menampilkan urutan dengan delay awal 1 detik
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playSequenceRecursive(0);
            }
        }, 1000);
    }

    // Fungsi rekursif untuk menggantikan for-loop dengan delay di Kotlin
    private void playSequenceRecursive(final int index) {
        if (index >= gameSequence.size()) {
            // Urutan selesai ditampilkan
            isPlayerTurn = true;
            tvStatus.setText("Ulangi Urutannya");
            setButtonsClickable(true);
            return;
        }

        final int buttonId = gameSequence.get(index);

        // Nyalakan lampu (Alpha 0.5)
        final Button button = buttons.get(buttonId);
        if (button != null) {
            button.setAlpha(0.5f);
        }

        // Delay 400ms untuk mematikan lampu
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    button.setAlpha(1.0f);
                }

                // Delay 200ms jeda antar lampu, lalu lanjut ke index berikutnya
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playSequenceRecursive(index + 1);
                    }
                }, 200);
            }
        }, 400);
    }

    private void setButtonsClickable(boolean clickable) {
        for (Button btn : buttons.values()) {
            btn.setClickable(clickable);
        }
    }

    private void checkPlayerInput() {
        int index = playerSequence.size() - 1;

        // Cek apakah input terakhir benar
        if (!playerSequence.get(index).equals(gameSequence.get(index))) {
            gameOver();
            return;
        }

        // Cek apakah seluruh urutan sudah selesai
        if (playerSequence.size() == gameSequence.size()) {
            level++;
            setButtonsClickable(false);

            // Jeda 1 detik sebelum ronde berikutnya
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextRound();
                }
            }, 1000);
        }
    }

    private void gameOver() {
        isPlayerTurn = false;
        int finalScore = level - 1;
        String feedbackMessage;

        if (finalScore >= 1 && finalScore <= 4) {
            feedbackMessage = "Mungkin Anda sedang banyak pikiran atau lelah.";
        } else if (finalScore >= 5 && finalScore <= 8) {
            feedbackMessage = "Ingatan dan fokus yang baik! Ini menunjukkan kamu masih bisa fokus.";
        } else if (finalScore >= 9) {
            feedbackMessage = "Luar biasa! Fokus dan memori jangka pendek Anda sangat tajam saat ini.";
        } else {
            feedbackMessage = "Coba lagi untuk melihat hasilnya!";
        }

        tvStatus.setText("Salah! Skor: " + finalScore + "\n" + feedbackMessage);
        saveScoreToFirebase(finalScore, feedbackMessage);

        level = 0;
        btnStartGame.setVisibility(View.VISIBLE);
    }

    private void saveScoreToFirebase(int score, String feedback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Log.w("Firebase", "User not signed in, cannot save score.");
            return;
        }

        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("score", score);
        scoreData.put("feedback", feedback);
        scoreData.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .collection("MindCareScores")
                .add(scoreData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("Firebase", "Score saved for user " + user.getUid() + " with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firebase", "Error adding document for user " + user.getUid(), e);
                    }
                });
    }

    private void highlightButton(int buttonId, long duration) {
        final Button button = buttons.get(buttonId);
        if (button != null) {
            button.setAlpha(0.5f);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    button.setAlpha(1.0f);
                }
            }, duration);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hapus semua callback yang pending agar tidak memory leak atau crash
        handler.removeCallbacksAndMessages(null);
    }
}