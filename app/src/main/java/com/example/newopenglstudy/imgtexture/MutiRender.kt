package com.example.newopenglstudy.imgtexture

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.example.newopenglstudy.EGLSurfaceRender
import com.example.newopenglstudy.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class MutiRender(var context: Context) : EGLSurfaceRender {

    private var vertexPoints = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f,
        //添加三角形
        -0.5f,-0.5f,
        0.5f,-0.5f,
        0f,0.5f
    )
    private var fragmentPoints = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer
    private var vPosition = 0
    private var fPosition = 0
    private var program = 0
    private lateinit var vbo: IntArray
    private var mTextureId: Int = 0
    private var index: Int = 0
    private var triangleImgTextureId = 0
    fun setTextureIdAndIndex(textureId: Int, index: Int) {
        mTextureId = textureId
        this.index = index
    }

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexPoints)
        vertexBuffer.position(0)
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentPoints)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated() {
        when(index){
            0 -> program = ShaderUtil.linkProgram(context, R.raw.vertex_shader, R.raw.fragment_shader1)
            1 -> program = ShaderUtil.linkProgram(context, R.raw.vertex_shader, R.raw.fragment_shader2)
            2 -> program = ShaderUtil.linkProgram(context, R.raw.vertex_shader, R.raw.fragment_shader3)
        }
//        GLES20.glUseProgram(program)
        //属性
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")

        //vbo
        vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexPoints.size * 4 + fragmentPoints.size * 4,
            null,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexPoints.size * 4, vertexBuffer)
        GLES20.glBufferSubData(
            GLES20.GL_ARRAY_BUFFER,
            vertexPoints.size * 4,
            fragmentPoints.size * 4,
            fragmentBuffer
        )
        //赋值后可以解绑
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        mTextureId = loadTexture(R.mipmap.timg)
        triangleImgTextureId = loadTexture(R.mipmap.mimg)
    }
    private fun loadTexture(resId: Int): Int {
        val textureId = IntArray(1)
        GLES20.glGenTextures(1, textureId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])
        //设置环绕，顶点坐标超出纹理坐标范围时作用，s==x t==y GL_REPEAT 重复
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        //过滤（纹理像素映射到坐标点）：（GL_TEXTURE_MIN_FILTER缩小、GL_TEXTURE_MAG_FILTER放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        return textureId[0]
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {

        GLES20.glClearColor(0f, 1.0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        //shader赋值
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
//        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,8,vertexBuffer)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexPoints.size * 4)
//        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
//        Log.e("MutiLog","mTextureId = $mTextureId")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)



        //绘制三角形美女图
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        //shader赋值
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 32)
//        GLES20.glVertexAttribPointer(vPosition,2,GLES20.GL_FLOAT,false,8,vertexBuffer)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexPoints.size * 4)
//        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, triangleImgTextureId)
//        Log.e("MutiLog","mTextureId = $mTextureId")
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

}