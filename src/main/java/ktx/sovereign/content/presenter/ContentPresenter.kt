package ktx.sovereign.content.presenter

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import kotlinx.coroutines.*
import ktx.sovereign.api.S3Client
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.repository.ContentRepository
import ktx.sovereign.database.repository.VolumeRepository
import ktx.sovereign.extension.html
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

class ContentPresenter (
    private val activity: FragmentActivity,
    provider: ContentContract.DependencyInjection
) : ContentContract.Presenter, CoroutineScope {
    private val job: Job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val _content: ContentRepository = provider.injectContentRepository()
    private val _volumes: VolumeRepository = provider.injectVolumeRepository()
    //TODO: Move away from demo AWS-S3 Client
    private val _aws: S3Client = S3Client.create()
    private val state: ContentContract.State = ContentContract.State()
    private var view: ContentContract.View? = null

    init {
        launch {
            val volumes = _volumes.getVolumes()
            launch(Dispatchers.IO) {
                val content = _content.getContent()
                ContentContract.State.Mutate(state)
                    .setVolumeList(ArrayList(volumes))
                    .setContentList(ArrayList(content))
                    .commit()
            }
            _content.index(volumes)
            _content.content.observe(activity, Observer<List<Content>> {
                val mutator = ContentContract.State.Mutate(state)
                    .setContentList(ArrayList(it))
                this@ContentPresenter.view?.displayIndex(mutator.commit())
            })
        }
    }

    override fun registerView(view: ContentContract.View) {
        this@ContentPresenter.view = view
    }
    override fun onViewCreated() {
        view?.displayState(state)
    }
    override fun saveState(): Bundle = state.values
    override fun restoreState(args: Bundle?) {
        state.restoreState(args)
        view?.displayState(state)
    }
    override fun onContentClicked(content: Content) {
        launch {
            val list = _content.getAllVolumeContent(content.token)
            ContentContract.State.Mutate(state)
                .setContext(ContentContract.Ctx.DETAILS)
                .setCurrentToken(content.token)
                .setCurrentContent(ArrayList(list))
                .setCurrentIndex(list.indexOf(content))
                .commit()
            view?.displayDetails()
        }
    }

    override suspend fun requestRead(content: Content): String = withContext(coroutineContext) {
        if (content.path.isEmpty()) {
            val volume = _volumes.getVolume(content.token)
                ?: return@withContext "<h1>404: No Associated Volume</h1>"
            try {
                val resp = _aws.getFileAsync("edutechnologic.optometry", volume.token, S3Client.sanitize(content.title))
                if (resp.isSuccessful) {
                    resp.body()?.string() ?: "<h1>201: No Content</h1>"
                } else {
                    resp.errorBody()?.string() ?: "<h1>${resp.code()}: ${resp.message()}</h1>"
                }
            } catch(ex: UnknownHostException) {
                html {
                    head {
                        title {+"No Network Connection"}
                    }
                    body {
                        h1 {+"No Network Detected"}
                        p {
                            +"You are currently not connected to the internet, and do not have a "
                            +"local version on the file!"
                        }
                        p {
                            +"If you would like to be able to view this file when offline, click "
                            +"on the save button in the toolbar next time you're online!"
                        }
                    }
                }.toString()
            } catch (ex: Exception) {
                """
                    <h1>${ex::class.simpleName}</h1>
                    <p>${ex.message}</p>
                """.trimIndent()
            }
        } else {
            _content.readFile(activity, content)
        }
    }
    override fun onSyncClicked() {
        launch(Dispatchers.IO) { _content.index(_volumes.getVolumes()) }
    }
    override fun onSaveClicked(content: Content, body: String) { }
    override fun onSaveClickedAsync(content: Content): Deferred<Boolean> {
        return async {
            val volume = _volumes.getVolume(content.token)
            if (volume != null) {
                _content.save(activity, content, volume.token)
            } else {
                false
            }
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