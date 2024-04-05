package com.example.carsteering4

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import java.util.logging.Handler
import kotlin.math.abs
import kotlin.math.sqrt

class GameView(c: Context) : View(c) {

    private var myPaint: Paint? = null
    private var myCarPosition = 0
    private var targetCarPosition = myCarPosition // New variable to store target position
    private var movingRight = false // Flag to indicate movement to the right
    private var movingLeft = false // Flag to indicate movement to the left
    private var speed = 1
    private var time = 0
    private var score = 0
    private val coins = ArrayList<HashMap<String, Any>>()
    private val smoothness = 6
    private var coinSize = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var gameOver = false

    init {
        myPaint = Paint()
        isFocusable = true // Set the view to be focusable
        isFocusableInTouchMode = true // Allow focus to be acquired in touch mode
        requestFocus() // Request focus for the view

        // Start the game timer
        val handler = android.os.Handler()
        handler.postDelayed({
            gameOver = true
            invalidate()
        }, 90000) // 90 seconds (90000 milliseconds)
    }

    @SuppressLint("DrawAllocation", "UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gameOver) {
            // Display final score
            myPaint!!.color = Color.WHITE
            myPaint!!.textSize = 80f
            canvas.drawText("Game Over", (viewWidth / 2 - 200).toFloat(), (viewHeight / 2 - 50).toFloat(), myPaint!!)
            canvas.drawText("Score : $score", (viewWidth / 2 - 180).toFloat(), (viewHeight / 2 + 50).toFloat(), myPaint!!)
            return
        }

        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (0 until smoothness).random() // Update to smoothness lanes
            map["startTime"] = time
            coins.add(map)
        }
        time += 10 + speed
        val carWidth = viewWidth / 10
        val carHeight = viewWidth / 10
        myPaint!!.style = Paint.Style.FILL

        // Calculate the position to draw the car
        val carX = targetCarPosition * viewWidth / smoothness + (viewWidth / smoothness - carWidth) / 2
        val carY = viewHeight - 2 - carHeight

        val d = resources.getDrawable(R.drawable.car, null)
        d.setBounds(carX, carY, carX + carWidth, carY + carHeight)
        d.draw(canvas)
        myPaint!!.color = Color.GREEN
        var highScore = 0

        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            try {
                val laneWidth = viewWidth / smoothness // Calculate the width of each lane
                val coinLane = (coin["lane"] as Int).coerceIn(0, smoothness - 1) // Ensure the coin's lane is within valid range

                // Calculate the X position of the coin
                val coinX = coinLane * laneWidth + laneWidth / 2 - coinSize / 2
                val coinY = time - coin["startTime"] as Int

                val d2 = resources.getDrawable(R.drawable.coin, null)

                // Calculate the coin size
                coinSize = viewWidth / 30

                d2.setBounds(coinX, coinY - coinSize, coinX + coinSize, coinY)
                d2.draw(canvas)

                // Calculate the center points of the car and the coin for collision detection
                val carHitboxWidth = carWidth * 0.6f
                val carHitboxHeight = carHeight * 0.6f
                val carHitboxX = carX + (carWidth - carHitboxWidth) / 2
                val carHitboxY = carY + (carHeight - carHitboxHeight) / 2
                val carHitboxCenterX = carHitboxX + carHitboxWidth / 2
                val carHitboxCenterY = carHitboxY + carHitboxHeight / 2
                val coinCenterX = coinX + coinSize / 2
                val coinCenterY = coinY - coinSize / 2

                // Check for collision between car hitbox and coin
                val distance = sqrt(
                    ((carHitboxCenterX - coinCenterX) * (carHitboxCenterX - coinCenterX)).toDouble() +
                            ((carHitboxCenterY - coinCenterY) * (carHitboxCenterY - coinCenterY)).toDouble()
                )
                if (distance < (carHitboxWidth + coinSize) / 2) {
                    iterator.remove() // Remove the collected coin
                    score++ // Increment the score
                    speed = 1 + abs(score / 8)
                    if (score > highScore) {
                        highScore = score
                    }
                }

                if (coinY > viewHeight + carHeight) {
                    iterator.remove()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        myPaint!!.color = Color.WHITE
        myPaint!!.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint!!)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint!!)

        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gameOver) return true // Don't handle touch events if the game is over

        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                // Get the X coordinate of the touch event
                val x1 = event.x

                // Calculate the width of each lane
                val laneWidth = viewWidth / smoothness
                // Determine the lane in which the touch event occurred
                val touchedLane = (x1 / laneWidth).toInt()

                // Update the target car position
                targetCarPosition = touchedLane
                // Set the movement flags based on the target position
                movingRight = targetCarPosition > myCarPosition
                movingLeft = targetCarPosition < myCarPosition
            }

            MotionEvent.ACTION_DOWN -> {
                // Stop the car's movement when touch is released
                movingRight = false
                movingLeft = false
            }
        }
        // Redraw the view to reflect the updated car position
        invalidate()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (gameOver) return true // Don't handle key events if the game is over

        when (keyCode) {
            KeyEvent.KEYCODE_SPACE -> {
                // Move the car to the right lane
                targetCarPosition = (targetCarPosition + 1).coerceAtMost(smoothness - 1)
                movingRight = true
                return true
            }
            KeyEvent.KEYCODE_TAB -> {
                // Move the car to the left lane
                targetCarPosition = (targetCarPosition - 1).coerceAtLeast(0)
                movingLeft = true
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (gameOver) return true // Don't handle key events if the game is over

        val action = event?.action

        when (event?.keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    // Move the car to the right lane
                    targetCarPosition = (targetCarPosition + 1).coerceAtMost(smoothness - 1)
                    movingRight = true
                    return true
                } else if (action == KeyEvent.ACTION_UP) {
                    movingRight = false
                    return true
                }
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (action == KeyEvent.ACTION_DOWN) {
                    // Move the car to the left lane
                    targetCarPosition = (targetCarPosition - 1).coerceAtLeast(0)
                    movingLeft = true
                    return true
                } else if (action == KeyEvent.ACTION_UP) {
                    movingLeft = false
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}