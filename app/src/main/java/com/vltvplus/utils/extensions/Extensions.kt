package com.vltvplus.utils.extensions

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

fun View.show() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
fun Fragment.toast(msg: String) = requireContext().toast(msg)

fun Long.toMinutes(): String {
    val m = (this / 1000 / 60).toInt()
    val s = (this / 1000 % 60).toInt()
    return String.format("%02d:%02d", m, s)
}
