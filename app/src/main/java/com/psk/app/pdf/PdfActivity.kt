package com.psk.app.pdf

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.psk.app.R
import com.psk.app.databinding.ActivityPdfBinding
import com.psk.ecg.replaceEcgChartView
import com.psk.pdf.DefaultSplit
import com.psk.pdf.Pdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class PdfActivity : AppCompatActivity() {
    private val mBinding: ActivityPdfBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_pdf)
    }
    private val pdf by lazy {
        Pdf(
            split = DefaultSplit(
                listOf(mBinding.tv8, mBinding.rv.getChildAt(19)),
                listOf(mBinding.ll)
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
                pdf.saveView(mBinding.sv.replaceEcgChartView(), file)
            }
        }
        mBinding.rv.layoutManager = LinearLayoutManager(this)
        mBinding.rv.adapter = MyAdapter().apply {
            val list = (100..200).map {
                Item(it.toString())
            }
            submitList(list)
        }

        mBinding.ecgChartView.apply {
            init(100)
            lifecycleScope.launch(Dispatchers.IO) {
                repeat(5) {
                    delay(1000)
                    addData(listOf(getEcgData(100)))
                }
            }
        }

    }

    private fun getEcgData(count: Int): List<Float> {
        return (0..count).map { kotlin.random.Random.nextFloat() - 0.5f }
    }

}
