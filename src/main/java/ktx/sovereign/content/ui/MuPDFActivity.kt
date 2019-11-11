package ktx.sovereign.content.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.FileProvider
import com.artifex.mupdf.viewer.DocumentActivity
import ktx.sovereign.content.R
import java.io.File

class MuPDFActivity : DocumentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mu_pdf)
    }

    companion object {
        @JvmStatic
        fun open(context: Context, file: File) = with (Intent(context, MuPDFActivity::class.java)) {
            action = Intent.ACTION_VIEW
            setDataAndType(
                FileProvider.getUriForFile(context, "", file),
                "application/pdf"
            )
            context.startActivity(this)
        }
    }
}
