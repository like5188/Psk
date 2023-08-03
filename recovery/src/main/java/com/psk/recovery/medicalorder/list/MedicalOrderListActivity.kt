package com.psk.recovery.medicalorder.list

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.like.common.util.AutoWired
import com.like.common.util.fitStatusBar
import com.like.common.util.injectForIntentExtras
import com.like.common.util.setTransparentStatusBar
import com.like.common.util.sp
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.customview.ViewPagerAdapter
import com.psk.recovery.R
import com.psk.recovery.databinding.ActivityMedicalOrderListBinding

/**
 * 医嘱列表界面
 */
class MedicalOrderListActivity : AppCompatActivity() {
    @AutoWired
    val status: Int = 3

    companion object {
        fun start(status: Int) {
            if (status < 0 || status > 3) {
                throw IllegalArgumentException("status is invalid")
            }
            CommonApplication.sInstance.startActivity<MedicalOrderListActivity>(
                "status" to status
            )
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.setContentView<ActivityMedicalOrderListBinding>(this, R.layout.activity_medical_order_list)
    }
    private val mFragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectForIntentExtras()
        setTransparentStatusBar(true)
        mBinding.ll.fitStatusBar()
        mFragments.add(MedicalOrderListFragment.newInstance(0))
        mFragments.add(MedicalOrderListFragment.newInstance(1))
        mFragments.add(MedicalOrderListFragment.newInstance(2))
        mFragments.add(MedicalOrderListFragment.newInstance(3))
        initView()
    }

    private fun initView() {
        initTabLayout(mBinding.tabLayout)
        initViewPager(mBinding.viewPager)
        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> {
                    "未开始"
                }

                1 -> {
                    "进行中"
                }

                2 -> {
                    "已完成"
                }

                else -> {
                    "全部医嘱"
                }
            }
            // 隐藏长按显示文本
            // 取消长按事件
            tab.view.isLongClickable = false
            // api 26 以上 设置空text
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                tab.view.tooltipText = ""
            }
        }.attach()
    }

    private fun initTabLayout(tabLayout: TabLayout) {
        tabLayout.setTabTextColors(
            resources.getColor(R.color.common_text_black_0, null),
            resources.getColor(R.color.common_text_red_0, null)
        )
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // tab 文字样式设置
                val text = tab?.text?.toString()?.trim() ?: return
                SpannableString(text).apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(20.sp), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    tab.text = this
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // tab 文字样式设置
                val text = tab?.text?.toString()?.trim() ?: return
                SpannableString(text).apply {
                    setSpan(StyleSpan(Typeface.NORMAL), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(16.sp), 0, text.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    tab.text = this
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun initViewPager(viewPager: ViewPager2) {
        viewPager.adapter = ViewPagerAdapter(mFragments, this)
        viewPager.offscreenPageLimit = 3
        viewPager.setCurrentItem(status, false)
    }

}
