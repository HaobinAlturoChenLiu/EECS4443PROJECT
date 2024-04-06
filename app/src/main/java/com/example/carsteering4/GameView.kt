package com.example.carsteering4

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class GameView(context: Context) : View(context), SensorEventListener {

    private var myPaint: Paint? = null
    private var myCarPosition = 0
    private var targetCarPosition = myCarPosition // New variable to store target position
    private var movingRight = false // Flag to indicate movement to the right
    private var movingLeft = false // Flag to indicate movement to the left
    private var speed = 1
    private var time = 0
    private var score = 0
    private val coins = ArrayList<HashMap<String, Any>>()
    private val smoothness = 10
    private var coinSize = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var gameOver = false

    // Gyroscope variables
    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    private var gyroscopeValues = FloatArray(3) // Array to store gyroscope values

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

    private fun moveCarRight() {
        targetCarPosition = (targetCarPosition + 1).coerceIn(0, smoothness - 3)
        movingRight = true
        movingLeft = false
    }

    private fun moveCarLeft() {
        targetCarPosition = (targetCarPosition - 1).coerceIn(2, smoothness - 2)
        movingLeft = true
        movingRight = false
    }

    private fun stopCarMovement() {
        movingLeft = false
        movingRight = false
    }
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Initialize sensor manager
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Get gyroscope sensor
        gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        // Register gyroscope sensor listener
        sensorManager?.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Unregister gyroscope sensor listener
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            // Update gyroscope values
            gyroscopeValues = event.values
            // Adjust car position based on gyroscope data
            handleGyroscopeData()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    private fun handleGyroscopeData() {
        // Adjust car position based on gyroscope data
        // Increase the threshold to make it less sensitive
        val tiltThreshold = 0.6f // Adjust threshold as needed
        if (gyroscopeValues[0] > tiltThreshold) {
            moveCarRight()
        } else if (gyroscopeValues[0] < -tiltThreshold) {
            moveCarLeft()
        } else {
            stopCarMovement()
        }
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
            // Generate random lane position within the range of center lanes
            map["lane"] = (smoothness / 2 - 3 until smoothness / 2 + 3).random()
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

        // Rotate the car image based on movement direction
        val rotationAngle = when {
            movingRight -> 15f // Tilt right when moving right
            movingLeft -> -15f // Tilt left when moving left
            else -> 0f // No tilt if not moving
        }
        canvas.save()
        canvas.rotate(rotationAngle, carX + carWidth / 2.toFloat(), carY + carHeight / 2.toFloat())

        d.setBounds(carX, carY, carX + carWidth, carY + carHeight)
        d.draw(canvas)
        canvas.restore()

        myPaint!!.color = Color.GREEN
        var highScore = 0

        val indicatorHeight = 20 // Height of the indicator
        val coinsToRemove = mutableListOf<HashMap<String, Any>>() // List to store coins that need to be removed
        for (coin in coins) {
            try {
                val coinLane = (coin["lane"] as Int).coerceIn(0, smoothness - 1)
                val laneWidth = viewWidth / smoothness
                val indicatorX = coinLane * laneWidth + (laneWidth - carWidth) / 2.toFloat()
                val indicatorY = (viewHeight - 2 - indicatorHeight).toFloat() // Position above the car
                // Draw the indicator
                myPaint!!.color = Color.YELLOW // Adjust color as needed
                canvas.drawRect(indicatorX, indicatorY, indicatorX + laneWidth.toFloat(), indicatorY + indicatorHeight.toFloat(), myPaint!!)

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
                    coinsToRemove.add(coin) // Add the collected coin to the removal list
                    score++ // Increment the score
                    speed = 1+ abs(score / 10)
                    if (score > highScore) {
                        highScore = score
                    }
                }

                if (coinY > viewHeight + carHeight) {
                    coinsToRemove.add(coin) // Add the coin to be removed if it goes beyond the screen
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Remove collected coins and coins that went beyond the screen
        coins.removeAll(coinsToRemove)

        myPaint!!.color = Color.WHITE
        myPaint!!.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint!!)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint!!)

        invalidate()
    }

    // Define a variable to track whether the volume key is being held

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (gameOver) return true // Don't handle key events if the game is over

        when (event?.action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    moveCarRight()
                    return true
                }

                if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    moveCarLeft()
                    return true
                }
            }
            KeyEvent.ACTION_UP -> {
                stopCarMovement()
                    return true

            }
        }

        return super.dispatchKeyEvent(event)
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gameOver) return true // Don't handle touch events if the game is over

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if the touch event occurred on the left or right half of the screen
                val touchX = event.x
                if (touchX < viewWidth / 2) {
                     moveCarLeft()
                } else {
                    moveCarRight()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                stopCarMovement()
                return true
            }
        }

        return super.onTouchEvent(event)
    }

}