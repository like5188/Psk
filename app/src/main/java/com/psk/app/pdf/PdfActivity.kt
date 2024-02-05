package com.psk.app.pdf

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psk.app.R
import com.psk.app.databinding.ActivityPdfBinding
import com.psk.ecg.painter.BgPainter
import com.psk.ecg.painter.StaticDataPainter
import com.psk.pdf.DefaultSplit
import com.psk.pdf.Pdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

class PdfActivity : AppCompatActivity() {
    private val mBinding: ActivityPdfBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_pdf)
    }
    private val pdf: Pdf by lazy {
        Pdf(
            split = DefaultSplit(
                listOf(mBinding.tv8, mBinding.rv.getChildAt(19)), listOf(mBinding.ll)
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btn.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val file = File(cacheDir, "${System.currentTimeMillis()}.pdf")
                val parentFile = file.parentFile
                if (parentFile != null && !parentFile.exists()) {
                    parentFile.mkdirs()
                }
                if (!file.exists()) {
                    file.createNewFile()
                }
                pdf.saveView(mBinding.sv, file)
            }
        }
        mBinding.rv.layoutManager = LinearLayoutManager(this)
        mBinding.rv.adapter = MyAdapter().apply {
            val list = (100..200).map {
                Item(it.toString())
            }
            submitList(list)
        }

        mBinding.btn1.setOnClickListener {
            mBinding.ecgChartView.setMmPerMv(Random.nextInt(5, 20))
        }
        mBinding.ecgChartView.apply {
            setSampleRate(250)
            setBgPainter(BgPainter(Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 2f
                isAntiAlias = true
                alpha = 120
            }, Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 1f
                isAntiAlias = true
                pathEffect = DashPathEffect(floatArrayOf(1f, 1f), 0f)
                alpha = 90
            }, Paint().apply {
                color = Color.parseColor("#000000")
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
                alpha = 125
            }, Paint().apply {
                textSize = 18f
                color = Color.RED
            }))
            setDataPainters((0 until 12).map {
                StaticDataPainter(Paint().apply {
                    color = Color.parseColor("#44C71E")
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                })
            })
            setLeadsNames(listOf("I", "II", "III", "aVR", "aVL", "aVF", "V1", "V2", "V3", "V4", "V5", "V6"))
            val list = createEcgData(500)
            setData((0 until 12).map { list })
        }
    }

    private fun createEcgData(count: Int): List<Float> {
        return (0 until count).map { kotlin.random.Random.nextFloat() - 0.5f }
    }

}
