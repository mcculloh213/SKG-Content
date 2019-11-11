package ktx.sovereign.content.contract

import android.os.Bundle
import kotlinx.coroutines.Deferred
import ktx.sovereign.core.contract.BaseDelegate
import ktx.sovereign.core.contract.BasePresenter
import ktx.sovereign.core.contract.BaseView
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume
import ktx.sovereign.database.repository.ContentRepository
import ktx.sovereign.database.repository.VolumeRepository

interface ContentContract {
    enum class Ctx {
        INDEX, DETAILS, TRAINING, SEARCH
    }
    interface DependencyInjection {
        fun injectVolumeRepository(): VolumeRepository
        fun injectContentRepository(): ContentRepository
    }
    interface Delegate: BaseDelegate {
        fun notifyViewCreated(view: View)
        fun notifyDialogCreated(dialog: Dialog)
    }
    interface Presenter: BasePresenter {
        fun registerView(view: View)
        fun onViewCreated()
        fun saveState(): Bundle
        fun restoreState(args: Bundle?)
        fun onContentClicked(content: Content)
        suspend fun requestRead(content: Content): String
        fun onSyncClicked()
        fun onSaveClicked(content: Content, body: String)
        fun onSaveClickedAsync(content: Content): Deferred<Boolean>
        fun onNextClicked()
        fun onPreviousClicked()
    }
    interface View: BaseView<Presenter> {
        fun displayState(state: State)
        fun displayIndex(state: State)
        fun displayDetails()
    }
    interface Dialog: BaseView<Presenter> { }
    class State {
        companion object {
            @JvmStatic fun Mutate(state: State): Mutator = Mutator(state)
            const val ARG_CONTEXT: String = "com.industrialbadger.content.CONTEXT"
            const val ARG_QUERY: String = "com.industrialbadger.content.QUERY"
            const val ARG_CONTENT_LIST: String = "com.industrialbadger.content.LIST"
            const val ARG_VOLUME_LIST: String = "com.industrialbadger.content.volume.LIST"
            const val ARG_TOKEN: String = "com.industrialbadger.content.volume.TOKEN"
            const val ARG_VOLUME_CONTENT: String = "com.industrialbadger.content.current.LIST"
            const val ARG_INDEX: String = "com.industrialbadger.content.current.INDEX"
        }
        val values: Bundle = Bundle()
        fun restoreState(savedInstanceState: Bundle?) = values.putAll(savedInstanceState)
        fun getContext(): Ctx = values.getSerializable(ARG_CONTEXT) as Ctx? ?: Ctx.INDEX
        fun getQuery(): String? = values.getString(ARG_QUERY)
        fun getContentList(): ArrayList<Content> = values.getParcelableArrayList(ARG_CONTENT_LIST) ?: ArrayList()
        fun getVolumeList(): ArrayList<Volume> = values.getParcelableArrayList(ARG_VOLUME_LIST) ?: ArrayList()
        fun getCurrentToken(): String = values.getString(ARG_TOKEN, "")
        fun getCurrentContent(): ArrayList<Content> = values.getParcelableArrayList(ARG_VOLUME_CONTENT) ?: ArrayList()
        fun getCurrentIndex(): Int = values.getInt(ARG_INDEX, 0)
        private fun mutate(newState: Bundle, clearCurrent: Boolean): State {
            with (values) {
                if (clearCurrent) {
                    remove(ARG_TOKEN)
                    remove(ARG_VOLUME_CONTENT)
                    remove(ARG_INDEX)
                }
                putAll(newState)
            }
            return this@State
        }
        class Mutator(private val state: State) {
            private val update: Bundle = Bundle()
            private var clear: Boolean = false

            fun setContext(ctx: Ctx): Mutator {
                update.putSerializable(ARG_CONTEXT, ctx)
                return this@Mutator
            }
            fun setQuery(query: String): Mutator {
                update.putString(ARG_QUERY, query)
                return this@Mutator
            }
            fun setContentList(content: ArrayList<Content>): Mutator {
                update.putParcelableArrayList(ARG_CONTENT_LIST, content)
                return this@Mutator
            }
            fun setVolumeList(volumes: ArrayList<Volume>): Mutator {
                update.putParcelableArrayList(ARG_VOLUME_LIST, volumes)
                return this@Mutator
            }
            fun setCurrentToken(token: String): Mutator {
                update.putString(ARG_TOKEN, token)
                return this@Mutator
            }
            fun setCurrentContent(content: ArrayList<Content>): Mutator {
                update.putParcelableArrayList(ARG_VOLUME_CONTENT, content)
                return this@Mutator
            }
            fun setCurrentIndex(index: Int): Mutator {
                update.putInt(ARG_INDEX, index)
                return this@Mutator
            }
            fun clearCurrent(): Mutator {
                clear = true
                return this@Mutator
            }
            /**
             * Updates the [State] all at once by calling [mutate].
             * This will return the reference to the full state
             */
            fun commit(): State = state.mutate(update, clear)
        }
    }
}