package com.example.newopenglstudy

import android.opengl.GLES20

class MyEGLSurfaceRender : EGLSurfaceRender {
    override fun onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onSurfaceCreated() {

    }

}