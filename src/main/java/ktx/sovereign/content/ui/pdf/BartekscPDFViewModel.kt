package ktx.sovereign.content.ui.pdf

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import ktx.sovereign.core.util.LogMAR
import kotlin.math.max
import kotlin.math.min

private const val UNSET_INTEGER = -1
private const val MIN_ZOOM = 1.0f
private const val MAX_ZOOM = 10.0f
class BartekscPDFViewModel private constructor() : ViewModel(), OnPageChangeListener,
    OnLoadCompleteListener, OnPageErrorListener {
    val page: Int
        get() = _page + 1
    private var _page: Int = UNSET_INTEGER
    val count: Int
        get() = _count
    private var _count: Int = UNSET_INTEGER
    var uri: Uri? = null
        private set
    val isNightModeEnabled: Boolean
        get() = _nightModeEnabled
    private var _nightModeEnabled: Boolean = false

    val title: LiveData<String>
        get() = _title
    private val _title = MutableLiveData<String>().also { it.value = "PDF Reader" }

    private val logmar = LogMAR(1.0f, supremum = 1.0f)
    private var zoom: Float = 1.0f

    fun open(context: Context, uri: Uri, configurator: PDFView.Configurator): PDFView.Configurator {
        this@BartekscPDFViewModel.uri = uri
        _page = 0
        _title.postValue(context.getDisplayName(uri))
        return configurator.defaultPage(_page)
            .onPageChange(this@BartekscPDFViewModel)
            .enableAnnotationRendering(true)
            .onLoad(this@BartekscPDFViewModel)
            .scrollHandle(DefaultScrollHandle(context))
            .likeViewPager()
            .onPageError(this@BartekscPDFViewModel)
            .pageFitPolicy(FitPolicy.WIDTH)
    }
    fun zoomIn(): Float = min(logmar.stepUp(), MAX_ZOOM)
    fun zoomOut(): Float = max(logmar.stepDown(), MIN_ZOOM)
    fun setNightMode(enabled: Boolean): Boolean {
        _nightModeEnabled = enabled
        return _nightModeEnabled
    }

    override fun onPageChanged(current: Int, count: Int) {
        _page = current
    }
    override fun loadComplete(count: Int) {
        _count = count
    }
    override fun onPageError(page: Int, t: Throwable?) {
        Log.e("PDFView", "Failed to load page $page", t)
    }

    private fun PDFView.Configurator.likeViewPager(): PDFView.Configurator {
        return swipeHorizontal(true)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
    }
    private fun Context.getDisplayName(uri: Uri): String {
        var display: String = Build.UNKNOWN
        contentResolver.query(uri, null, null, null, null)?.apply {
            moveToFirst()
            display = getString(getColumnIndex(OpenableColumns.DISPLAY_NAME)) ?: Build.UNKNOWN
            close()
        }
        return display
    }

    class Factory : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BartekscPDFViewModel::class.java)) {
                return BartekscPDFViewModel() as T
            }
            throw ClassCastException("Unable to create BartekscPDFViewModel from $modelClass.")
        }
    }
}