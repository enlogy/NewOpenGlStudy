package com.example.newopenglstudy.imgtexture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.example.newopenglstudy.EGLSurfaceRender
import com.example.newopenglstudy.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class ImgTextureRender(var context: Context) : EGLSurfaceRender {
    //顶点坐标(中心0.0)
    private val vertexPoints = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
        // OpenGL标准坐标(左下角0.0，右上角1.1)
    private val fragmentPoints = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f
    )
    //Android 纹理坐标(左上角0.0，右下角1.1)
    private var fragmentPoints2 = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    private lateinit var vertexData: FloatBuffer
    private lateinit var fragmentData: FloatBuffer
    private lateinit var fragmentData2: FloatBuffer
    private var vPosition = 0
    private var fPosition = 0
    private lateinit var bitmap: Bitmap
    private var sampler2D = 0
    private lateinit var vbo: IntArray
    private lateinit var fbo: IntArray
    lateinit var fboTextureId: IntArray
    private var imgTexture: Int = 0
    private var program = 0
    private val matrix = FloatArray(16)
    private var uMatrix = 0
    var onRenderCreateListener:OnRenderCreateListener? = null
    private var width = 0
    private var height = 0
    var TAG = "ImgRender"
    /**
     * 使用FboRender的原理是，创建新的Program，设置的东西跟原来的Program互不干扰，需要用到时候调用useProgram就行
     */
    private val fboRender = FboRender(context)
    override fun onDrawFrame() {
        Log.i(TAG,"onDrawFrame")
        //设置画布大小跟fbo画布一致
        GLES20.glViewport(0, 0, 1080, 1920)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        //1 矩阵的个数
        //fbo和矩阵同时使用时，如果默认fbo按照竖屏大小来绑定texture，则会导致横屏时矩阵作用后的显示不正常，原因是fbo竖屏的画布大小跟横屏的画布大小不一致
        //矩阵保持图片比例放大(正交投影)作用于顶点坐标
        GLES20.glUniformMatrix4fv(uMatrix,1,false, matrix,0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glEnableVertexAttribArray(vPosition)
        // vPosition 赋值的字段
        // 2一个点占用多少个float，这里xy合起来两个
        // GLES20.GL_FLOAT 数据格式
        // true or false 不在范围-1 1的自动转成该范围的，一般正常定义的坐标写false就行了
        // stride 8 一个坐标占用了多少内存 ,这里一个坐标占用2个float，一个float占用4个bit ，因此2*4 =8
        //vertexData赋值的数据：两种方式 1.cpu -> gpu 2. gpu读取显存vbo
        //cpu -> gpu
//        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexData)
        //gpu读取显存vbo
        //offset 在vbo读取数据时的偏移量
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(fPosition)
        //cpu -> gpu
//        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentData)
        //gpu读取显存vbo
        // offset : vertexPoints.size * 4 偏移的显存大小，作为起始位置
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexPoints.size * 4+fragmentPoints.size*4)
        //数据赋值后才可以解绑vbo，不然赋值时在vbo上取不了数据
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)

        //设置图片
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imgTexture)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        //显示fbo中的texture,此处显示的为倒像，因为OPENGL中的坐标跟片段着色器不一样
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,vbo[0])
////        GLES20.glEnableVertexAttribArray(vPosition)
//        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
//////        GLES20.glEnableVertexAttribArray(fPosition)
//        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexPoints.size*4+fragmentPoints.size*4)
//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER,0)
//
//
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,fboTextureId[0])
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        //恢复实际窗口大小
        GLES20.glViewport(0, 0, width, height)
        //绘制fbo中的texture出来,正向图形
        fboRender.onDraw(fboTextureId[0])
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
        if (width > height){
            //横屏
            //left right 屏幕宽度和照片宽度等比缩放后的比例 = width : 等比缩放后的照片宽度
            // height/1050f 屏幕和照片高度比 ,height/1050f*700f 屏幕和照片高度比 * 照片宽度 = 等比缩放后的照片宽度
            // 1050f 照片高度 700f照片宽度
            Matrix.orthoM(matrix,0,-width/(height/1050f*700f),width/(height/1050f*700f),-1f,1f,-1f,1f)
        }else{
            //竖屏
            Matrix.orthoM(matrix,0,-1f,1f,-height/(width/700f*1050f),height/(width/700f*1050f),-1f,1f)
        }
        //矩阵旋转（左手坐标）
        //a 正数顺时针旋转 负数逆时针旋转
        //x = 1 基于X轴来旋转，其他yz置0
        Matrix.rotateM(matrix,0,180f,1f,0f,0f)
        fboRender.onChange(width, height)
    }

    override fun onSurfaceCreated() {
        fboRender.onCreate()

        vertexData = ByteBuffer.allocateDirect(vertexPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexPoints)
        vertexData.position(0)
        fragmentData = ByteBuffer.allocateDirect(fragmentPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentPoints)
        fragmentData.position(0)

        fragmentData2 = ByteBuffer.allocateDirect(fragmentPoints2.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(fragmentPoints2)
        fragmentData2.position(0)

        //创建VBO，使用显存存储数据，省去cpu->gpu获取数据过程
        vbo = IntArray(1)
        //1 创建 顶点缓冲的个数 vbo
        //vbo 储存 vbo对象
        //0 offset
        GLES20.glGenBuffers(1, vbo, 0)
        //绑定vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        //给vbo分配需要的缓存空间
        //vertexPoints.size * 4 + fragmentPoints.size * 4 分配显存大小
        // null 只是创建不赋值
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexPoints.size * 4 + fragmentPoints.size * 4 + fragmentPoints2.size * 4,
            null,
            GLES20.GL_STATIC_DRAW
        )
        //vbo赋值
        // offset 偏移起始位置
        //size 需要赋值的显存长度
        // vertexData or fragmentData ,赋值的数据
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexPoints.size * 4, vertexData)
        GLES20.glBufferSubData(
            GLES20.GL_ARRAY_BUFFER,
            vertexPoints.size * 4,
            fragmentPoints.size * 4,
            fragmentData
        )
        GLES20.glBufferSubData(
            GLES20.GL_ARRAY_BUFFER,
            vertexPoints.size * 4 + fragmentPoints.size * 4,
            fragmentPoints2.size * 4,
            fragmentData2
        )
        //解绑vbo
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        program = ShaderUtil.linkProgram(context, R.raw.vertex_shader_m, R.raw.fragment_shader)
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")
        sampler2D = GLES20.glGetUniformLocation(program, "sTexture")
        uMatrix = GLES20.glGetUniformLocation(program,"u_Matrix")
        //FBO
        //创建FBO缓冲对象
        fbo = IntArray(1)
        GLES20.glGenBuffers(1, fbo, 0)
        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
//        检查FBO绑定是否成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
        } else {
            //success
        }

        //创建离屏缓冲用的texture
        //创建纹理
        fboTextureId = IntArray(1)
        //1创建纹理的个数
        //textureId存储创建的纹理id
        GLES20.glGenTextures(1, fboTextureId, 0)
        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTextureId[0])
//        //激活第一个纹理
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
//        //把sampler2D和第一个纹理绑定
//        GLES20.glUniform1i(sampler2D,0)
        //设置环绕，顶点坐标超出纹理坐标范围时作用，s==x t==y GL_REPEAT 重复
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        //过滤（纹理像素映射到坐标点）：（GL_TEXTURE_MIN_FILTER缩小、GL_TEXTURE_MAG_FILTER放大：GL_LINEAR线性）
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
//        设置FBO分配内存大小（要在绑定了texture后调用）
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            1080,
            1920,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        );
//        把纹理绑定到FBO
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            fboTextureId[0],
            0
        )
        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.timg)
        imgTexture = loadTexture(R.mipmap.timg)

        //分享texture给外部使用
        onRenderCreateListener?.onCreate(fboTextureId[0])
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

    interface OnRenderCreateListener{
        fun onCreate(textureId:Int)
    }
}