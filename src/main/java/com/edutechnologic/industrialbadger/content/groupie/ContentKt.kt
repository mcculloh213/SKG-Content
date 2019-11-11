package com.edutechnologic.industrialbadger.content.groupie

import android.util.Log
import android.view.View
import com.edutechnologic.industrialbadger.base.util.DeviceUtil
import com.edutechnologic.industrialbadger.content.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_content.*
import kotlinx.android.synthetic.main.item_hmt_content.*
import ktx.sovereign.database.AWSConfiguration.isFileDownloaded

class ContentKt(val content: String) : Item() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        with(viewHolder) {
            Log.e("check_content", content)
            val arr = content.split("/")
            val file = if (arr.size > 1)
                arr[1]
            else
                arr[0]
            tv_title.text = file.substringBeforeLast('.')
            if (DeviceUtil.DeviceIsHMT()) {
                val tag = "Select Content ${position + 1}"
                val directive = "hf_no_number|hf_no_text|$tag"
                tv_content_description.text = tag
                itemView.contentDescription = directive
            }
            if (content.split("@")[0].toBoolean())
                ivMicroCredential.visibility = View.VISIBLE
            else
                ivMicroCredential.visibility = View.GONE
            if (isFileDownloaded("Contents/$file"))
                tvIndicator.visibility = View.VISIBLE
            else
                tvIndicator.visibility = View.GONE
        }
    }
    override fun getLayout(): Int = if (DeviceUtil.DeviceIsHMT()) {
        R.layout.item_hmt_content
    } else {
        R.layout.item_content
    }
}