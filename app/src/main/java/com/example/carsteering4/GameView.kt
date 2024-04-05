package com.example.carsteering4

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

class GameView(var c: Context, var gameActivity: GameActivity): View(c) {

    private var myPaint: Paint? = null
    private var myCarPosition = 0
    private var speed = 1
    private var time = 0
    private var score = 0
    private val coins = ArrayList<HashMap<String, Any>>()
    private val smoothness = 15
    var viewWidth = 0
    var viewHeight = 0
    private var coinSize = 0

    init {
        myPaint = Paint()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        viewWidth = this.measuredWidth
        viewHeight = this.measuredHeight

        if (time % 700 < 10 + speed) {
            val map = HashMap<String, Any>()
            map["lane"] = (0..9).random() // Update to 10 lanes
            map["startTime"] = time
            coins.add(map)
        }
        time = time + 10 + speed
        val carWidth = viewWidth / 10
        val carHeight = viewWidth / 10
        myPaint!!.style = Paint.Style.FILL

        // Calculate the position to draw the car
        val carX = myCarPosition * viewWidth / smoothness + (viewWidth / smoothness - carWidth) / 2
        val carY = viewHeight - 2 - carHeight

        val d = resources.getDrawable(R.drawable.car, null)
        d.setBounds(carX, carY, carX + carWidth, carY + carHeight)
        d.draw(canvas!!)
        myPaint!!.color = Color.GREEN
        var highScore = 0

        val iterator = coins.iterator()
        while (iterator.hasNext()) {
            val coin = iterator.next()
            try {
                val coinX = coin["lane"] as Int * viewWidth / 10 + (viewWidth / 10 - coinSize) / 2
                var coinY = time - coin["startTime"] as Int

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
                val distance = Math.sqrt(
                    ((carHitboxCenterX - coinCenterX) * (carHitboxCenterX - coinCenterX)).toDouble() +
                            ((carHitboxCenterY - coinCenterY) * (carHitboxCenterY - coinCenterY)).toDouble()
                )
                if (distance < (carHitboxWidth + coinSize) / 2) {
                    iterator.remove() // Remove the collected coin
                    score++ // Increment the score
                    speed = 1 + Math.abs(score / 8)
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



    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                // Get the X coordinate of the touch event
                val x1 = event.x

                // Calculate the width of each lane
                val laneWidth = viewWidth / smoothness
                // Determine the lane in which the touch event occurred
                val touchedLane = (x1 / laneWidth).toInt()

                // Update the car's position based on the touched lane
                myCarPosition = touchedLane
            }
            MotionEvent.ACTION_UP -> {
                // Handle touch up event if needed
            }
        }
        // Redraw the view to reflect the updated car position
        invalidate()
        return true
    }
}
