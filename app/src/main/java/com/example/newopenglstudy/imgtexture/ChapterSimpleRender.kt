package com.example.newopenglstudy.imgtexture

import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import com.example.newopenglstudy.EGLSurfaceRender
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ChapterSimpleRender : EGLSurfaceRender {
    //绘制每一帧的回调
    override fun onDrawFrame() {
        //清空屏幕
        glClear(GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged( width: Int, heigh: Int) {
        //设置surface绘制的区域大小
        glViewport(0, 0, width, heigh)
    }

    override fun onSurfaceCreated() {
        //设置清屏颜色
        glClearColor(1f, 0f, 0f, 0f)

    }
}