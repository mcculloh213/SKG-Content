package ktx.sovereign.content.view

import android.content.Context
import android.graphics.Color
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.math.MathUtils.clamp
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.android.synthetic.main.content_view_pdf.view.*
import kotlinx.coroutines.*
import ktx.sovereign.api.IntelligentMixedReality
import ktx.sovereign.content.R
import ktx.sovereign.core.util.LogMAR
import ktx.sovereign.database.provider.MediaProvider
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

typealias OnPageChangeListener = (page: Int, pageCount: Int) -> Unit
typealias OnPageLoadedListener = (pages: Int) -> Unit
typealias OnPageErrorListener = (page: Int, t: Throwable) -> Unit

private const val MIN_ZOOM = 1.0f
private const val MAX_ZOOM = 10.0f

class PDFContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ContentView(context, attrs), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    var page: Int = 0
        private set

    private val logmar = LogMAR(1.0f, supremum = 1.0f)
    private var onPageChangeListener: OnPageChangeListener? = null
    private var onPageLoadedListener: OnPageLoadedListener? = null
    private var onPageErrorListener: OnPageErrorListener? = null

    private val ready: AtomicBoolean = AtomicBoolean(false)
    private val documentRect: RectF = RectF()

    init {
        inflate(context, R.layout.content_view_pdf, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        progress.visibility = View.VISIBLE
        pdfview.apply {
            setBackgroundColor(Color.LTGRAY)
            setTag(R.id.is_color_inverted, false)
        }
    }

    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        onPageChangeListener = listener
    }
    fun setOnPageLoadedListener(listener: OnPageLoadedListener?) {
        onPageLoadedListener = listener
    }
    fun setOnPageErrorListener(listener: OnPageErrorListener?) {
        onPageErrorListener = listener
    }

    override fun load(uri: Uri) = Unit
    override fun load(uri: String) {
        launch(Dispatchers.Main) {
            pdfview.fromFile(fetch(uri))
                .defaultPage(page)
                .onPageChange { page, pageCount ->
                    ready.set(false)
                    pdfview.getPageSize(page).let { size ->
                        with(documentRect) {
                            left = 0f
                            top = 0f
                            right = size.width
                            bottom = size.height
                        }
                    }
                    onPageChangeListener?.invoke(page, pageCount)
                    ready.set(true)
                }
                .enableAnnotationRendering(true)
                .onLoad { pages ->
                    progress.visibility = View.GONE
                    onPageLoadedListener?.invoke(pages)
                }
                .swipeHorizontal(true)
                .pageSnap(true)
                .autoSpacing(true)
                .pageFling(true)
                .onPageError { page, t -> onPageErrorListener?.invoke(page, t) }
                .pageFitPolicy(FitPolicy.WIDTH)
                .load()
        }
    }
    override fun tilt(x: Float, y: Float) {
        if (ready.get()) {
            pdfview.apply {
                val dx = max(0f, min(this.scrollX + (x * 60.0f),
                    (toCurrentScale(documentRect.right)) - this.width)
                ) - this.scrollX
                val dy = max(0f, min(this.scrollY + (y * 60.0f),
                    (toCurrentScale(documentRect.bottom)) - this.height)
                ) - this.scrollY
                this.scrollBy(toCurrentScale(dx).roundToInt(), toCurrentScale(dy).roundToInt())
            }
        }
    }
    override fun invertColors() {
        pdfview.apply {
            val invert = (getTag(R.id.is_color_inverted) as? Boolean ?: false).not()
            setNightMode(invert)
            invalidate()
            setTag(R.id.is_color_inverted, invert)
        }
    }
    override fun setScaleValue(level: Int) {
        logmar.stepTo(level)
        val z = clamp(logmar.scale, MIN_ZOOM, MAX_ZOOM)
        if (z != pdfview.zoom) {
            pdfview.zoomWithAnimation(z)
        }
    }
    override fun zoomIn(): Boolean {
        val z = min(logmar.stepUp(), MAX_ZOOM)
        return if (z > pdfview.zoom) {
            pdfview.zoomWithAnimation(z)
            true
        } else {
            false
        }
    }
    override fun zoomOut(): Boolean {
        val z = max(logmar.stepDown(), MIN_ZOOM)
        return if (z < pdfview.zoom) {
            pdfview.zoomWithAnimation(z)
            true
        } else {
            false
        }
    }
    override fun pageLeft(): Boolean = if (pdfview.currentPage > 0) {
        pdfview.jumpTo(pdfview.currentPage - 1)
        true
    } else {
        false
    }
    override fun pageRight(): Boolean = if (pdfview.currentPage < pdfview.pageCount) {
        pdfview.jumpTo(pdfview.currentPage + 1)
        true
    } else {
        false
    }
    override fun nextPage(): Boolean = pageRight()
    override fun previousPage(): Boolean = pageLeft()

    private suspend fun fetch(uri: String) : File = coroutineScope {
        async {
            try {
                val segments = Uri.parse(uri).pathSegments
                val file = withContext(Dispatchers.Main) {
                    MediaProvider.getContentFile(
                        context,
                        segments[segments.size - 2],
                        segments[segments.size - 1]
                    )
                }
                if (file.exists() && file.length() != 0L) {
                    return@async file
                } else {
                    IntelligentMixedReality.Content.getFile(uri).writeToFile(file)
                    return@async file
                }
            } catch (ex: Exception) {
                Log.e("PDFContentView", "Failed to fetch file", ex)
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to fetch file", Toast.LENGTH_LONG).show()
                }
                throw ex
            }
        }.await()
    }
    private fun ResponseBody.writeToFile(dest: File) {
        try {
            val buffer = ByteArray(4096)
            val len = contentLength()
            var downloaded = 0L
            FileOutputStream(dest).use { out ->
                byteStream().use { server ->
                    while (true) {
                        val read = server.read(buffer)
                        if (read == -1) break
                        out.write(buffer, 0, read)
                        downloaded += read
                        Log.i(dest.nameWithoutExtension, "----- Downloaded $downloaded of $len -----")
                    }
                }
                out.flush()
            }
        } catch (ex: Exception) {
            Log.e("PDFContentView", "Failed to write file", ex)
            throw ex
        }
    }
}