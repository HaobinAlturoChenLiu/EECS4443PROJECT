package com.example.carsteering4

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.sqrt

// Enum to represent different control modes
enum class ControlMode {
    GYROSCOPE,
    TOUCH,
    VOLUME
}

class GameView(context: Context) : View(context), SensorEventListener {

    // Paint object for drawing
    private var myPaint: Paint? = null

    // Car position variables
    private var myCarPosition = 0
    private var targetCarPosition = myCarPosition
    private var movingRight = false
    private var movingLeft = false

    // Game variables
    private var speed = 1
    private var time = 0
    private var score = 0
    private val coins = ArrayList<HashMap<String, Any>>()
    private val smoothness = 10
    private var coinSize = 0
    private var viewWidth = 0
    private var viewHeight = 0
    private var gameOver = false

    // Control mode variable
    private var controlMode = ControlMode.VOLUME

    // Gyroscope variables
    private var sensorManager: SensorManager? = null
    private var gyroscopeSensor: Sensor? = null
    private var gyroscopeValues = FloatArray(3)

    // Game over listener interface
    var gameOverListener: GameOverListener? = null

    // Interface for game over listener
    interface GameOverListener {
        fun onGameOver()
    }

    // Initialization block
    init {
        // Initialize Paint object
        myPaint = Paint()
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        restartTimer()
    }

    // Method to move car to the right
    private fun moveCarRight() {
        targetCarPosition = (targetCarPosition + 1).coerceIn(0, smoothness - 3)
        movingRight = true
        movingLeft = false
    }

    // Method to move car to the left
    private fun moveCarLeft() {
        targetCarPosition = (targetCarPosition - 1).coerceIn(2, smoothness - 2)
        movingLeft = true
        movingRight = false
    }

    // Method to stop car movement
    private fun stopCarMovement() {
        movingLeft = false
        movingRight = false
    }

    // Makes Volume KeyDown work
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscopeSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (controlMode == ControlMode.GYROSCOPE) {
            sensorManager?.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    // Makes Volume KeyDown work
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (controlMode == ControlMode.GYROSCOPE) {
            sensorManager?.unregisterListener(this)
        }
    }

    // Rendering the game (I hope you like pasta because there is a lot of spaghetti code ahead)
    // Code based from this video: https://www.youtube.com/watch?v=fs0LuDvVVb0
    @SuppressLint("DrawAllocation", "UseCompatLoadingForDrawables")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gameOver) {
            // Draw game over message
            myPaint!!.color = Color.WHITE
            myPaint!!.textSize = 80f
            canvas.drawText("Game Over", (viewWidth / 2 - 200).toFloat(), (viewHeight / 2 - 50).toFloat(), myPaint!!)
            canvas.drawText("Score : $score", (viewWidth / 2 - 180).toFloat(), (viewHeight / 2 + 50).toFloat(), myPaint!!)
            (context as? AppCompatActivity)?.runOnUiThread {
                (context as? AppCompatActivity)?.findViewById<Button>(R.id.restartBtn)?.visibility = View.VISIBLE
            }
            return
        }

        // Update view dimensions
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        // Add coins to the game
        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (smoothness / 2 - 3 until smoothness / 2 + 3).random()
            map["startTime"] = time
            coins.add(map)
        }
        time += 10 + speed
        val carWidth = viewWidth / 10
        val carHeight = viewWidth / 10
        myPaint!!.style = Paint.Style.FILL

        val carX = targetCarPosition * viewWidth / smoothness + (viewWidth / smoothness - carWidth) / 2
        val carY = viewHeight - 2 - carHeight

        val d = resources.getDrawable(R.drawable.car, null)

        val rotationAngle = when {
            movingRight -> 15f
            movingLeft -> -15f
            else -> 0f
        }
        canvas.save()
        canvas.rotate(rotationAngle, carX + carWidth / 2.toFloat(), carY + carHeight / 2.toFloat())

        d.setBounds(carX, carY, carX + carWidth, carY + carHeight)
        d.draw(canvas)
        canvas.restore()

        myPaint!!.color = Color.GREEN

        val indicatorHeight = 20
        val coinsToRemove = mutableListOf<HashMap<String, Any>>()
        for (coin in coins) {
            try {
                val coinLane = (coin["lane"] as Int).coerceIn(0, smoothness - 1)
                val laneWidth = viewWidth / smoothness
                val indicatorX = coinLane * laneWidth + (laneWidth - carWidth) / 2.toFloat()
                val indicatorY = (viewHeight - 2 - indicatorHeight).toFloat()
                myPaint!!.color = Color.YELLOW
                canvas.drawRect(indicatorX, indicatorY, indicatorX + laneWidth.toFloat(), indicatorY + indicatorHeight.toFloat(), myPaint!!)

                val coinX = coinLane * laneWidth + laneWidth / 2 - coinSize / 2
                val coinY = time - coin["startTime"] as Int

                val d2 = resources.getDrawable(R.drawable.coin, null)

                coinSize = viewWidth / 30

                d2.setBounds(coinX, coinY - coinSize, coinX + coinSize, coinY)
                d2.draw(canvas)

                val carHitboxWidth = carWidth * 0.6f
                val carHitboxHeight = carHeight * 0.6f
                val carHitboxX = carX + (carWidth - carHitboxWidth) / 2
                val carHitboxY = carY + (carHeight - carHitboxHeight) / 2
                val carHitboxCenterX = carHitboxX + carHitboxWidth / 2
                val carHitboxCenterY = carHitboxY + carHitboxHeight / 2
                val coinCenterX = coinX + coinSize / 2
                val coinCenterY = coinY - coinSize / 2

                val distance = sqrt(((carHitboxCenterX - coinCenterX) * (carHitboxCenterX - coinCenterX)).toDouble() + ((carHitboxCenterY - coinCenterY) * (carHitboxCenterY - coinCenterY)).toDouble())
                if (distance < (carHitboxWidth + coinSize) / 2) {
                    coinsToRemove.add(coin)
                    score++
                    speed = 1 + abs(score / 20)
                }

                if (coinY > viewHeight + carHeight) {
                    coinsToRemove.add(coin)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        coins.removeAll(coinsToRemove)

        myPaint!!.color = Color.WHITE
        myPaint!!.textSize = 40f
        canvas.drawText("Score : $score", 80f, 80f, myPaint!!)
        canvas.drawText("Speed : $speed", 380f, 80f, myPaint!!)

        invalidate()
    }

    // Called when sensor data is changed (Gyroscope)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            gyroscopeValues = event.values
            handleGyroscopeData()
        }
    }

    // Empty Method so it compiles
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // Method to handle gyroscope data for controlling the car
    private fun handleGyroscopeData() {
        val tiltThreshold = 0.6f
        if (gyroscopeValues[0] > tiltThreshold) {
            moveCarRight()
        } else if (gyroscopeValues[0] < -tiltThreshold) {
            moveCarLeft()
        } else {
            stopCarMovement()
        }
    }

    // Key events for controlling the car (Volume Buttons)
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (gameOver) return true
        if (controlMode == ControlMode.VOLUME) {
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
        }

        return super.dispatchKeyEvent(event)
    }

    // Handle touch events for controlling the car
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gameOver) return true
        if (controlMode == ControlMode.TOUCH) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
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
        }

        return super.onTouchEvent(event)
    }

    // Method to set control mode
    fun setControlMode(mode: ControlMode) {
        controlMode = mode
        when (mode) {
            ControlMode.GYROSCOPE -> sensorManager?.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME)
            ControlMode.TOUCH -> sensorManager?.unregisterListener(this)
            ControlMode.VOLUME -> sensorManager?.unregisterListener(this)
        }
    }

    // Method for resetting timer (90s for game to end by itself)
    private fun restartTimer() {
        val handler = android.os.Handler()
        handler.postDelayed({
            gameOver = true
            invalidate()
            gameOverListener?.onGameOver()
        }, 90000)
    }

    // Method to reset game state
    fun resetGame() {
        // Reset game state variables
        myCarPosition = 0
        targetCarPosition = myCarPosition
        movingRight = false
        movingLeft = false
        speed = 1
        time = 0
        score = 0
        coins.clear()
        gameOver = false

        // Restart the timer for game over after 90 seconds
        restartTimer()

        // Invalidate the view to trigger redraw
        invalidate()
    }
}