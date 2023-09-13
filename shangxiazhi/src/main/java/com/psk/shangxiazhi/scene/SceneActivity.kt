package com.psk.shangxiazhi.scene

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.activityresultlauncher.startActivityForResult
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivitySceneBinding
import com.twsz.twsystempre.TrainScene

/**
 * 选择场景界面
 */
class SceneActivity : AppCompatActivity() {
    companion object {
        const val KEY_SCENE = "key_scene"
        fun start(
            activity: ComponentActivity,
            callback: ActivityResultCallback<ActivityResult>
        ) {
            activity.startActivityForResult<SceneActivity>(
                callback = callback
            )
        }
    }

    private val mBinding: ActivitySceneBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_scene)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.iv0.setOnClickListener {
            setResultAndFinish(TrainScene.country)
        }
        mBinding.iv1.setOnClickListener {
            setResultAndFinish(TrainScene.dust)
        }
        mBinding.iv2.setOnClickListener {
            setResultAndFinish(TrainScene.lasa)
        }
        mBinding.iv3.setOnClickListener {
            setResultAndFinish(TrainScene.sea)
        }
    }

    private fun setResultAndFinish(scene: TrainScene) {
        val intent = Intent()
        intent.putExtra(KEY_SCENE, scene)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
