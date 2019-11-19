package ktx.sovereign.content.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.widget.FrameLayout
import ktx.sovereign.database.entity.Content

abstract class ContentView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    abstract fun load(uri: Uri)
    abstract fun load(uri: String)
    abstract fun tilt(x: Float, y: Float)
    abstract fun invertColors()
    abstract fun setScaleValue(level: Int)
    abstract fun zoomIn(): Boolean
    abstract fun zoomOut(): Boolean

    //region HMT-1 Utility Functions
//    abstract fun pageUp(): Boolean
//    abstract fun pageDown(): Boolean
    abstract fun pageLeft(): Boolean
    abstract fun pageRight(): Boolean
    abstract fun nextPage(): Boolean
    abstract fun previousPage(): Boolean
    //endregion

    class Factory {
        fun build(context: Context, content: Content): ContentView = when {
            content.title.contains(".pdf") -> PDFContentView(context)
            content.title.contains(".html") -> HTMLContentView(context)
            else -> throw IllegalArgumentException("Unsupported file type: ${content.title}")
        }
    }
}