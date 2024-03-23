package com.example.cargamemaybe;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class FullscreenActivity extends AppCompatActivity {

    private ImageView carImageView;
    private Handler handler;
    private Random random;

    private int xpos = 300;
    private int ypos = 700;
    private int roadmove = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);

        carImageView = findViewById(R.id.carImageView);
        handler = new Handler();
        random = new Random();

        startGame();
    }

    private void startGame() {
        handler.postDelayed(gameRunnable, 100);
    }

    private Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            updateGameState();
            handler.postDelayed(this, 100); // Adjust timing as needed
        }
    };

    private void updateGameState() {
        // Update game state here
        // Example: Move car, update positions, handle collisions, etc.
        // Use carImageView.setImageResource(R.drawable.carImage) to set car image
        // For simplicity, let's just move the car horizontally back and forth
        xpos += 10; // Move the car horizontally
        if (xpos > 600) { // If the car goes beyond the right boundary
            xpos = 600; // Reset the car position
        }
        carImageView.setX(xpos); // Set the new X position of the car ImageView
    }

    // Add touch event handling if needed
}
