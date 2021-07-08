package com.ktc.remote

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.FragmentActivity


class MainActivity : FragmentActivity() {

    private val DRAW_OVER_OTHER_APP_PERMISSION = 123

//    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//        if (event.action === KeyEvent.ACTION_DOWN) {
//            Toast.makeText(this, "Down button" + event.keyCode, Toast.LENGTH_LONG).show()
//        } else if (event.action === KeyEvent.ACTION_UP) {
//            Toast.makeText(this, "UP button" + event.keyCode, Toast.LENGTH_LONG).show()
//        }
//        return true
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askForSystemOverlayPermission()


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this@MainActivity)) {
            startService(Intent(this@MainActivity, FloatingWidgetService::class.java))
        } else {
            errorToast()
        }
        val ai = applicationInfo
        Toast.makeText(this@MainActivity,"Remote"+"Application UID: " + ai.uid + ", SYSTEM_UID: " + Process.SYSTEM_UID, Toast.LENGTH_LONG).show()
    }

    private fun askForSystemOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION)
        }
    }
    override fun onPause() {
        super.onPause()


        // To prevent starting the service if the required permission is NOT granted.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            startService(
                Intent(
                    this@MainActivity,
                    FloatingWidgetService::class.java
                ).putExtra("activity_background", true)
            )
            finish()
        } else {
            errorToast()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    //Permission is not available. Display error text.
                    errorToast()
                    finish()
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun errorToast() {
        Toast.makeText(
            this,
            "Draw over other app permission not available. Can't start the application without the permission.",
            Toast.LENGTH_LONG
        ).show()
    }
}