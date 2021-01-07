package com.example.newopenglstudy.imgtexture

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.newopenglstudy.R
import kotlinx.android.synthetic.main.activity_imgtexture.*

class ImgTextureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val deviceConfigurationInfo = am.deviceConfigurationInfo
        val supportEs2 = deviceConfigurationInfo.reqGlEsVersion >= 0x20000
                || Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 &&
                Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
        if (supportEs2) {
            //设置OpenGL的版本
            Log.e("Activity", "support GLES2")
        }
        setContentView(R.layout.activity_imgtexture)
        val imgRender = ImgTextureRender(this)
        mSurfaceView.setRender(imgRender)
        imgRender.onRenderCreateListener =
            object : ImgTextureRender.OnRenderCreateListener {
                override fun onCreate(textureId: Int) {
                    mSurfaceView.postDelayed({
                        if (mContent.childCount > 0) mContent.removeAllViews()
                        for (i in 0..2) {
                        val eglSurfaceView = MutiEGLSurfaceView(this@ImgTextureActivity)
                        eglSurfaceView.setSurfaceAndEGLContext(
                            null,
                            mSurfaceView.getEglContext()
                        )
                        eglSurfaceView.setTextureId(textureId,i)
                        val lp = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        lp.width = 200
                        lp.height = 300
                        eglSurfaceView.layoutParams = lp
                        mContent.addView(eglSurfaceView)
                        }
                    },10)

                }
            }


    }
}