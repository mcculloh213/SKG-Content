package ktx.sovereign.content.groupie

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_content.view.*
import kotlinx.android.synthetic.main.item_hmt_content.view.*
import ktx.sovereign.content.R
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume
import ktx.sovereign.hmt.extension.isHMT

class ContentItem(
    val volume: Volume,
    val content: Content
) : Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        with (viewHolder.itemView) {
            tv_title.text = content.title.substringBeforeLast('.')
            if (isHMT()) {
                val tag = "Select Content ${position+1}"
                val directive = "hf_no_number|hf_no_text|$tag"
                tv_content_description.text = tag
                contentDescription = directive
            }
        }
    }
    override fun getLayout(): Int = if (isHMT()) {
        R.layout.item_hmt_content
    } else {
        R.layout.item_content
    }
}