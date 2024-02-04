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

class PdfActivity : AppCompatActivity() {
    private val mBinding: ActivityPdfBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_pdf)
    }
    private val pdfHelper by lazy {
        PdfHelper(
            Pdf(
                split = DefaultSplit(
                    listOf(mBinding.tv8, mBinding.rv.getChildAt(19)),
                    listOf(mBinding.ll)
                )
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
                pdfHelper.save(mBinding.sv, file)
            }
        }
        mBinding.rv.layoutManager = LinearLayoutManager(this)
        mBinding.rv.adapter = MyAdapter().apply {
            val list = (100..200).map {
                Item(it.toString())
            }
            submitList(list)
        }

        var mm_per_mv = 50
        mBinding.btn1.setOnClickListener {
            mm_per_mv -= 10
            if (mm_per_mv <= 0) {
                mm_per_mv = 10
            }
            mBinding.ecgChartView.setMmPerMv(mm_per_mv)
        }
        mBinding.ecgChartView.apply {
            setSampleRate(250)
            setMmPerMv(mm_per_mv)
            setBgPainter(BgPainter(Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 2f
                isAntiAlias = true
                alpha = 120
            }, Paint().apply {
                color = Color.parseColor("#00a7ff")
                strokeWidth = 1f
                isAntiAlias = true
                pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
                alpha = 90
            }, Paint().apply {
                color = Color.parseColor("#000000")
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
                alpha = 125
            }))
            setDataPainters((0 until 1).map {
                StaticDataPainter(Paint().apply {
                    color = Color.parseColor("#44C71E")
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                })
            })
        }
        mBinding.ecgChartView.setData(listOf(createEcgData(300)))
    }

    private fun createEcgData(count: Int): List<Float> {
        return (0 until count).map { kotlin.random.Random.nextFloat() - 0.5f }
    }

}
