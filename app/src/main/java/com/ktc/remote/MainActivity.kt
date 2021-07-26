package com.ktc.remote

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.format.Formatter
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.lang.String.format
import java.net.InetAddress


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


        if (Settings.canDrawOverlays(this@MainActivity)) {
            startService(Intent(this@MainActivity, FloatingWidgetService::class.java))
        } else {
            errorToast()
        }
        val ai = applicationInfo
        Toast.makeText(this@MainActivity,"Remote"+"Application UID: " + ai.uid + ", SYSTEM_UID: " + Process.SYSTEM_UID, Toast.LENGTH_LONG).show()
        val ctx:Context = applicationContext
        val wm: WifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip:String = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)
        val genQr: Button = findViewById<Button>(R.id.generateQr)
        genQr.setOnClickListener{
            val width = 300
            val height = 300
            val bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888)
            val codeWriter = MultiFormatWriter()
            try{
                val bitMatrix = codeWriter.encode(ip,BarcodeFormat.QR_CODE,width,height)
                for (x in 0 until width){
                    for (y in 0 until height){
                        bitmap.setPixel(x,y,if(bitMatrix[x,y])Color.BLACK else Color.WHITE)
                    }
                }
            }catch(e:WriterException){
                Log.e("Remote","generateQRCode: ${e.message}")
            }
            val qrcode: ImageView = findViewById<ImageView>(R.id.qrcode)
            qrcode.setImageBitmap(bitmap)
            val ipAddress: TextView = findViewById<TextView>(R.id.ipaddr)
            ipAddress.text = ip
            val card:CardView = findViewById<CardView>(R.id.cardView2)
            if(card.visibility == View.GONE){
                card.visibility = View.VISIBLE
            }else{
                card.visibility = View.GONE
            }
        }

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