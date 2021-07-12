package com.ktc.remote

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.DataOutputStream
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.engine.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.util.Identity.decode

class FloatingWidgetService : Service() {
    private var mWindowManager: WindowManager? = null
    private var mOverlayView: View? = null
    var Fab: FloatingActionButton? = null
    private lateinit var upbtn:MaterialButton
    private var disableTouch:Boolean = false
    private lateinit var context:Context
    private lateinit var remoteView: ConstraintLayout
    private var animShow: Animation? = null
    private  var animHide:Animation? = null

    @Nullable
    override fun onBind(intent: Intent): IBinder? {
        return null
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        context = this
        embeddedServer(Netty, 4444) {
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
                masking = false
            }
            routing {
                webSocket("/test") {
                    send("You are connected!")
                    for(frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        Toast.makeText(this@FloatingWidgetService,receivedText,Toast.LENGTH_SHORT).show()
                    }
                }
                webSocket("/command") {
                    for(frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        Toast.makeText(this@FloatingWidgetService,receivedText,Toast.LENGTH_SHORT).show()
                        val thread = Thread {
                            try {
                                var inst = Instrumentation()
                                inst.sendKeyDownUpSync(11);
                            }catch (e: Exception) {
                                Log.e("Exception", e.toString())
                            }
                        }
                        thread.start();
                    }
                    send("ack")
                }
            }
        }.start()
        setTheme(R.style.AppTheme)
        Log.d("Service", "onCreate")
        mOverlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val alpha = AlphaAnimation(0.5f, 0.5f)
        alpha.duration = 0 // Make animation instant

        alpha.fillAfter = true // Tell it to persist after the animation ends

        animShow = AnimationUtils.loadAnimation(this, R.anim.view_show);
        animHide = AnimationUtils.loadAnimation(this, R.anim.view_hide);

        // And then on your layout
        //Specify the view position
        params.gravity =
            Gravity.BOTTOM or Gravity.LEFT //Initially view will be added to bottom-left corner
        params.x = 0
        params.y = 100
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mOverlayView, params)
        Fab = mOverlayView!!.findViewById<View>(R.id.fabHead) as FloatingActionButton
        remoteView = mOverlayView!!.findViewById(R.id.remoteLayout) as ConstraintLayout
        remoteView.setBackgroundColor(0xaf2c3e50.toInt())
        Fab!!.setBackgroundColor(0xaf616161.toInt())
        Fab!!.setOnClickListener {
            if (remoteView.visibility == View.GONE) {
                val interpolator = OvershootInterpolator()
                ViewCompat.animate(Fab!!).rotation(180f).withLayer().setDuration(1000)
                    .setInterpolator(interpolator).start()
                remoteView.visibility = View.VISIBLE;
                remoteView.startAnimation(animShow)
            } else {
                val interpolator = OvershootInterpolator()
                ViewCompat.animate(Fab!!).rotation(0f).withLayer().setDuration(1000)
                    .setInterpolator(interpolator).start()
                remoteView.visibility = View.GONE
                remoteView.startAnimation(animHide)
            }
        }

        upbtn = mOverlayView!!.findViewById(R.id.btnup) as MaterialButton
        upbtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP );
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var leftBtn:MaterialButton = mOverlayView!!.findViewById(R.id.leftBtn) as MaterialButton
        leftBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var rightBtn:MaterialButton = mOverlayView!!.findViewById(R.id.rightBtn) as MaterialButton
        rightBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var okBtn:MaterialButton = mOverlayView!!.findViewById(R.id.okBtn) as MaterialButton
        okBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var downBtn:MaterialButton = mOverlayView!!.findViewById(R.id.downBtn) as MaterialButton
        downBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var menuBtn:MaterialButton = mOverlayView!!.findViewById(R.id.menu) as MaterialButton
        menuBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_MENU);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var homeBtn:MaterialButton = mOverlayView!!.findViewById(R.id.home) as MaterialButton
        homeBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var backBtn:MaterialButton = mOverlayView!!.findViewById(R.id.back) as MaterialButton
        backBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var volDown:MaterialButton = mOverlayView!!.findViewById(R.id.voldown) as MaterialButton
        volDown.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var volUp:MaterialButton = mOverlayView!!.findViewById(R.id.volup) as MaterialButton
        volUp.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_UP);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

        var muteBtn:MaterialButton = mOverlayView!!.findViewById(R.id.mute) as MaterialButton
        muteBtn.setOnClickListener {
            val thread = Thread {
                try {
                    var inst = Instrumentation()
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_MUTE);
//                    var p = Runtime.getRuntime().exec("su")
//                    val dos = DataOutputStream(p.outputStream)
//                    dos.writeBytes("input keyevent " + KeyEvent.KEYCODE_BACK+"\n")
//                    dos.writeBytes("exit\n")
//                    dos.flush()
//                    dos.close()
//                    p.waitFor()

                } catch (e: Exception) {
                    Log.e("Exception", e.toString())
                }
            }
            thread.start()
        }

//        Fab!!.setOnTouchListener(object : OnTouchListener {
//            private var initialX = 0
//            private var initialY = 0
//            private var initialTouchX = 0f
//            private var initialTouchY = 0f
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                when (event.action) {
//                    MotionEvent.ACTION_DOWN -> {
//
//                        //remember the initial position.
//                        initialX = params.x
//                        initialY = params.y
//                        disableTouch = false
//
//                        //get the touch location
//                        initialTouchX = event.rawX
//                        initialTouchY = event.rawY
//                        return true
//                    }
//                    MotionEvent.ACTION_UP -> {
//                        if (!disableTouch) {
//                            if (remoteView.visibility == View.GONE) {
//                                val interpolator = OvershootInterpolator()
//                                ViewCompat.animate(Fab!!).rotation(180f).withLayer().setDuration(1000)
//                                    .setInterpolator(interpolator).start()
//                                remoteView.visibility = View.VISIBLE;
//                                remoteView.startAnimation(animShow)
//                            } else {
//                                val interpolator = OvershootInterpolator()
//                                ViewCompat.animate(Fab!!).rotation(0f).withLayer().setDuration(1000)
//                                    .setInterpolator(interpolator).start()
//                                remoteView.visibility = View.GONE
//                                remoteView.startAnimation(animHide)
//                            }
//                        }
//                        return true
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val Xdiff = Math.round(event.rawX - initialTouchX).toFloat()
//                        val Ydiff = Math.round(event.rawY - initialTouchY).toFloat()
//                        disableTouch = true
//
//                        //Calculate the X and Y coordinates of the view.
//                        params.x = initialX + Xdiff.toInt()
//                        params.y = initialY + Ydiff.toInt()
//
//                        //Update the layout with new X & Y coordinates
//                        mWindowManager!!.updateViewLayout(mOverlayView, params)
//                        return true
//                    }
//                }
//                return false
//            }
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mOverlayView != null) mWindowManager!!.removeView(mOverlayView)
    }
}