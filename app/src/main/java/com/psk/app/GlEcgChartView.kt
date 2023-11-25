package com.psk.app

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GlEcgChartView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        // 设置 OpenGL ES 版本
        setEGLContextClientVersion(2)

        // 设置渲染器
        setRenderer(EcgRenderer())
    }
}

class EcgRenderer : GLSurfaceView.Renderer {
    // 添加顶点着色器和片段着色器的代码。它们将顶点位置传递给渲染管线并使用固定颜色进行渲染
    // 顶点着色器的代码
    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}"

    // 片段着色器的代码
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"

    // 顶点数据。对应openGL的世界坐标系
    private val vertexData = floatArrayOf(
        0.0f, 0.5f, 0.0f,// 点1：x，y，z
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )

    // 顶点缓冲区对象
    private var vertexBufferId = 0

    // 着色器程序id
    private var shaderProgramId = 0

    /**
     * 添加编译和链接着色器的方法
     * @return  返回着色器程序的 ID
     */
    private fun loadShaderProgram(vertexCode: String, fragmentCode: String): Int {
        // 编译顶点着色器
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexCode)
        GLES20.glCompileShader(vertexShader)

        // 编译片段着色器
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentCode)
        GLES20.glCompileShader(fragmentShader)

        // 链接着色器程序
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("onSurfaceCreated")
        // 设置清除屏幕时使用的颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // 创建顶点缓冲区对象
        val buffers = IntArray(1)
        GLES20.glGenBuffers(1, buffers, 0)
        vertexBufferId = buffers[0]

        // 将顶点数据上传到缓冲区对象
        val vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertexData)
        vertexBuffer.position(0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexData.size * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        // 加载并创建着色器程序
        shaderProgramId = loadShaderProgram(vertexShaderCode, fragmentShaderCode)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("onSurfaceChanged")
        // 设置视口大小
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        println("onDrawFrame")
        // 清除屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 使用着色器程序
        GLES20.glUseProgram(shaderProgramId)

        // 绑定顶点缓冲区对象并启用顶点属性
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
        val positionLocation = GLES20.glGetAttribLocation(shaderProgramId, "vPosition")
        GLES20.glEnableVertexAttribArray(positionLocation)
        GLES20.glVertexAttribPointer(positionLocation, 3, GLES20.GL_FLOAT, false, 0, 0)

        // 设置片段着色器的颜色
        val colorLocation = GLES20.glGetUniformLocation(shaderProgramId, "vColor")
        GLES20.glUniform4f(colorLocation, 1.0f, 0.0f, 0.0f, 1.0f)

        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3) // GL_TRIANGLES:三角形 GL_POINTS:点

        // 禁用顶点属性并解除顶点缓冲区对象的绑定
        GLES20.glDisableVertexAttribArray(positionLocation)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}