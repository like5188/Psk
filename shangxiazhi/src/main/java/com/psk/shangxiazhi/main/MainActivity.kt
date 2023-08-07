package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.util.startApp
import com.twsz.twsystempre.GameController
import com.twsz.twsystempre.GameData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 主界面
 */
class MainActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<MainActivity>()
        }
    }

    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val gameController by lazy {
        GameController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.ivAutonomyTraining.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                // 启动游戏
                startApp("com.twsz.twsystempre", "com.twsz.twsystempre.activity.GameActivity")
                delay(3000)
                gameController.initGame("country", true)
                delay(3000)
                launch {
                    gameController.updateGameConnectionState(true)
                    while (isActive) {
                        delay(1000)
                        val gameData = GameData(
                            model = 0,
                            speed = (60..100).random(),
                            speedLevel = 5,
                            time = "20230731",
                            mileage = "11",
                            cal = DecimalFormat("######0.00").format(10.0),
                            resistance = 10,
                            offset = -1,
                            offsetValue = 20,
                            spasm = 10,
                            spasmLevel = 2,
                            spasmFlag = 110,
                            pause = 0,
                            over = 0,
                        )
                        gameController.updateGame(gameData)
                    }
                }
                launch {
                    gameController.updateEcgConnectionState(true)
                    while (isActive) {
                        delay(1000L / 125)
                        gameController.updateEcg(
                            BigDecimal.valueOf((-128..127).random().toDouble()).setScale(5, BigDecimal.ROUND_HALF_DOWN).toFloat() / 150f,
                            (60..100).random()
                        )
                    }
                }
            }
        }
        mBinding.ivMedicalOrderTraining.setOnClickListener {
            showToast("医嘱训练")
        }
        mBinding.ivTrainingRecords.setOnClickListener {
            showToast("训练记录")
        }
    }

    override fun onResume() {
        super.onResume()
        // 当从游戏界面返回时，需要解绑游戏服务
        gameController.destroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameController.destroy()
    }
}
