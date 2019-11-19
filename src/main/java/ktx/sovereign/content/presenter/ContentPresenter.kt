package ktx.sovereign.content.presenter

import android.os.Bundle
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import ktx.sovereign.content.ContentModuleState
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

class ContentPresenter(
    provider: ContentContract.DependencyInjection
) : ContentContract.Presenter, CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val lifecycleOwnerRef = WeakReference(provider.provideLifecycleOwner())
    private val _viewmodel = provider.provideViewModel()
    private val state: ContentContract.State = ContentContract.State()
    private var view: ContentContract.View? = null

    init {
        _viewmodel.state.observe(provider.provideLifecycleOwner(), Observer { state ->
            when (state) {
                is ContentModuleState.Index -> {
                    view?.displayState(ContentContract.State.Mutate(this@ContentPresenter.state)
                        .setVolumeList(ArrayList(state.volumes))
                        .setContentList(ArrayList(state.content))
                        .commit())
                }
                is ContentModuleState.Details -> {
                    view?.displayState(ContentContract.State.Mutate(this@ContentPresenter.state)
                        .setCurrentContent(ArrayList(state.content))
                        .setCurrentIndex(state.index)
                        .commit())
                }
            }
        })
    }

    override fun registerView(view: ContentContract.View) {
        this@ContentPresenter.view = view
    }
    override fun onViewCreated() {
        when (state.getContext()) {
            ContentContract.Ctx.INDEX -> lifecycleOwnerRef.get()?.let { _viewmodel.onIndex(it) } ?: Unit
            ContentContract.Ctx.DETAILS -> lifecycleOwnerRef.get()?.let { owner ->
                state.getInitialContent()?.let { _viewmodel.onDetails(owner, it) }
            } ?: Unit
            ContentContract.Ctx.TRAINING -> Unit
            ContentContract.Ctx.SEARCH -> Unit
            ContentContract.Ctx.ERROR -> Unit
        }
    }
    override fun saveState(): Bundle = state.values
    override fun restoreState(args: Bundle?) {
        state.restoreState(args)
        view?.displayState(state)
    }
    override fun onQueryChanged(query: String?) {
        view?.displayState(
            ContentContract.State.Mutate(state)
                .setQuery(query)
                .commit()
        )
    }
    override fun onContentClicked(volume: Volume, content: Content) {
        ContentContract.State.Mutate(state)
            .setContext(ContentContract.Ctx.DETAILS)
            .setInitialContent(content)
            .commit()
        view?.displayDetails()
    }

    override suspend fun requestRead(content: Content): String = withContext(coroutineContext) {
        _viewmodel.readContent(content.token, content.title).await()
        //        if (content.path.isEmpty()) {
//            val volume = _volumes.getVolume(content.token)
//                    ?: return@withContext "<h1>404: No Associated Volume</h1>"
//            try {
//                val resp = _aws.getFileAsync("edutechnologic.motoman", volume.token, S3Client.sanitize(content.title))
//                if (resp.isSuccessful) {
//                    resp.body()?.string() ?: "<h1>201: No Content</h1>"
//                } else {
//                    resp.errorBody()?.string() ?: "<h1>${resp.code()}: ${resp.message()}</h1>"
//                }
//            } catch(ex: UnknownHostException) {
//                html {
//                    head {
//                        title {+"No Network Connection"}
//                    }
//                    body {
//                        h1 {+"No Network Detected"}
//                        p {
//                            +"You are currently not connected to the internet, and do not have a "
//                            +"local version on the file!"
//                        }
//                        p {
//                            +"If you would like to be able to view this file when offline, click "
//                            +"on the save button in the toolbar next time you're online!"
//                        }
//                    }
//                }.toString()
//            } catch (ex: Exception) {
//                """
//                    <h1>${ex::class.simpleName}</h1>
//                    <p>${ex.message}</p>
//                """.trimIndent()
//            }
//        } else {
//            _content.readFile(content)
//        }
    }
    override fun onSyncClicked() {

    }
    override fun onSaveClicked(content: Content, body: String) { }
    override fun onSaveClickedAsync(content: Content): Deferred<Boolean> {
        return async {
            //            val volume = _volumes.getVolume(content.token)
//            if (volume != null) {
//                _content.save(content, volume.token)
//            } else {
//                false
//            }
            false
        }
    }
    override fun onNextClicked() {
    }
    override fun onPreviousClicked() {
    }
    override fun onDestroy() {
        job.cancelChildren()
    }
}