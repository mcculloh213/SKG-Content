package ktx.sovereign.content.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_content_detail.*
import kotlinx.android.synthetic.main.item_webview.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.sovereign.content.R
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.core.controller.TiltScrollController
import ktx.sovereign.core.util.LogMAR
import ktx.sovereign.core.view.FloatingActionButtonMenu
import ktx.sovereign.database.entity.Content
import ktx.sovereign.hmt.SpeechRecognizer
import kotlin.math.min
import kotlin.math.roundToInt

class ContentDetailFragment : ContentPresenterFragment() {
    companion object {
        @JvmStatic fun newInstance(): ContentDetailFragment = ContentDetailFragment()
        private val ARG_INDEX = "com.industrialbadger.content.detail.INDEX"
        private val INVERT_COLORS = ("javascript: ("
                + "function () { "

                + "var css = 'html {-webkit-filter: invert(100%);' +"
                + "    '-moz-filter: invert(100%);' + "
                + "    '-o-filter: invert(100%);' + "
                + "    '-ms-filter: invert(100%); }',"

                + "head = document.getElementsByTagName('head')[0],"
                + "style = document.createElement('style');"

                + "if (!window.counter) { window.counter = 1;} else  { window.counter ++;"
                + "if (window.counter % 2 == 0) { var css ='html {-webkit-filter: invert(0%); -moz-filter:    invert(0%); -o-filter: invert(0%); -ms-filter: invert(0%); }'}"
                + "};"

                + "style.type = 'text/css';"
                + "if (style.styleSheet){"
                + "style.styleSheet.cssText = css;"
                + "} else {"
                + "style.appendChild(document.createTextNode(css));"
                + "}"

                //injecting the css to the head
                + "head.appendChild(style);"
                + "}());")
    }
    private val GET_SCROLLABLE_AREA: String = "javascript:ContentViewer.getScrollableArea(document.body.scrollHeight, document.body.scrollWidth)"

    /**
     * [LogMAR] utility to handle zoom & scale of [WebView] content.
     *
     *  Values:
     *      Default M Size: 100.00f - Default [WebView] text zoom
     *      Infimum: 0.0f           - Disable ability to decrease text below x1 times
     *      Supremum: 0.9f          - Ability to enlarge text up to x8 times
     */
    private val logmar: LogMAR = LogMAR(100.00f, supremum = 0.9f)
    private val adapter: ContentPagerAdapter = ContentPagerAdapter()
    private val gyroscope: TiltScrollController by lazy {
        TiltScrollController(requireContext(), object: TiltScrollController.TiltScrollListener {
            override fun onTilt(x: Int, y: Int) {
                with(adapter.getCurrentWebView(content_view_pager.currentItem)) {
                    // width
                    val dx = when {
                        x > 0 -> if ((scrollX + x) > viewportWidth) {
                            min((viewportWidth - scrollX), (viewportHeight - x)).roundToInt()
                        } else x
                        x < 0 -> if ((scrollX + x) < 0) (-1 * scrollX) else x
                        else -> 0
                    }
                    // height
                    val dy = when {
                        y > 0 -> if ((scrollY + y) > viewportHeight) {
                            min((viewportHeight - scrollY), (viewportHeight - y)).roundToInt()
                        } else y
                        y < 0 -> if ((scrollY + y) < 0) (-1 * scrollY) else y
                        else -> 0
                    }
                    scrollBy(dx, dy)
                }
            }
        })
    }
    private val hmtSpeechRecognizer: SpeechRecognizer = SpeechRecognizer(SpeechRecognizer.CommandList(
        extraCommands = listOf(
            "Freeze",
            "Freeze Document",
            "Invert",
            "Invert Document",
            "Toggle",
            "Toggle Menu",
            "Page Up",
            "Page Right",
            "Page Down",
            "Page Left",
            "Zoom In",
            "Zoom Out"
        )
    ))
    private var idx: Int = 0
    private var viewportHeight: Float = 0f
    private var viewportWidth: Float = 0f
    private lateinit var menu: FloatingActionButtonMenu

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hmtSpeechRecognizer.onAttach(context)
        menu = FloatingActionButtonMenu(context).apply {
            addButton(R.id.menu_fab_freeze, R.drawable.ic_freeze, "Freeze") {
                gyroscope.toggleFreeze()
            }
            addButton(R.id.menu_fab_invert, R.drawable.ic_invert_color, "Invert") {
                adapter.getCurrentWebView(content_view_pager.currentItem).apply {
                    val inverted = getTag(R.id.is_color_inverted) as Boolean? ?: false
                    loadUrl(INVERT_COLORS)
                    setBackgroundColor(context.getColor(
                        if (inverted) android.R.color.white else android.R.color.black
                    ))
                    setTag(R.id.is_color_inverted, !inverted)
                }
            }
            addButton(R.id.menu_fab_zoom_in, R.drawable.ic_zoom_in, "Zoom In") {
                adapter.getCurrentWebView(content_view_pager.currentItem).let { view ->
                    view.settings.apply {
                        slot_view.increaseZoomLevel()
                        textZoom = logmar.stepUp().toInt()
                    }
                    view.loadUrl(GET_SCROLLABLE_AREA)
                }
            }
            addButton(R.id.menu_fab_zoom_out, R.drawable.ic_zoom_out, "Zoom Out") {
                adapter.getCurrentWebView(content_view_pager.currentItem).let { view ->
                    view.settings.apply {
                        slot_view.decreaseZoomLevel()
                        textZoom = logmar.stepDown().toInt()
                    }
                    view.loadUrl(GET_SCROLLABLE_AREA)
                }
            }
        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_content_detail, container, false)
        delegate?.notifyViewCreated(this@ContentDetailFragment)
        return view
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        delegate?.createExtraMenu(menu)
        content_view_pager.apply {
            if (adapter == null) {
                adapter = this@ContentDetailFragment.adapter
            }
            currentItem = savedInstanceState?.getInt(ARG_INDEX) ?: idx
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) { }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
                override fun onPageSelected(position: Int) {
                    (adapter as ContentPagerAdapter?)?.getCurrentWebView(position)?.let {
                        it.settings.textZoom = logmar.scale.roundToInt()
                        it.loadUrl(GET_SCROLLABLE_AREA)
                    }
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        gyroscope.requestRotationSensor()
        hmtSpeechRecognizer.onResume()
        slot_view.setOnClickSlotListener {
            onClick = {
                adapter.getCurrentWebView(content_view_pager.currentItem).let { view ->
                    view.settings.apply {
                        textZoom = logmar.stepTo(it).roundToInt()
                    }
                    view.loadUrl(GET_SCROLLABLE_AREA)
                }
            }
        }
        hmtSpeechRecognizer.setSpeechEventListener {
            onSpeechEvent = { command ->
                when (command) {
                    "Freeze", "Freeze Document" -> gyroscope.toggleFreeze()
                    "Invert", "Invert Document" -> {
                        adapter.getCurrentWebView(content_view_pager.currentItem).apply {
                            val inverted = getTag(R.id.is_color_inverted) as Boolean? ?: false
                            loadUrl(INVERT_COLORS)
                            setBackgroundColor(context.getColor(
                                if (inverted) android.R.color.white else android.R.color.black
                            ))
                            setTag(R.id.is_color_inverted, !inverted)
                        }
                    }
                    "Toggle", "Toggle Menu" -> menu.toggle()
                    "Zoom In" -> {
                        adapter.getCurrentWebView(content_view_pager.currentItem).let { view ->
                            view.settings.apply {
                                slot_view.increaseZoomLevel()
                                textZoom = logmar.stepUp().toInt()
                            }
                            view.loadUrl(GET_SCROLLABLE_AREA)
                        }
                    }
                    "Zoom Out" -> {
                        adapter.getCurrentWebView(content_view_pager.currentItem).let { view ->
                            view.settings.apply {
                                slot_view.decreaseZoomLevel()
                                textZoom = logmar.stepDown().toInt()
                            }
                            view.loadUrl(GET_SCROLLABLE_AREA)
                        }
                    }
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_save -> {
                handleSave(adapter.getCurrentContent(content_view_pager.currentItem))
                true
            }
            else ->  super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_INDEX, content_view_pager.currentItem)
        super.onSaveInstanceState(outState)
    }
    override fun onPause() {
        gyroscope.releaseRotationSensor()
        hmtSpeechRecognizer.onPause()
        super.onPause()
    }
    override fun onDestroy() {
        delegate?.removeExtraMenu()
        hmtSpeechRecognizer.onDestroy()
        super.onDestroy()
    }

    override fun displayState(state: ContentContract.State) {
        adapter.setContent(state.getCurrentContent())
        idx = state.getCurrentIndex()
    }

    private fun handleSave(content: Content) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = presenter?.onSaveClickedAsync(content)?.await() ?: false
            withContext(Dispatchers.Main) {
                view?.let {
                    if (success) {
                        Snackbar.make(it, "Saved ${content.title.substringBeforeLast('.')}", Snackbar.LENGTH_SHORT).apply {
                            val tv = TypedValue()
                            context.theme.resolveAttribute(R.attr.colorSecondary, tv, true)
                            view.setBackgroundColor(tv.data)
                        }.show()
                    } else {
                        Snackbar.make(it, "Failed to save ${content.title.substringBeforeLast('.')}", Snackbar.LENGTH_SHORT).apply {
                            val tv = TypedValue()
                            context.theme.resolveAttribute(R.attr.colorError, tv, true)
                            view.setBackgroundColor(tv.data)
                        }.show()
                    }
                }
            }
        }
    }

    inner class ContentPagerAdapter : PagerAdapter() {
        private val content: ArrayList<Content> = ArrayList()
        private val textMap: HashMap<String, String?> = HashMap()
        private val registeredViews: SparseArray<View> = SparseArray()
        fun setContent(values: List<Content>) {
            content.clear()
            content.addAll(values)
            notifyDataSetChanged()
        }

        fun getCurrentWebView(position: Int): WebView = registeredViews[position].webview
        fun getCurrentContent(position: Int): Content = content[position]
        fun getCurrentText(position: Int): String? = textMap[content[position].id]

        @SuppressLint("SetJavaScriptEnabled")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(container.context)
                .inflate(R.layout.item_webview, container, false)
            view.apply {
                webview.visibility = View.INVISIBLE
                progress.visibility = View.VISIBLE
                webview.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        progress.visibility = View.GONE
                        view?.apply {
                            visibility = View.VISIBLE
                            loadUrl(GET_SCROLLABLE_AREA)
                        }
                    }
                }
                webview.settings.apply {
                    javaScriptEnabled = true
                    textZoom = logmar.scale.roundToInt()
//                    loadWithOverviewMode = true
//                    useWideViewPort = true
                }
                webview.setTag(R.id.is_color_inverted, false)
                webview.addJavascriptInterface(ContentViewerInterface(), "ContentViewer")
                CoroutineScope(Dispatchers.Main).launch {
                    with (content[position]) {
                        var payload = textMap[id]
                        if (payload == null) {
                            payload = presenter?.requestRead(this)
                        }
                        webview.loadData(payload, "text/html; charset=utf-8", "UTF-8")
                        textMap[id] = payload
                    }
                }
                registeredViews.put(position, this)
            }
            container.addView(view)
            return view
        }
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            registeredViews.remove(position)
            container.removeView(`object` as View)
        }
        override fun getCount(): Int = content.size
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }
    }
    inner class ContentViewerInterface {
        @JavascriptInterface
        fun getScrollableArea(height: Float, width: Float) {
//            Log.d("Interface", "(x,y) := ($width, $height)")
            viewportHeight = height
            viewportWidth = width
        }
    }
}