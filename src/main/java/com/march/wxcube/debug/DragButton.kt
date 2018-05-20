package com.march.wxcube.debug

import android.content.Context
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView

/**
 * CreateAt : 2018/5/19
 * Describe :
 *
 * @author chendong
 */
class DragButton(context: Context) : Button(context) {

    private var lastX: Int = 0
    private var lastY: Int = 0
    private var lastDownX: Int = 0
    private var lastDownY: Int = 0
    private var listener: OnClickListener? = null


    override fun onTouchEvent(event: MotionEvent): Boolean {

        // 绝对位置
        val rawX = event.rawX.toInt()
        val rawY = event.rawY.toInt()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = rawX
                lastY = rawY

                lastDownX = lastX
                lastDownY = lastY
            }
            MotionEvent.ACTION_MOVE -> {
                // 计算偏移量
                val offSetX = rawX - lastX
                val offSetY = rawY - lastY

                // 在当前的left、top、right、bottom 基础上加上偏移量
                layout(left + offSetX,
                        top + offSetY,
                        right + offSetX,
                        bottom + offSetY)

                // 重新设置坐标
                lastX = rawX
                lastY = rawY
            }
            MotionEvent.ACTION_UP -> if (Math.abs(lastDownX - lastX) <= 10 && Math.abs(lastDownY - lastY) <= 10) {
                listener?.onClick(this)
            }
        }
        return true
    }

    override fun setOnClickListener(l: OnClickListener?) {
        this.listener = l
    }
}