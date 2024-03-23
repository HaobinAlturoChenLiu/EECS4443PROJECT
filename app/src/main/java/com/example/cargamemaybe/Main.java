package com.example.cargamemaybe;

import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class Main extends AppCompatActivity {

    private ImageView carImageView;
    private Handler handler;
    private Random random;

    private int xpos = 300; // Initial X position of the car
    private int roadWidth = 600; // Width of the road
    private int roadMargin = 50; // Margin from the road boundary
    private int roadCenter; // Center of the road

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
        if (xpos > roadWidth - roadMargin) {
            xpos = roadMargin;
        }
        carImageView.setX(xpos);
    }
    // Add touch event
}
