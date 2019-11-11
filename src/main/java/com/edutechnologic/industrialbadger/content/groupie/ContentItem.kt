package com.edutechnologic.industrialbadger.content.groupie

import com.edutechnologic.industrialbadger.base.util.DeviceUtil
import com.edutechnologic.industrialbadger.content.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume
import kotlinx.android.synthetic.main.item_content.*
import kotlinx.android.synthetic.main.item_hmt_content.*

class ContentItem(
        val volume: Volume,
        val content: Content,
        private val index: Int
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        with (viewHolder) {
            tv_title.text = content.title.substringBeforeLast('.')
            if (DeviceUtil.DeviceIsHMT()) {
                val tag = "Select Page ${index+1}"
                val directive = "hf_no_number|hf_no_text|$tag"
                tv_content_description.text = tag
                itemView.contentDescription = directive
            }
        }
    }
    override fun getLayout(): Int = if (DeviceUtil.DeviceIsHMT()) {
        R.layout.item_hmt_content
    } else {
        R.layout.item_content
    }
}