package com.example.newopenglstudy

interface EGLSurfaceRender {
     fun onDrawFrame()
     fun onSurfaceChanged(width: Int, height: Int)
     fun onSurfaceCreated()
}