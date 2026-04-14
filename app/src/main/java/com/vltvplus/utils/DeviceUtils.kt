package com.vltvplus.utils

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.view.KeyEvent

object DeviceUtils {

    fun isTV(context: Context): Boolean {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) return true
        if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) return true
        val model = Build.MODEL.lowercase()
        return model.contains("firetv") || model.contains("fire tv") ||
               model.contains("androidtv") || model.contains("tvbox") ||
               model.contains("mi box") || model.contains("mibox") ||
               model.contains("shield") || model.contains("chromecast")
    }

    fun isMobile(context: Context): Boolean = !isTV(context)

    fun isFireStick(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL.lowercase()
        return manufacturer.contains("amazon") || model.contains("fire") || model.contains("aftt")
    }

    fun isMiBox(): Boolean {
        val model = Build.MODEL.lowercase()
        return model.contains("mi box") || model.contains("mibox") || model.contains("mbox")
    }
}

object RemoteKeyUtils {
    fun isDpadCenter(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER

    fun isDpadUp(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_UP
    fun isDpadDown(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_DOWN
    fun isDpadLeft(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
    fun isDpadRight(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT

    fun isBack(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_BACK ||
            keyCode == KeyEvent.KEYCODE_ESCAPE

    fun isPlayPause(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE

    fun isRewind(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_MEDIA_REWIND ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS

    fun isFastForward(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ||
            keyCode == KeyEvent.KEYCODE_MEDIA_NEXT

    fun isChannelUp(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
            keyCode == KeyEvent.KEYCODE_PAGE_UP

    fun isChannelDown(keyCode: Int): Boolean = keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
            keyCode == KeyEvent.KEYCODE_PAGE_DOWN
}
