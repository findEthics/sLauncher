package com.example.slauncher

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class GestureManager(
    context: Context,
    private val listener: GestureListener
) : View.OnTouchListener {
    
    interface GestureListener {
        fun onSwipeUp()
        fun onSwipeDown()
    }
    
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        
        private val swipeThreshold = 100
        private val swipeVelocityThreshold = 100
        
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
        
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            
            if (abs(diffY) > abs(diffX)) {
                if (abs(diffY) > swipeThreshold && abs(velocityY) > swipeVelocityThreshold) {
                    if (diffY < 0) {
                        // Swipe up
                        listener.onSwipeUp()
                        return true
                    } else {
                        // Swipe down
                        listener.onSwipeDown()
                        return true
                    }
                }
            }
            return false
        }
    })
    
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return if (event != null) {
            gestureDetector.onTouchEvent(event)
        } else {
            false
        }
    }
    
    fun attachToView(view: View) {
        view.setOnTouchListener(this)
    }
}