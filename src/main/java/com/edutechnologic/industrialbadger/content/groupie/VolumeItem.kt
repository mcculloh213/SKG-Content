package com.edutechnologic.industrialbadger.content.groupie

import com.edutechnologic.industrialbadger.base.util.DeviceUtil
import com.edutechnologic.industrialbadger.content.R
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_hmt_volume.view.*
import ktx.sovereign.database.entity.Volume
import kotlinx.android.synthetic.main.item_volume.*

class VolumeItem (
        val volume: Volume
): Item(), ExpandableItem {
    private var group: ExpandableGroup? = null
    private var holder: ViewHolder? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val tag = "Select Content ${position+1}"
        val directive = "hf_no_number|hf_no_text|$tag"
        holder = viewHolder.also {
            it.iv_icon.setImageResource(if (volume.isCredential) {
                R.drawable.ic_credential
            } else {
                R.drawable.ic_content
            })
            it.tv_header.text = volume.name
            it.itemView.contentDescription = directive
            it.itemView.setOnClickListener {
                group?.onToggleExpanded()
                setToggleIcon()
            }
            if (DeviceUtil.DeviceIsHMT()) {
                it.itemView.tv_content_description.text = tag
            }
        }
    }
    override fun getLayout(): Int = if (DeviceUtil.DeviceIsHMT()) {
        R.layout.item_hmt_volume
    } else {
        R.layout.item_volume
    }
    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        group = onToggleListener
        setToggleIcon()
    }
    fun onDataUpdate() {
        setCount()
        setToggleIcon()
    }
    private fun setCount() {
        holder?.tv_results?.text = "${group?.groupCount ?: 0}"
    }
    private fun setToggleIcon() {
        holder?.iv_toggle?.setImageResource(
            if (group?.isExpanded == true) {
                R.drawable.ic_expand_less
            } else {
                R.drawable.ic_expand_more
            }
        )
    }
}