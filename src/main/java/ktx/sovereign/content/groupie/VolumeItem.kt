package ktx.sovereign.content.groupie

import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import ktx.sovereign.content.R
import ktx.sovereign.database.entity.Volume
import kotlinx.android.synthetic.main.item_volume.view.*

class VolumeItem (
    val volume: Volume
): Item(), ExpandableItem {
    private var group: ExpandableGroup? = null
    private var holder: ViewHolder? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val directive = "hf_no_number|hf_show_text|Select Item $position"
        holder = viewHolder.also {
            it.itemView.apply {
                iv_icon.setImageResource(R.drawable.ic_content)
                tv_header.text = volume.name
                contentDescription = directive
                setOnClickListener {
                    group?.onToggleExpanded()
                    setToggleIcon()
                }
            }
        }
    }
    override fun getLayout(): Int = R.layout.item_volume
    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        group = onToggleListener
        setToggleIcon()
    }
    fun onDataUpdate() {
        setCount()
        setToggleIcon()
    }
    private fun setCount() {
        holder?.itemView?.tv_results?.text = "${group?.groupCount ?: 0}"
    }
    private fun setToggleIcon() {
        holder?.itemView?.iv_toggle?.setImageResource(
            if (group?.isExpanded == true) {
                R.drawable.ic_expand_less
            } else {
                R.drawable.ic_expand_more
            }
        )
    }
}