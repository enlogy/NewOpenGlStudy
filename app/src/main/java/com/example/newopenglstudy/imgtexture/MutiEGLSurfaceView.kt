package com.example.newopenglstudy.imgtexture

import android.content.Context
import android.util.AttributeSet
import com.example.newopenglstudy.EGLSurfaceView

class MutiEGLSurfaceView : EGLSurfaceView {
     var mutiRender: MutiRender

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mutiRender = MutiRender(context)
        setRender(mutiRender)
    }

    fun setTextureId(textureId:Int,index:Int){
        mutiRender.setTextureIdAndIndex(textureId,index)
    }
}