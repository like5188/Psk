package com.psk.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.app.databinding.ActivityMainBinding
import com.psk.app.pdf.PdfActivity
import com.psk.app.sixminutes.SixMinutesActivity

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnSixMinutes.setOnClickListener {
            startActivity(Intent(this, SixMinutesActivity::class.java))
        }
        mBinding.btnPdf.setOnClickListener {
            startActivity(Intent(this, PdfActivity::class.java))
        }
    }

}