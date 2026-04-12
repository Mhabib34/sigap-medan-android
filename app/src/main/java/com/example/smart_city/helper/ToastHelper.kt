package com.example.smart_city.helper

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.smart_city.R

object ToastHelper {

    fun showSuccess(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.bg_toast_success, "✓")
    }

    fun showError(context: Context, message: String) {
        showCustomToast(context, message, R.drawable.bg_toast_error, "✕")
    }

    private fun showCustomToast(context: Context, message: String, bgDrawable: Int, icon: String) {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.toast_success, null)

        layout.findViewById<TextView>(R.id.tvToastIcon).text = icon
        layout.findViewById<TextView>(R.id.tvToastMessage).text = message
        layout.setBackgroundResource(bgDrawable)

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(Gravity.TOP, 0, 80)
            show()
        }
    }
}