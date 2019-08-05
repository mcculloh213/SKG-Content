package ktx.sovereign.content.fragment

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Section
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.fragment_content_list.*
import ktx.sovereign.content.R
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.content.groupie.ContentItem
import ktx.sovereign.content.groupie.VolumeItem
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume

class ContentIndexFragment : ContentPresenterFragment() {
    companion object {
        @JvmStatic fun newInstance(): ContentIndexFragment = ContentIndexFragment()
    }

    private val adapter: GroupAdapter<ViewHolder> = GroupAdapter()
    private val volumeMap: HashMap<String, Pair<VolumeItem, Section>> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_content_list, container, false)
        delegate?.notifyViewCreated(this@ContentIndexFragment)
        return view
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        content_recycler_view.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = this@ContentIndexFragment.adapter
        }
    }
    override fun onResume() {
        super.onResume()
        adapter.setOnItemClickListener { item, _ ->
            when (item) {
                is ContentItem -> {
                    presenter?.onContentClicked(item.content)
                }
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sync -> {
                presenter?.onSyncClicked()
                view?.let {
                    Snackbar.make(it, "Synchronizing Content", Snackbar.LENGTH_LONG).show()
                }
                true
            }
            else ->  super.onOptionsItemSelected(item)
        }
    }

    override fun displayState(state: ContentContract.State) {
        updateIndex(state.getVolumeList(), state.getContentList())
    }
    override fun displayIndex(state: ContentContract.State) {
        updateIndex(state.getVolumeList(), state.getContentList())
    }
    override fun displayDetails() {
        delegate?.requestNavigationEvent(R.id.action_index_to_detail)
    }
    private fun updateIndex(volumes: List<Volume>, content: List<Content>) {
        volumes.forEach { volume ->
            val filter = content.filter { it.token == volume.id }
            val pair = volumeMap[volume.token]
            if (pair == null) {
                createMapping(volume, filter)
            } else {
                updateMapping(pair, volume, filter)
            }
        }
    }
    private fun createMapping(volume: Volume, content: List<Content>) {
        val container = VolumeItem(volume)
        val group = ExpandableGroup(container)
        val section = Section()
        val items = ArrayList<ContentItem>()
        adapter.add(group)
        items.addAll(content.map { ContentItem(volume, it) })
        group.add(section.apply { addAll(items) })
        container.onDataUpdate()
        volumeMap[volume.token] = Pair(container, section)
    }
    private fun updateMapping(pair: Pair<VolumeItem, Section>, volume: Volume, content: List<Content>) {
        val items = ArrayList<ContentItem>()
        items.addAll(content.map { ContentItem(volume, it) })
        pair.second.update(items)
        pair.first.onDataUpdate()
    }
}