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
        // RENDERMODE_WHEN_DIRTY 表示被动渲染，只有在调用requestRender或者onResume等方法时才会进行渲染。
        // RENDERMODE_CONTINUOUSLY 表示持续渲染。这是默认值
        renderMode = RENDERMODE_WHEN_DIRTY
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
    // 顶点着色器的代码，使用的是opengl的着色语言OpenGl Shader Language(GLSL)，详细语法参考https://juejin.cn/post/6874885969653596167
    // gl_Position 放置顶点坐标信息；gl_PointSize 绘制点的大小
    private val vertexShaderCode = "attribute vec4 a_position;" +
            "void main() {" +
            "  gl_Position = a_position;" +
            "  gl_PointSize = 10.0;" +
            "}"

    // 片段着色器的代码
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 u_color;" +
            "void main() {" +
            "  gl_FragColor = u_color;" +
            "}"

    // 在代码中这些顶点会用浮点数数组来表示，因为是二维坐标，所以每个顶点要用俩个浮点数来记录，一个标记x轴位置，一个标记y轴位置，这个数组通常被称为属性（attribute）数组
    // 这个数组表示俩个三角形，每个三角形都以逆时针表示，一共四个顶点，俩个三角形共用俩个顶点，这样就形成了一个矩形。
    // 定义好顶点了，但是我们的java代码是运行在虚拟机上，而opengl是运行在本地的硬件上的，那么如何才能把java数据可以让opengl使用呢？
    // ByteBuffer 可以分配本地的内存块，并且把java数据复制到本地内存
    // opengl会把屏幕映射到【-1，1】的范围内
    private val tableVertices = floatArrayOf(
        //第一个三角
        -0.5f, -0.5f,
        0.5f, 0.5f,
        -0.5f, 0.5f,
        //第二个三角
        -0.5f, -0.5f,
        0.5f, -0.5f,
        0.5f, 0.5f,
        //线
        -0.5f, 0f,
        0.5f, 0f,
        //点
        0f, -0.25f,
        0f, 0.25f
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

    private var u_color = 0
    private var a_position = 0

    /**
     * 链接着色器
     * @return  返回着色器程序的 ID
     */
    private fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
        //创建程序对象
        val programId = GLES20.glCreateProgram()
        if (programId == 0) {
            println("创建program失败")
            return 0
        }
        //依附着色器
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)
        //链接程序
        GLES20.glLinkProgram(programId)
        //检查链接状态
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        println("链接程序" + GLES20.glGetProgramInfoLog(programId))
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(programId)
            println("链接program失败")
            return 0
        }
        return programId
    }

    /**
     * 编译着色器
     * @param type      着色器类型。[GLES20.GL_VERTEX_SHADER]、[GLES20.GL_FRAGMENT_SHADER]
     * @param source    着色器代码
     */
    private fun compileShader(type: Int, source: String): Int {
        //创建shader
        val shaderId = GLES20.glCreateShader(type)
        if (shaderId == 0) {
            println("创建shader失败")
            return 0
        }
        //上传shader源码
        GLES20.glShaderSource(shaderId, source)
        //编译shader源代码
        GLES20.glCompileShader(shaderId)
        //取出编译结果
        val compileStatus = IntArray(1)
        //取出shaderId的编译状态并把他写入compileStatus的0索引
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        println("编译状态${GLES20.glGetShaderInfoLog(shaderId)}")
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderId)
            println("创建shader失败")
            return 0
        }
        return shaderId
    }

    /**
     * 在使用opengl之前我们应该验证一下，看当前程序对opengl是否有效
     */
    fun validateProgram(program: Int): Boolean {
        GLES20.glValidateProgram(program)
        val validateStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_VALIDATE_STATUS, validateStatus, 0)
        println("当前opengl情况" + validateStatus[0] + "/" + GLES20.glGetProgramInfoLog(program))
        return validateStatus[0] != 0
    }

    // 当surface被创建时，GlsurfaceView会调用这个方法，这个发生在应用程序
    // 第一次运行的时候或者从其他Activity回来的时候也会调用
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        println("onSurfaceCreated")
        // 设置清除屏幕时使用的颜色
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        // 编译顶点着色器
        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        // 编译片段着色器
        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        // 链接着色器程序
        val program = linkProgram(vertexShader, fragmentShader)
        // 验证程序
        validateProgram(program)
        // 使用程序
        GLES20.glUseProgram(program)
        //获取shader属性
        u_color = GLES20.glGetUniformLocation(program, "u_color")// 获取指定uniform的位置，并保存在返回值u_color变量中，方便之后使用
        a_position = GLES20.glGetAttribLocation(program, "a_position")
        //绑定a_position和verticeData顶点位置
        /**
         * 第一个参数，这个就是shader属性
         * 第二个参数，每个顶点有多少分量，我们这个只有2个分量
         * 第三个参数，数据类型
         * 第四个参数，只有整形才有意义，忽略
         * 第5个参数，一个数组有多个属性才有意义，我们只有一个属性，传0
         * 第六个参数，opengl从哪里读取数据
         */
        GLES20.glVertexAttribPointer(a_position, 2, GLES20.GL_FLOAT, false, 0, verticesData)
        //开启顶点
        GLES20.glEnableVertexAttribArray(a_position)
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

        //绘制长方形
        //指定着色器u_color的颜色为白色
        GLES20.glUniform4f(u_color, 1.0f, 1.0f, 1.0f, 1.0f);
        /**
         * 绘制三角形
         * 第一个参数：你想画什么，有三种模式GLES20.GL_TRIANGLES三角形，GLES20.GL_LINES线，GLES20.GL_POINTS点，
         * 第二个参数：从数组那个位置开始读，
         * 第三个参数：一共读取几个顶点
         *
         * 最终绘制俩个三角形，组成矩形
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);

        //绘制分割线
        GLES20.glUniform4f(u_color, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        //绘制点
        GLES20.glUniform4f(u_color, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glUniform4f(u_color, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);
    }
}