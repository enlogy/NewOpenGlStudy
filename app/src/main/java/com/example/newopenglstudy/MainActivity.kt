package com.example.newopenglstudy

import android.opengl.GLES20
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        initSurfaceView()
        mSurfaceView.setRender(MyEGLSurfaceRender())

    }

    private fun initSurfaceView() {
//        mSurfaceView.setRenderer()
        mSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Thread {
                    val eglHelper = EGLHelper()
                    eglHelper.configure(holder.surface,null)
//                    while (true){
                        Thread.sleep(16)
                        GLES20.glViewport(0,0,width,height)
                        GLES20.glClearColor(1.0f,0.0f,0.0f,1.0f)
                        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                        eglHelper.swapBuffers()
//                    }

                }.start()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }

            override fun surfaceCreated(holder: SurfaceHolder) {

            }
        })
    }
}