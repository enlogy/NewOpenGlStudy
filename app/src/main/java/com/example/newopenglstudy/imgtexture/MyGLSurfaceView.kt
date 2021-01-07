package com.example.newopenglstudy.imgtexture

import android.content.Context
import android.util.AttributeSet
import com.example.newopenglstudy.EGLSurfaceView

class MyGLSurfaceView:EGLSurfaceView {
//    var imgTextureRender:ImgTextureRender
    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
//        imgTextureRender = ImgTextureRender(context!!)
//        setRender(imgTextureRender)
        setRender(MutiRender(context!!))
    }

}