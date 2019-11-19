package ktx.sovereign.content.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.util.TypedValue
import android.view.*
import android.webkit.*
import androidx.core.math.MathUtils.clamp
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_content_detail.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ktx.hdmccullough.sensors.SensorFactory
import ktx.hdmccullough.sensors.UnitCircle
import ktx.hdmccullough.sensors.motion.RotationVector
import ktx.sovereign.api.S3Client
import ktx.sovereign.content.R
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.content.view.ContentView
import ktx.sovereign.core.util.LogMAR
import ktx.sovereign.core.view.SlotView
import ktx.sovereign.database.entity.Content
import ktx.sovereign.hmt.SpeechRecognizer
import ktx.sovereign.util.angularRounding
import ktx.sovereign.util.threshold
import ktx.sovereign.util.toDegrees
import ktx.sovereign.util.toast
import java.util.concurrent.atomic.AtomicBoolean

class ContentDetailFragment : ContentPresenterFragment() {
    companion object {
        @JvmStatic fun newInstance(): ContentDetailFragment = ContentDetailFragment()
        private val ARG_INDEX = "com.industrialbadger.content.detail.INDEX"
    }


    /**
     * [LogMAR] utility to handle zoom & scale of [WebView] content.
     *
     *  Values:
     *      Default M Size: 100.00f - Default [WebView] text zoom
     *      Infimum: 0.0f           - Disable ability to decrease text below x1 times
     *      Supremum: 0.9f          - Ability to enlarge text up to x8 times
     */
    private val logmar: LogMAR = LogMAR(1.0f, supremum = 0.9f)
    private val adapter: ContentPagerAdapter = ContentPagerAdapter()
    private val tracking: AtomicBoolean = AtomicBoolean(false)
    private val orientation: FloatArray = FloatArray(3)
    private val azimuth: Float
        get() = clamp(orientation[0], -1f * UnitCircle.RAD_180, UnitCircle.RAD_180).toDegrees()
    private val pitch: Float
        get() = clamp(orientation[1], -1f * UnitCircle.RAD_180, UnitCircle.RAD_180).toDegrees()
    private val roll: Float
        get() = clamp(orientation[2], -1f * UnitCircle.RAD_180 / 2f, UnitCircle.RAD_180 / 2f).toDegrees()

    private var worldX: Float = 0f
    private var worldZ: Float = 0f

    private val sensor: RotationVector by lazy {
        SensorFactory(requireContext()).get(RotationVector::class.java).also {
            it.setOnSensorDataChangedListener {
                synchronized(this@ContentDetailFragment) {
                    sensor.adjustedOrientation.copyInto(orientation)
                    val (a, p, r) = Triple(azimuth, pitch, roll)
//                    Log.i("RotationVector", "<$a, $p, $r>")
                    var (dx, dz) = Pair(
                        (p - worldX).angularRounding().threshold(),
                        (a - worldZ).angularRounding().threshold()
                    )

                    if (!tracking.get()) {
                        dx = 0f
                        dz = 0f
                        tracking.set(true)
                    }

                    worldX = p
                    worldZ = a
                    adapter.getContentView(content_view_pager.currentItem).tilt(dz, dx)
                }
            }
        }
    }
    private val hmtSpeechRecognizer: SpeechRecognizer = SpeechRecognizer(SpeechRecognizer.CommandList(
        extraCommands = listOf(
            "Assessment",
            "Freeze", "Freeze Document",
            "Invert", "Invert Document",
            "Toggle", "Toggle Menu", "Content Menu",
            "Page Up", "Page Down",
            "Next Page", "Page Right",
            "Previous Page", "Page Left",
            "Zoom In",
            "Zoom Out",
            "Zoom Level One",
            "Zoom Level Two",
            "Zoom Level Three",
            "Zoom Level Four",
            "Zoom Level Five",
            "Zoom Level Six",
            "Zoom Level Seven",
            "Zoom Level Eight",
            "Zoom Level Nine",
            "Zoom Level Ten"
        )
    ))
    private var idx: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hmtSpeechRecognizer.onAttach(context)
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
        fab_action_menu.apply {
            setToggleClickListener(View.OnClickListener { close() })
            setOnFloatingActionOptionItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.action_freeze -> toggleGyroscope()
                    R.id.action_invert_colors -> invertColors()
                    R.id.action_zoom_in -> zoomIn()
                    R.id.action_zoom_out -> zoomOut()
                }
            }
        }
        content_view_pager.apply {
            if (adapter == null) {
                adapter = this@ContentDetailFragment.adapter
            }
            currentItem = savedInstanceState?.getInt(ARG_INDEX) ?: idx
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) { }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) { }
                override fun onPageSelected(position: Int) {
                    (adapter as ContentPagerAdapter?)?.getContentView(position)?.setScaleValue(slot_view.currentLevel)
                }
            })
        }
    }
    override fun onResume() {
        super.onResume()
        sensor.start()
        hmtSpeechRecognizer.onResume()
        slot_view.setOnClickSlotListener {
            onClick = { slot ->
                logmar.stepTo(slot)
                adapter.getContentView(content_view_pager.currentItem).setScaleValue(slot)
            }
        }
        hmtSpeechRecognizer.setSpeechEventListener {
            onSpeechEvent = { command ->
                when (command) {
                    "Assessment" -> {
                        //TODO: Fix this.
                        if (content_view_pager.currentItem == adapter.count - 1) {
                            context?.packageManager?.getLaunchIntentForPackage(
                                "com.augmentir.glass.runtime"
                            )?.let { startActivity(it) }
                        }
                    }
                    "Freeze", "Freeze Document" -> toggleGyroscope()
                    "Invert", "Invert Document" -> invertColors()
                    "Toggle", "Toggle Menu", "Content Menu" -> fab_action_menu.performClick()
                    "Zoom In" -> zoomIn()
                    "Zoom Out" -> zoomOut()
                    "Next Page", "Page Right" -> {
                        if (!adapter.getContentView(content_view_pager.currentItem).nextPage()) {
                            content_view_pager.setCurrentItem(
                                content_view_pager.currentItem + 1,
                                true
                            )
                        }
                    }
                    "Previous Page", "Page Left" -> {
                        if (!adapter.getContentView(content_view_pager.currentItem).previousPage()) {
                            content_view_pager.setCurrentItem(
                                content_view_pager.currentItem - 1,
                                true
                            )
                        }
                    }
                    "Zoom Level One" -> zoomTo(SlotView.ZoomLevel.ONE)
                    "Zoom Level Two" -> zoomTo(SlotView.ZoomLevel.TWO)
                    "Zoom Level Three" -> zoomTo(SlotView.ZoomLevel.THREE)
                    "Zoom Level Four" -> zoomTo(SlotView.ZoomLevel.FOUR)
                    "Zoom Level Five" -> zoomTo(SlotView.ZoomLevel.FIVE)
                    "Zoom Level Six" -> zoomTo(SlotView.ZoomLevel.SIX)
                    "Zoom Level Seven" -> zoomTo(SlotView.ZoomLevel.SEVEN)
                    "Zoom Level Eight" -> zoomTo(SlotView.ZoomLevel.EIGHT)
                    "Zoom Level Nine" -> zoomTo(SlotView.ZoomLevel.NINE)
                    "Zoom Level Ten" -> zoomTo(SlotView.ZoomLevel.TEN)
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
        sensor.stop()
        hmtSpeechRecognizer.onPause()
        super.onPause()
    }
    override fun onDestroy() {
        delegate?.removeExtraMenu()
        hmtSpeechRecognizer.onDestroy()
        super.onDestroy()
    }

    override fun displayState(state: ContentContract.State) {
        idx = state.getCurrentIndex()
        adapter.setContent(state.getCurrentContent())
        content_view_pager.currentItem = idx
    }
    private fun toggleGyroscope() = with(sensor) {
        if (listening) { stop() } else { start() }
    }
    private fun invertColors() = adapter.getContentView(content_view_pager.currentItem).invertColors()
    private fun zoomIn() {
        if (adapter.getContentView(content_view_pager.currentItem).zoomIn()) {
            slot_view.increaseZoomLevel()
        } else {
            context?.toast("At Max")
        }
    }
    private fun zoomOut() {
        if (adapter.getContentView(content_view_pager.currentItem).zoomOut()) {
            slot_view.decreaseZoomLevel()
        } else {
            context?.toast("At Min")
        }
    }
    private fun zoomTo(level: SlotView.ZoomLevel) {
        slot_view.setCurrentZoomLevel(level)
        logmar.stepTo(level.value)
        adapter.getContentView(content_view_pager.currentItem)
            .setScaleValue(level.value)
    }
    private fun handleSave(content: Content) {
        CoroutineScope(Dispatchers.IO).launch {
            val success = presenter?.onSaveClickedAsync(content)?.await() ?: false
            withContext(Dispatchers.Main) {
                view?.let {
                    if (success) {
                        Snackbar.make(it, "Saved ${content.title.substringBeforeLast('.')}", Snackbar.LENGTH_SHORT).apply {
                            val tv = TypedValue()
                            context.theme.resolveAttribute(R.attr.colorPrimary, tv, true)
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
        private val registeredViews: SparseArray<ContentView> = SparseArray()
        fun setContent(values: List<Content>) {
            content.clear()
            content.addAll(values)
            notifyDataSetChanged()
        }

        fun getContentView(position: Int): ContentView = registeredViews[position]
        fun getCurrentContent(position: Int): Content = content[position]

        @SuppressLint("SetJavaScriptEnabled")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val c = content[position]
            val view: ContentView = ContentView.Factory().build(container.context, c)
            CoroutineScope(Dispatchers.Main).launch {
                with (c) {
                    try {
                        view.load(S3Client.url("edutechnologic.motoman", token, title))
                    } catch (ex: Exception) {
//                        webview.loadData(payload, "text/html; charset=utf-8", "UTF-8")
                    }
                }
            }
            registeredViews.put(position, view)
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
}