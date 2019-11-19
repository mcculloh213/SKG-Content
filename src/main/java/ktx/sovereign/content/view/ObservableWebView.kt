package ktx.sovereign.content.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class ObservableWebView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int
) : WebView(context, attrs, defStyle) {
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        if (event.findPointerIndex(0) == -1) return super.onTouchEvent(event)
        if (event.pointerCount >= 2) {
            requestDisallowInterceptTouchEvent(true)
        } else {
            requestDisallowInterceptTouchEvent(false)
        }
        return super.onTouchEvent(event)
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY)
        requestDisallowInterceptTouchEvent(true)
    }
}