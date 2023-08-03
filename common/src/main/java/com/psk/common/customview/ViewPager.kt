package com.psk.common.customview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.like.common.base.BaseLazyFragment
import com.like.paging.PagingResult
import com.like.recyclerview.adapter.CombineAdapter
import com.like.recyclerview.layoutmanager.WrapLinearLayoutManager
import com.psk.common.R
import com.psk.common.databinding.CommonFragmentRecyclerviewBinding
import com.psk.common.databinding.CommonFragmentSwiperefreshlayoutRecyclerviewBinding

/**
 * 包含 [SwipeRefreshLayout] 和 [RecyclerView] 的 [Fragment]。
 */
abstract class SwipeRefreshLayoutRecyclerViewFragment<T> : BaseLazyFragment() {
    private lateinit var mBinding: CommonFragmentSwiperefreshlayoutRecyclerviewBinding
    private val mCombineAdapter by lazy {
        createCombineAdapter(mBinding.rv)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.common_fragment_swiperefreshlayout_recyclerview, container, false)
        mBinding.swipeRefreshLayout.setOnRefreshListener {
            refresh()
        }
        mBinding.rv.layoutManager = WrapLinearLayoutManager(requireContext())
        mBinding.rv.adapter = mCombineAdapter.concatAdapter
        return mBinding.root
    }

    override fun onLazyLoadData() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mCombineAdapter.collectFrom(getPagingResult())
        }
    }

    fun refresh() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mCombineAdapter.refresh()
        }
    }

    abstract fun createCombineAdapter(recyclerView: RecyclerView): CombineAdapter<T>
    abstract fun getPagingResult(): PagingResult<List<T>?>

}

/**
 * 包含 [RecyclerView] 的 [Fragment]。
 */
abstract class RecyclerViewFragment<T> : BaseLazyFragment() {
    private lateinit var mBinding: CommonFragmentRecyclerviewBinding
    private val mCombineAdapter by lazy {
        createCombineAdapter(mBinding.rv)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.common_fragment_recyclerview, container, false)
        mBinding.rv.layoutManager = WrapLinearLayoutManager(requireContext())
        mBinding.rv.adapter = mCombineAdapter.concatAdapter
        return mBinding.root
    }

    override fun onLazyLoadData() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mCombineAdapter.collectFrom(getPagingResult())
        }
    }

    fun refresh() {
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            mCombineAdapter.refresh()
        }
    }

    abstract fun createCombineAdapter(recyclerView: RecyclerView): CombineAdapter<T>
    abstract fun getPagingResult(): PagingResult<List<T>?>

}

class ViewPagerAdapter(private val fragments: List<Fragment>, fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}