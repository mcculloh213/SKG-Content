package com.edutechnologic.industrialbadger.content.task

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edutechnologic.industrialbadger.base.util.NotificationManager
import com.edutechnologic.industrialbadger.base.util.NotificationManager.*
import com.edutechnologic.industrialbadger.content.R
import ktx.sovereign.database.entity.Content
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by H.D. "Chip" McCullough on 5/15/2019.
 */
class RapidAutomaticKeywordExtractionTask(notificationManager: NotificationManager) : AsyncTask<Void, Int, Void>() {
    companion object {
        private val TAG: String = RapidAutomaticKeywordExtractionTask::class.java.simpleName
        private val NOTIFICATION_PREPARING: String = "Preparing %s"
        private val NOTIFICATION_ANALYZING: String = "Analyzing %s"
        private val NOTIFICATION_EXTRACTING: String = "Extracting keywords from %s"
        private val NOTIFICATION_FINISHED: String = "Finished analyzing %s"

        private val NOTIFICATION_PROGRESS: String = "%s of 100"

        private val TERM_CLEAN_PATTERN_RAKE: String = "[^a-zA-Z0-9\\-.'&_]"
        private var mNotificationIdMap: Map<String, Int> = HashMap()
    }
    private val mNotificationManagerRef: WeakReference<NotificationManager>
            = WeakReference(notificationManager)
    private val notificationManager: NotificationManager? get() = mNotificationManagerRef.get()
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to [.execute]
     * by the caller of this task.
     *
     * This method can call [.publishProgress] to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     *
     * @return A result, defined by the subclass of this task.
     *
     * @see .onPreExecute
     * @see .onPostExecute
     *
     * @see .publishProgress
     */
    override fun doInBackground(vararg params: Void?): Void {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun prepare() {

    }

    private fun extract() {

    }

    private fun createNotificationBundle(title: String, text: String, notificationId: Int): Bundle {
        return Bundle().apply {
            putString(ARG_CHANNEL_ID, CHANNEL_TEXT_ANALYSIS)
            putString(ARG_CHANNEL_NAME, CHANNEL_TEXT_ANALYSIS_TITLE)
            putString(ARG_CHANNEL_DESCRIPTION, CHANNEL_TEXT_ANALYSIS_DESCRIPTION)
            putInt(ARG_CHANNEL_IMPORTANCE, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            putInt(ARG_SMALL_ICON, R.drawable.ic_content)
            putString(ARG_CONTENT_TITLE, title)
            putString(ARG_CONTENT_TEXT, text)
            putInt(ARG_PRIORITY, NotificationCompat.PRIORITY_DEFAULT)
            putInt(ARG_NOTIFICATION_ID, notificationId)
        }
    }

    private fun getRelatedNotificationId(content: Content): Int {
        Log.d(TAG, "getRelatedNotificationId")
        if (mNotificationIdMap.containsKey(content.checksum))
            return mNotificationIdMap.getValue(content.checksum)

        val id = Random().nextInt()
        return id
    }
}