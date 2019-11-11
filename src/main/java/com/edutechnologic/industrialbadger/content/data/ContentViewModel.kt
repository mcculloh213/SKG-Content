package com.edutechnologic.industrialbadger.content.data

import android.content.Context
import android.util.Log
import androidx.collection.LruCache
import androidx.lifecycle.*
import com.industrialbadger.api.client.S3Client
import ktx.sovereign.api.IntelligentMixedReality
import ktx.sovereign.api.data.ContentDataSource
import ktx.sovereign.api.model.ContentRestModel
import ktx.sovereign.api.model.RestResponse
import ktx.sovereign.database.ApplicationDatabase
import ktx.sovereign.database.entity.Content
import ktx.sovereign.database.entity.Volume
import ktx.sovereign.database.repository.ContentRepository
import ktx.sovereign.database.repository.VolumeRepository

private val OPTOMETRY_TOKEN: String = "5c509dcb-15fa-4ff4-899b-aef74e703453"
private val MOTOMAN_TOKEN: String = "f2cbe9b7-0fe4-491b-a96d-a9d6c7f8cbb5"
class ContentViewModel private constructor(
        private val contentRepo: ContentRepository,
        private val volumeRepo: VolumeRepository,
        private val webservice: ContentDataSource
) : ViewModel() {
    private val _state: MutableLiveData<ContentModuleState> = MutableLiveData()
    val state: LiveData<ContentModuleState> = _state

    private val index: MutableList<ContentRestModel.Volume> = mutableListOf()

    fun onIndex(owner: LifecycleOwner) {
        webservice.indexVolumes(MOTOMAN_TOKEN).observe(owner, Observer { response ->
            index.clear()
            val volumes = mutableListOf<Volume>()
            val content = mutableListOf<Content>()
            when (response) {
                is RestResponse.OK -> {
                    index.addAll(response.body)
                    response.body.forEach { volume ->
                        volumes.add(Volume.from(volume))
                        volume.pages.forEach { page ->
                            content.add(Content.from(page, volume.id))
                        }
                    }
                    _state.postValue(ContentModuleState.Index(
                            volumes = volumes,
                            content = content
                    ))
                }
                is RestResponse.NotFound -> {
                    _state.postValue(ContentModuleState.Error(
                            message = "No online content available."
                    ))
                }
            }

        })
    }
    fun onDetails(owner: LifecycleOwner, content: Content) {
        val list = mutableListOf<Content>()
        val volume = index.firstOrNull { it.id === content.token }?.also { volume ->
            list.addAll(volume.pages.map { Content.from(it, volume.id) })
        }

        Log.i("Content", "Content: ${content.title} Index: ${content.index}")

        if (volume != null) {
            _state.postValue(ContentModuleState.Details(
                    volume = Volume.from(volume),
                    content = list,
                    index = content.index
            ))
        } else {
            _state.postValue(ContentModuleState.Error(
                    message = "Failed to fetch Volume"
            ))
        }
    }
    fun readContent(volumeId: String, fileName: String) = webservice.fetchHtml(volumeId, fileName)

    class Factory(context: Context) : ViewModelProvider.Factory {
        private val _db: ApplicationDatabase = ApplicationDatabase.getDatabase(context)
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ContentViewModel::class.java)) {
                return ContentViewModel (
                        contentRepo = ContentRepository(_db.ContentDao()),
                        volumeRepo = VolumeRepository(_db.VolumeDao()),
                        webservice = ContentDataSource(
                                client = IntelligentMixedReality.Content,
                                aws = S3Client.create()
                        )
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel Class")
        }
    }
}