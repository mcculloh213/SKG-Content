package ktx.sovereign.content.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.SizeF
import android.view.View
import android.webkit.*
import kotlinx.android.synthetic.main.content_view_html.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ktx.sovereign.content.JavaScript
import ktx.sovereign.content.R
import ktx.sovereign.core.util.LogMAR
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

typealias OnPageFinishedListener = (view: WebView?, url: String?) -> Unit
typealias OnScaleChangedListener = (view: WebView?, oldScale: Float, newScale: Float) -> Unit
typealias OnWebViewErrorListener = (view: WebView?, request: WebResourceRequest?, error: WebResourceError?) -> Unit

private const val INTERFACE_NAME: String = "ContentViewer"

@SuppressLint("SetJavaScriptEnabled")
class HTMLContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ContentView(context, attrs), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    /**
     * [LogMAR] utility to handle zoom & scale of [WebView] content.
     *
     *  Values:
     *      Default M Size: 100.00f - Default [WebView] text zoom
     *      Infimum: 0.0f           - Disable ability to decrease text below x1 times
     *      Supremum: 0.9f          - Ability to enlarge text up to x8 times
     */
    private val logmar = LogMAR(100.00f, supremum = 0.9f)

    private var onPageFinishedListener: OnPageFinishedListener? = null
    private var onScaleChangedListener: OnScaleChangedListener? = null
    private var onWebViewErrorListener: OnWebViewErrorListener? = null

    val scale: Float
        get() = _scale
    private var _scale: Float = 1.0f
        set(value) {
//            debug_scale.setText("Scale: x${(value / 100.00f).format(2)}")
            field = value
        }
    val delta: SizeF
        get() = _delta
    private var _delta: SizeF = SizeF(0f, 0f)
        set(value) {
            if (ready.get()) {
//                debug_delta.setText("dX = (${value.width.format(3)}, ${value.height.format(3)})")
            }
            field = value
        }

    private val ready: AtomicBoolean = AtomicBoolean(false)
    private var viewportHeight = 0f
    private var viewportWidth = 0f

    init {
        inflate(context, R.layout.content_view_html, this)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        progress.visibility = View.VISIBLE
        webview.apply {
            visibility = View.INVISIBLE
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    this@HTMLContentView.progress.visibility = View.GONE
                    view?.apply {
                        visibility = View.VISIBLE
                        evaluateJavascript(JavaScript.GetViewportSize) {
                            Log.i("Bridge", "Javascript Evaluated: $it")
                        }
                        onPageFinishedListener?.invoke(view, url)
                    }
                }
                override fun onScaleChanged(view: WebView?, oldScale: Float, newScale: Float) {
                    Log.i("HTMLContentView", "Old: $oldScale\nNew: $newScale")
                    onScaleChangedListener?.invoke(view, oldScale, newScale)
                }
                override fun onReceivedError(
                    view: WebView?, request: WebResourceRequest?, error: WebResourceError?
                ) {
                    onWebViewErrorListener?.invoke(view, request, error)
                }
            }
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                textZoom = logmar.scale.roundToInt()
            }
            addJavascriptInterface(JavaScriptInterface(), INTERFACE_NAME)
            setTag(R.id.is_color_inverted, false)
            _scale = 1.0f
            _delta = SizeF(0f, 0f)
            ready.set(true)
        }
    }

    fun setOnPageFinishedListener(listener: OnPageFinishedListener?) {
        onPageFinishedListener = listener
    }
    fun setOnScaleChangedListener(listener: OnScaleChangedListener?) {
        onScaleChangedListener = listener
    }
    fun setOnWebViewErrorListener(listener: OnWebViewErrorListener?) {
        onWebViewErrorListener = listener
    }

    override fun load(uri: Uri) = webview.loadUrl(uri.toString())
    override fun load(uri: String) = webview.loadUrl(uri)
    override fun tilt(x: Float, y: Float) {
        if (ready.get()) {
//            _delta = SizeF(x, y)
            scrollview.smoothScrollBy((x * 60.0f).roundToInt(), (y * 60.0f).roundToInt())
        }
    }
    override fun invertColors() {
        webview.apply {
            val inverted = getTag(R.id.is_color_inverted) as? Boolean ?: false
            loadUrl(JavaScript.InvertColors)
            setBackgroundColor(context.getColor(
                if (inverted) android.R.color.white else android.R.color.black
            ))
            setTag(R.id.is_color_inverted, !inverted)
        }
    }
    override fun setScaleValue(level: Int) {
        logmar.stepTo(level)
        _scale = logmar.scale
        webview.apply {
            settings.textZoom = logmar.scale.toInt()
            evaluateJavascript(JavaScript.GetViewportSize) {
                Log.i("Bridge", "Javascript Evaluated: $it")
            }
        }
    }
    override fun zoomIn(): Boolean {
        webview.apply {
            settings.textZoom = logmar.stepUp().toInt()
            _scale = logmar.scale
            evaluateJavascript(JavaScript.GetViewportSize) {
                Log.i("Bridge", "Javascript Evaluated: $it")
            }
        }
        return true
    }
    override fun zoomOut(): Boolean {
        webview.apply {
            settings.textZoom = logmar.stepDown().toInt()
            _scale = logmar.scale
            evaluateJavascript(JavaScript.GetViewportSize) {
                Log.i("Bridge", "Javascript Evaluated: $it")
            }
        }
        return true
    }
    override fun pageLeft(): Boolean = false
    override fun pageRight(): Boolean = false
    override fun nextPage(): Boolean = false
    override fun previousPage(): Boolean = false

    @Suppress("UNUSED")
    private inner class JavaScriptInterface {
        @JavascriptInterface
        fun getViewportSize(height: Float, width: Float) {
            Log.d("Interface", "(x,y) := ($width, $height)")
            viewportHeight = height
            viewportWidth = width
        }
    }
}