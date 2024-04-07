package com.example.carsteering4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView

class MainActivity : AppCompatActivity(), GameView.GameOverListener {

    private lateinit var rootLayout: LinearLayout
    private lateinit var startBtn: Button
    private lateinit var restartBtn: Button
    private lateinit var gameView: GameView
    private lateinit var score: TextView
    private lateinit var choiceRadioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        startBtn = findViewById(R.id.startBtn)
        restartBtn = findViewById(R.id.restartBtn)
        rootLayout = findViewById(R.id.rootLayout)
        choiceRadioGroup = findViewById(R.id.choiceRadioGroup)
        score = findViewById(R.id.score)
        score.visibility = View.GONE

        // Initialize GameView
        gameView = GameView(this).apply {
            gameOverListener = this@MainActivity
        }

        // Set OnClickListener for Start Button
        startBtn.setOnClickListener {
            // Start the game
            startGame()
        }

        // Set OnClickListener for Restart Button
        restartBtn.setOnClickListener {
            restartGame()
        }

        // Set OnCheckedChangeListener for choice radio group
        choiceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Update control mode based on selected radio button
            when (checkedId) {
                R.id.gyroscopeRadioButton -> gameView.setControlMode(ControlMode.GYROSCOPE)
                R.id.touchRadioButton -> gameView.setControlMode(ControlMode.TOUCH)
                R.id.volumeButtonRadioButton -> gameView.setControlMode(ControlMode.VOLUME)
            }
        }
    }

    // Hide the status bar
    // Adding suppress because of OCD
    @Suppress("DEPRECATION")
    private fun hideStatusBar(){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide() // Hide the action bar if present
    }

    // Method to start the game
    private fun startGame() {
        // Set background resource for GameView
        gameView.setBackgroundResource(R.drawable.road)

        // Add GameView to the root layout
        rootLayout.addView(gameView)

        // Hide Start Button and choice radio group
        startBtn.visibility = View.GONE
        restartBtn.visibility = View.GONE
        choiceRadioGroup.visibility = View.GONE
        hideStatusBar()
    }

    // Method to restart the game
    private fun restartGame() {
        // Remove GameView from root layout
        rootLayout.removeView(gameView)

        // Show Start Button and choice radio group
        startBtn.visibility = View.VISIBLE
        restartBtn.visibility = View.GONE
        choiceRadioGroup.visibility = View.VISIBLE
        score.visibility = View.GONE
        gameView.resetGame()
        hideStatusBar()
    }

    // Implement the onGameOver method from GameView.GameOverListener interface
    override fun onGameOver() {
        // Show the restart button
        restartBtn.visibility = View.VISIBLE
        hideStatusBar()
    }
}