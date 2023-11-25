package com.psk.app

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * Android中GLES20 API的一些主要功能说明：
Shaders（着色器）：GLES20使用可编程的着色器来渲染图形。顶点着色器处理顶点数据，片段着色器处理像素数据。着色器需要用GLSL（OpenGL Shading Language）编写。
Buffers（缓冲区）：GLES20使用缓冲区来存储顶点数据和索引数据。顶点缓冲区对象（VBO）存储顶点数据，元素缓冲区对象（EBO）存储索引数据。
Textures（纹理）：GLES20支持多种纹理类型，如2D纹理、立方体贴图等。纹理用于给3D对象添加详细的表面特征
Framebuffers（帧缓冲区）：GLES20使用帧缓冲区对象（FBO）来存储渲染结果。你可以将渲染结果渲染到纹理中，然后将纹理应用到其他对象上，实现高级渲染效果。
Transformations（变换）：GLES20支持多种变换操作，如平移、旋转、缩放等。变换矩阵用于在顶点着色器中处理顶点数据。
Lighting（光照）：GLES20支持基本的光照计算，如环境光、漫反射光、镜面反射光等。光照计算通常在顶点着色器或片段着色器中进行。
Blending（混合）：GLES20支持颜色混合，用于实现透明度、半透明等效果。混合操作可以根据源颜色和目标颜色按照指定的混合因子进行计算。
Culling（剔除）：GLES20支持面剔除，可以剔除不可见的面，提高渲染性能。面剔除可以根据面的正面或反面进行。
Depth Testing（深度测试）：GLES20支持深度测试，用于判断像素的可见性。深度测试可以根据像素的深度值进行比较，只渲染最前面的像素。
Stencil Testing（模板测试）：GLES20支持模板测试，用于实现遮罩、镜子等效果。模板测试可以根据模板缓冲区的值对像素进行掩盖或者保留。
 *
 * GLES20 API中一些主要方法的说明：
glGenBuffers：生成缓冲区对象，如顶点缓冲区对象（VBO）和元素缓冲区对象（EBO）。
glBindBuffer：绑定缓冲区对象，使其成为当前活动的缓冲区。
glBufferData：将数据上传到缓冲区对象。
glVertexAttribPointer：定义顶点属性数组，指定顶点数据在缓冲区中的布局。
glEnableVertexAttribArray：启用顶点属性数组。
glDisableVertexAttribArray：禁用顶点属性数组。
glUseProgram：使用某个着色器程序进行渲染。
glGetUniformLocation：获取着色器程序中uniform变量的位置。
glUniformMatrix4fv：为uniform变量设置矩阵数据。
glCreateShader：创建着色器对象。
glShaderSource：为着色器对象设置源代码。
glCompileShader：编译着色器对象。
glGetShaderiv：获取着色器对象的编译状态。
glAttachShader：将着色器对象附加到着色器程序。
glLinkProgram：链接着色器程序。
glGetProgramiv：获取着色器程序的链接状态。
glDeleteShader：删除着色器对象。
glGenTextures：生成纹理对象。
glBindTexture：绑定纹理对象。
glTexParameteri：设置纹理参数，如过滤模式、环绕模式等。
glTexImage2D：上传纹理数据。
glGenFramebuffers：生成帧缓冲区对象。
glBindFramebuffer：绑定帧缓冲区对象。
glFramebufferTexture2D：将纹理对象附加到帧缓冲区对象。
glDrawArrays：绘制顶点数组，渲染图形。
glDrawElements：绘制索引数组，渲染图形。
glEnable：启用某个OpenGL功能，如深度测试、剔除、混合等。
glDisable：禁用某个OpenGL功能。
glBlendFunc：设置混合函数。
glCullFace：设置剔除面的模式。
glClearColor：设置清除颜色缓冲区时使用的颜色。
glClear：清除颜色缓冲区、深度缓冲区和/或模板缓冲区。
 */
class GlEcgChartView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    init {
        if (!supportsEs2()) {
            throw UnsupportedOperationException("not support opengl es2.0")
        }
        // 设置 OpenGL ES 版本
        setEGLContextClientVersion(2)
        // 设置渲染器
        setRenderer(EcgRenderer())
        // 设置渲染方式
        // RENDERMODE_WHEN_DIRTY表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。
        // RENDERMODE_CONTINUOUSLY表示持续渲染。这是默认值
        renderMode = RENDERMODE_CONTINUOUSLY
        // 打开调试和日志
        debugFlags = DEBUG_CHECK_GL_ERROR or DEBUG_LOG_GL_CALLS
    }

    /**
     * 判断设备是否支持2.0版本
     */
    private fun supportsEs2(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val configurationInfo = activityManager?.deviceConfigurationInfo ?: return false
        return (configurationInfo.reqGlEsVersion >= 0x20000 ||
                Build.FINGERPRINT.startsWith("generic") ||
                Build.FINGERPRINT.startsWith("unknown") ||
                Build.MODEL.contains("google_sdk") ||
                Build.MODEL.contains("Emulator") ||
                Build.MODEL.contains("Android SDK built for x86"))
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

    // 在代码中这些顶点会用浮点数数组来表示，因为是二维坐标，所以每个顶点要用俩个浮点数来记录，一个标记x轴位置，一个标记y轴位置，这个数组通常被称为属性（attribute）数组
    // 这个数组表示俩个三角形，每个三角形都以逆时针表示，一共四个顶点，俩个三角形共用俩个顶点，这样就形成了一个矩形。
    // 定义好顶点了，但是我们的java代码是运行在虚拟机上，而opengl是运行在本地的硬件上的，那么如何才能把java数据可以让opengl使用呢？
    // ByteBuffer 可以分配本地的内存块，并且把java数据复制到本地内存
    private val tableVertices = floatArrayOf(
        //第一个三角形
        0f, 0f,
        9f, 14f,
        0f, 14f,
        //第二个三角形
        0f, 0f,
        9f, 0f,
        9f, 14f,
        //中间的直线
        0f, 7f,
        9f, 7f,
        //点
        4.5f, 2f,
        4.5f, 12f
    )

    // allocateDirect 分配一块本地内存，分配大小由外部传入
    // 每个浮点数有32位精度，而每个byte有8位精度，所以每个浮点数都占4个字节
    private val verticesData: FloatBuffer = ByteBuffer.allocateDirect(tableVertices.size * 4)
        .order(ByteOrder.nativeOrder())// 告诉缓冲区，按照本地字节序组织内容
        .asFloatBuffer()
        .put(tableVertices).apply {
            position(0)
        }
    /*
    现在opengl已经拥有了数据，在把矩形画到屏幕之前，他们还需要在opengl的管道（pipeline）中传递，这一步就需要使用着色器（shader），
    这些着色器会告诉图形处理单元（GPU）如何绘制数据，有俩种着色器我们需要定义：
    顶点着色器（vertex shader）：生成每个顶点的最终位置，针对每个顶点他都会执行一次，一旦位置确定，opengl就可以把这些顶点组装成点，线和三角形
    片段着色器（fragment shader）：为组成点，线，三角形的每个片段生成最终的颜色，针对每个片段他都会执行一次，一个片段是一个小的，单一颜色的长方形区域，类似计算机屏幕上的一个像素
    一旦最终的颜色生成后，opengl会把他们写到一块称为帧缓冲区（frame buffer）的内存块中，然后Android会把这个帧缓冲区显示到屏幕上
     */

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

    // 当surface被创建时，GlsurfaceView会调用这个方法，这个发生在应用程序
    // 第一次运行的时候或者从其他Activity回来的时候也会调用
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

    //在Surface创建以后，每次surface尺寸大小发生变化，这个方法会被调用到，比如横竖屏切换
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        println("onSurfaceChanged")
        // 设置视口大小，告诉opengl需要渲染的surface尺寸大小
        GLES20.glViewport(0, 0, width, height)
    }

    // 当绘制每一帧数据的时候，会调用这个放方法，这个方法一定要绘制一些东西，即使只是清空屏幕，
    // 因为这个方法返回后，渲染区的数据会被交换并显示在屏幕上，如果什么都没有话，会看到闪烁效果。
    // 在OpenGl中只能绘制点，直线，三角形。
    override fun onDrawFrame(gl: GL10?) {
        println("onDrawFrame")
        // 清空屏幕，并用之前glClearColor定义的颜色填充
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