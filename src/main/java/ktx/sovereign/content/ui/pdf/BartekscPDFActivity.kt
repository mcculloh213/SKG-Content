package ktx.sovereign.content.ui.pdf

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.android.synthetic.main.activity_barteksc_pdf.*
import ktx.sovereign.content.R
import ktx.sovereign.core.controller.TiltScrollController
import ktx.sovereign.core.util.LogMAR

private const val REQUEST_GET_PDF = 42
private const val SAMPLE_PDF = "Low Vision Rehabilitation.pdf"
class BartekscPDFActivity : AppCompatActivity() {

    private val viewmodel: BartekscPDFViewModel by lazy {
        ViewModelProvider(viewModelStore, BartekscPDFViewModel.Factory())
            .get(BartekscPDFViewModel::class.java)
    }
    private val gyroscope: TiltScrollController by lazy {
        TiltScrollController(this@BartekscPDFActivity, object: TiltScrollController.TiltScrollListener {
            override fun onTilt(x: Int, y: Int) {
//                try {
//                    pdf_view.moveRelativeTo(x.toFloat(), y.toFloat())
//                } catch (ex: Exception) {
//
//                }
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barteksc_pdf)
        viewmodel.title.observe(this, Observer {
            val t = it ?: "PDF Reader"
            title = t
        })
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.barteksc_pdf, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.toggle_night_mode).isChecked = viewmodel.isNightModeEnabled
        return true
    }
    override fun onResume() {
        super.onResume()
        gyroscope.requestRotationSensor()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_open -> {
                openFile(this@BartekscPDFActivity)
                true
            }
            R.id.toggle_night_mode -> {
                val enabled = viewmodel.setNightMode(!item.isChecked)
                enableNightMode(enabled)
                item.isChecked = enabled
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                when (requestCode) {
                    REQUEST_GET_PDF -> {
                        val uri = data?.data ?: return
                        viewmodel.open(this, uri, pdf_view.fromUri(uri)).load()
                    }
                    else -> super.onActivityResult(requestCode, resultCode, data)
                }
            }
            else -> {
                title = SAMPLE_PDF
                pdf_view.apply {
                    setBackgroundColor(Color.LTGRAY)
                    fromAsset(SAMPLE_PDF)
                        .defaultPage(currentPage)
                        .onPageChange(viewmodel)
                        .enableAnnotationRendering(true)
                        .onLoad(viewmodel)
                        .scrollHandle(DefaultScrollHandle(this@BartekscPDFActivity))
                        .swipeHorizontal(true)
                        .pageSnap(true)
                        .autoSpacing(true)
                        .pageFling(true)
                        .onPageError(viewmodel)
                        .pageFitPolicy(FitPolicy.WIDTH)
                        .load()
                }
            }
        }
    }
    override fun onPause() {
        gyroscope.releaseRotationSensor()
        super.onPause()
    }

    fun zoomIn(view: View) {
        pdf_view.zoomWithAnimation(viewmodel.zoomIn())
    }
    fun zoomOut(view: View) {
        pdf_view.zoomWithAnimation(viewmodel.zoomOut())
    }
    fun enableNightMode(enabled: Boolean) {
        pdf_view.setNightMode(enabled)
        pdf_view.invalidate()
    }

    companion object {
        @JvmStatic
        fun openFile(activity: AppCompatActivity) = with(Intent(Intent.ACTION_GET_CONTENT)) {
            type = "application/pdf"
            try {
                activity.startActivityForResult(this, REQUEST_GET_PDF)
            } catch (ex: ActivityNotFoundException) {
                Log.e("OpenFile", "Unable to pick file", ex)
                Toast.makeText(activity, "I am Error!", Toast.LENGTH_LONG).show()
            }
        }
    }
}