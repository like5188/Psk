package com.psk.common.util

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import coil.load
import com.like.common.util.CoilImageLoaderFactory
import com.like.common.util.UiStatus
import com.like.common.util.gone
import com.like.paging.PagingResult
import com.like.paging.RequestType
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.adapter.BaseLoadStateAdapter
import com.like.recyclerview.adapter.CombineAdapter
import com.like.recyclerview.ui.adapter.BaseUiStatusController
import com.like.recyclerview.ui.adapter.UiStatusCombineAdapter
import com.like.recyclerview.ui.loadstate.LoadStateAdapter
import com.like.recyclerview.ui.loadstate.LoadStateItem
import com.psk.common.R
import com.psk.common.databinding.CommonViewUiStatusBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 对数据请求操作做了封装。
 * @author like
 * Date: 2021-03-02
 */
object DataHandler {

    suspend fun <ResultType> collect(
        context: Context,
        block: suspend () -> ResultType?,
        onError: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (ResultType?) -> Unit)? = null
    ) {
        performCollect(
            context = context,
            block = block,
            show = null,
            hide = null,
            onError = onError,
            onSuccess = onSuccess
        )
    }

    suspend fun <ResultType> collectWithProgress(
        context: Context,
        block: suspend () -> ResultType?,
        show: () -> Unit,
        hide: () -> Unit,
        onError: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (ResultType?) -> Unit)? = null
    ) {
        performCollect(
            context = context,
            block = block,
            show = show,
            hide = hide,
            onError = onError,
            onSuccess = onSuccess
        )
    }

    suspend fun <ResultType> collectWithProgress(
        dialog: Dialog,
        block: suspend () -> ResultType?,
        onError: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (ResultType?) -> Unit)? = null
    ) {
        performCollect(
            context = dialog.context,
            block = block,
            show = { dialog.show() },
            hide = { dialog.dismiss() },
            onError = onError,
            onSuccess = onSuccess
        )
    }

    suspend fun <ResultType> collectWithProgress(
        swipeRefreshLayout: SwipeRefreshLayout,
        block: suspend () -> ResultType?,
        onError: (suspend (Throwable) -> Unit)? = null,
        onSuccess: (suspend (ResultType?) -> Unit)? = null
    ) {
        performCollect(
            context = swipeRefreshLayout.context,
            block = block,
            show = { swipeRefreshLayout.isRefreshing = true },
            hide = { swipeRefreshLayout.isRefreshing = false },
            onError = onError,
            onSuccess = onSuccess
        )
    }

    suspend fun <ResultType> collect(
        context: Context,
        block: suspend () -> ResultType?,
    ): ResultType? = performCollect(
        context = context,
        block = block,
        show = null,
        hide = null,
    )

    suspend fun <ResultType> collectWithProgress(
        context: Context,
        block: suspend () -> ResultType?,
        show: () -> Unit,
        hide: () -> Unit,
    ): ResultType? = performCollect(
        context = context,
        block = block,
        show = show,
        hide = hide,
    )

    suspend fun <ResultType> collectWithProgress(
        dialog: Dialog,
        block: suspend () -> ResultType?,
    ): ResultType? = performCollect(
        context = dialog.context,
        block = block,
        show = { dialog.show() },
        hide = { dialog.dismiss() },
    )

    suspend fun <ResultType> collectWithProgress(
        swipeRefreshLayout: SwipeRefreshLayout,
        block: suspend () -> ResultType?,
    ): ResultType? = performCollect(
        context = swipeRefreshLayout.context,
        block = block,
        show = { swipeRefreshLayout.isRefreshing = true },
        hide = { swipeRefreshLayout.isRefreshing = false },
    )

    fun <ValueInList> createAdapter(
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = null,
        getItemsFrom = null,
        uiStatusController = uiStatusController,
        show = null,
        hide = null,
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createAdapterWithProgress(
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        show: () -> Unit,
        hide: () -> Unit,
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = null,
        getItemsFrom = null,
        uiStatusController = uiStatusController,
        show = show,
        hide = hide,
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createAdapterWithProgress(
        swipeRefreshLayout: SwipeRefreshLayout,
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = null,
        getItemsFrom = null,
        uiStatusController = uiStatusController,
        show = { swipeRefreshLayout.isRefreshing = true },
        hide = { swipeRefreshLayout.isRefreshing = false },
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createAdapterWithProgress(
        dialog: Dialog,
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = null,
        getItemsFrom = null,
        uiStatusController = uiStatusController,
        show = { dialog.show() },
        hide = { dialog.dismiss() },
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createPagingAdapter(
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        getItemsFrom: ((List<ValueInList>?) -> List<ValueInList>?)? = { it },
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = LoadStateAdapter(LoadStateItem()),
        getItemsFrom = getItemsFrom,
        uiStatusController = uiStatusController,
        show = null,
        hide = null,
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createPagingAdapterWithProgress(
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        getItemsFrom: ((List<ValueInList>?) -> List<ValueInList>?)? = { it },
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        show: () -> Unit,
        hide: () -> Unit,
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = LoadStateAdapter(LoadStateItem()),
        getItemsFrom = getItemsFrom,
        uiStatusController = uiStatusController,
        show = show,
        hide = hide,
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createPagingAdapterWithProgress(
        swipeRefreshLayout: SwipeRefreshLayout,
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        getItemsFrom: ((List<ValueInList>?) -> List<ValueInList>?)? = { it },
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = LoadStateAdapter(LoadStateItem()),
        getItemsFrom = getItemsFrom,
        uiStatusController = uiStatusController,
        show = { swipeRefreshLayout.isRefreshing = true },
        hide = { swipeRefreshLayout.isRefreshing = false },
        onError = onError,
        onSuccess = onSuccess
    )

    fun <ValueInList> createPagingAdapterWithProgress(
        dialog: Dialog,
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()
        ),
        itemAdapter: BaseListAdapter<*, ValueInList>,
        getItemsFrom: ((List<ValueInList>?) -> List<ValueInList>?)? = { it },
        uiStatusController: DefaultUiStatusController? = DefaultUiStatusController(recyclerView),
        onError: (suspend (RequestType, Throwable) -> Unit)? = null,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)? = null
    ): CombineAdapter<ValueInList> = performCreateAdapter(
        recyclerView = recyclerView,
        concatAdapter = concatAdapter,
        itemAdapter = itemAdapter,
        loadStateAdapter = LoadStateAdapter(LoadStateItem()),
        getItemsFrom = getItemsFrom,
        uiStatusController = uiStatusController,
        show = { dialog.show() },
        hide = { dialog.dismiss() },
        onError = onError,
        onSuccess = onSuccess
    )

    /**
     * 执行代码块[block]。
     */
    private suspend fun <ResultType> performCollect(
        context: Context,
        block: suspend () -> ResultType?,
        show: (() -> Unit)?,
        hide: (() -> Unit)?,
    ): ResultType? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            launch {
                performCollect(context, block, show, hide, {
                    continuation.resumeWithException(it)
                }) {
                    continuation.resume(it)
                }
            }
        }
    }

    /**
     * 执行代码块[block]。
     *
     * @param block             获取数据的函数
     * @param show              初始化或者刷新开始时显示进度条
     * @param hide              初始化或者刷新成功或者失败时隐藏进度条
     * @param onError           失败回调，会先再需要重试时进行重试，然后通过[ExceptionHandler.handle]进行统一错误处理，如果需要做其它错误处理，可以从这里获取。
     * @param onSuccess         成功回调，如果需要结果，可以从这里获取。
     */
    private suspend fun <ResultType> performCollect(
        context: Context,
        block: suspend () -> ResultType?,
        show: (() -> Unit)?,
        hide: (() -> Unit)?,
        onError: (suspend (Throwable) -> Unit)?,
        onSuccess: (suspend (ResultType?) -> Unit)?
    ) {
        block.asFlow()
            .flowOn(Dispatchers.IO)
            .onStart {
                show?.invoke()
            }
            .onCompletion {
                hide?.invoke()
            }
            .catch { throwable ->
                ExceptionHandler.handle(context.applicationContext, throwable)
                onError?.invoke(throwable)
            }
            .flowOn(Dispatchers.Main)
            .collect {
                onSuccess?.invoke(it)
            }
    }

    /**
     * 为[RecyclerView]创建的 adapter。配合[com.like.paging]库使用
     *
     * @param loadStateAdapter  如果不需要分页，可以不传。
     * @param onError           失败回调，会先再需要重试时进行重试，然后通过[ExceptionHandler.handle]进行统一错误处理，如果需要做其它错误处理，可以从这里获取。
     */
    private fun <ValueInList> performCreateAdapter(
        recyclerView: RecyclerView,
        concatAdapter: ConcatAdapter,
        itemAdapter: BaseListAdapter<*, ValueInList>,
        loadStateAdapter: BaseLoadStateAdapter<*>?,
        getItemsFrom: ((List<ValueInList>?) -> List<ValueInList>?)?,
        uiStatusController: DefaultUiStatusController?,
        show: (() -> Unit)?,
        hide: (() -> Unit)?,
        onError: (suspend (RequestType, Throwable) -> Unit)?,
        onSuccess: (suspend (RequestType, List<ValueInList>?) -> Unit)?
    ): CombineAdapter<ValueInList> = object : UiStatusCombineAdapter<ValueInList>(uiStatusController, concatAdapter) {
        override fun getItemsFrom(list: List<ValueInList>?): List<ValueInList>? {
            return getItemsFrom?.invoke(list)
        }

        override suspend fun collectFrom(pagingResult: PagingResult<List<ValueInList>?>) {
            super.collectFrom(pagingResult.apply {
                flow = flow.flowOn(Dispatchers.IO)
            })
        }
    }.apply {
        this.attachedToRecyclerView(recyclerView)
        this.show = show
        this.hide = hide
        this.onError = { requestType, throwable ->
            ExceptionHandler.handle(recyclerView.context.applicationContext, throwable)
            onError?.invoke(requestType, throwable)
        }
        this.onSuccess = onSuccess
        this.withListAdapter(itemAdapter)
        loadStateAdapter?.let {
            this.withLoadStateFooter(it)
        }
    }

    /**
     * 此类需要配合 [UiStatusCombineAdapter] 使用，否则需要自己参照 [UiStatusCombineAdapter] 做处理。
     */
    open class DefaultUiStatusController(view: View) : BaseUiStatusController(view) {

        override fun addUiStatus(context: Context, refresh: suspend () -> Unit) {
            addUiStatus(TAG_UI_STATUS_EMPTY, UiStatus<CommonViewUiStatusBinding>(context, R.layout.common_view_ui_status).apply {
                dataBinding.iv.setImageResource(R.drawable.common_icon_empty)
                dataBinding.tvDes.text = "暂无数据~"
                dataBinding.tvFun.gone()
                dataBinding.tvTitle.gone()
            })
            addUiStatus(TAG_UI_STATUS_LOADING, UiStatus<CommonViewUiStatusBinding>(context, R.layout.common_view_ui_status).apply {
                dataBinding.iv.load(R.drawable.common_gif_loading, CoilImageLoaderFactory.createGifImageLoader(context))
                dataBinding.tvDes.text = "正在奋力加载中..."
                dataBinding.tvFun.gone()
                dataBinding.tvTitle.gone()
            })
            addUiStatus(TAG_UI_STATUS_ERROR, UiStatus<CommonViewUiStatusBinding>(context, R.layout.common_view_ui_status).apply {
                dataBinding.iv.setImageResource(R.drawable.common_icon_error)
                dataBinding.tvDes.text = "加载失败"
                dataBinding.tvFun.text = "刷新试试"
                dataBinding.tvFun.setOnClickListener {
                    if (context is LifecycleOwner) {
                        (context as LifecycleOwner).lifecycleScope.launch {
                            refresh()
                        }
                    }
                }
                dataBinding.tvTitle.gone()
            })
        }

        override fun getEmptyStatusTag(): String {
            return TAG_UI_STATUS_EMPTY
        }

        override fun getLoadingStatusTag(): String {
            return TAG_UI_STATUS_LOADING
        }

        override fun getErrorStatusTag(throwable: Throwable): String {
            return TAG_UI_STATUS_ERROR
        }
    }

}

// 用于 UiStatusController 的 key。用来进行界面状态管理。
/**
 * 空状态
 */
const val TAG_UI_STATUS_EMPTY = "tag_ui_status_empty"

/**
 * 错误状态
 */
const val TAG_UI_STATUS_ERROR = "tag_ui_status_error"

/**
 * 加载状态
 */
const val TAG_UI_STATUS_LOADING = "tag_ui_status_loading"
