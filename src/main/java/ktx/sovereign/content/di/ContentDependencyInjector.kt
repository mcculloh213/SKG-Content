package ktx.sovereign.content.di

import android.content.Context
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.database.Database
import ktx.sovereign.database.repository.ContentRepository
import ktx.sovereign.database.repository.VolumeRepository

class ContentDependencyInjector(context: Context) : ContentContract.DependencyInjection {
    private val _db: Database = Database.getDatabase(context)
    override fun injectVolumeRepository(): VolumeRepository = VolumeRepository(_db.VolumeDao())
    override fun injectContentRepository(): ContentRepository = ContentRepository(_db.ContentDao())
}