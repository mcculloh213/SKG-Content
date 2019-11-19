package ktx.sovereign.content.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
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

class ContentIndexFragment : ContentPresenterFragment(), ContentContract.SearchManager {
    companion object {
        @JvmStatic fun newInstance(): ContentIndexFragment = ContentIndexFragment()
    }

    private val adapter: GroupAdapter<ViewHolder> = GroupAdapter()
    private val volumeMap: HashMap<String, ContentMap> = HashMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val view = inflater.inflate(R.layout.fragment_content_list, container, false)
        delegate?.notifyViewCreated(this@ContentIndexFragment)
        return view
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_content_list, menu)
        with (menu.findItem(R.id.menu_search).actionView as SearchView) {
            setOnQueryTextListener(this@ContentIndexFragment)
        }
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
                    presenter?.onContentClicked(item.volume, item.content)
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
        updateIndex(state.getVolumeList(), state.getContentList(), state.getQuery())
    }
    override fun displayIndex(state: ContentContract.State) {
        updateIndex(state.getVolumeList(), state.getContentList(), state.getQuery())
    }
    override fun displayDetails() {
        delegate?.requestNavigationEvent(R.id.action_index_to_detail)
    }
    override fun onQueryTextSubmit(query: String?): Boolean {
        presenter?.onQueryChanged(query)
        return true
    }
    override fun onQueryTextChange(newText: String?): Boolean {
        presenter?.onQueryChanged(newText)
        return true
    }
    private fun updateIndex(volumes: List<Volume>, content: List<Content>, query: String? = null) {
        volumes.forEach { volume ->
            val filter = if (query.isNullOrEmpty()) {
                content.filter { it.token == volume.id }
            } else {
                content.filter { it.token == volume.id && (volume.name.contains(query, true) || it.title.contains(query, true))}
            }
            val map = volumeMap[volume.id]
            if (map == null) {
                createMapping(volume, filter)
            } else {
                updateMapping(map, volume, filter)
            }
        }
    }
    private fun createMapping(volume: Volume, content: List<Content>) {
        val container = VolumeItem(volume)
        val group = ExpandableGroup(container)
        val section = Section()
        val items = ArrayList<ContentItem>()
        adapter.add(group)
        items.addAll(content.mapIndexed { index, ct -> ContentItem(volume, ct, index) })
        group.add(section.apply { addAll(items) })
        container.onDataUpdate()
        volumeMap[volume.id] = ContentMap(adapter.getAdapterPosition(group), group, container, section) //Triple(group, container, section)
    }
    private fun updateMapping(map: ContentMap, volume: Volume, content: List<Content>) {
        if (content.isEmpty()) {
            try {
                adapter.apply {
                    if (this.getAdapterPosition(map.group) > -1) {
                        remove(map.group)
                    }
                }
            } catch (ex: IllegalArgumentException) {
                Log.e("Groupie", ex.message ?: "Exception occurred while removing element from list")
                ex.printStackTrace()
            }
        } else {
            val items = ArrayList<ContentItem>()
            if (adapter.getAdapterPosition(map.group) == -1) { adapter.add(map.group) }
            items.addAll(content.mapIndexed { index, ct -> ContentItem(volume, ct, index) })
            map.section.update(items)
            map.volume.onDataUpdate()
        }
        adapter.notifyDataSetChanged()
    }

    private data class ContentMap (
        val index: Int,
        val group: ExpandableGroup,
        val volume: VolumeItem,
        val section: Section
    )
}