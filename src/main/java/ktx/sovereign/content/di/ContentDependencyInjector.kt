package ktx.sovereign.content.di

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProviders
import ktx.sovereign.content.contract.ContentContract
import ktx.sovereign.content.ui.content.ContentViewModel

class ContentDependencyInjector(
    private val activity: AppCompatActivity
) : ContentContract.DependencyInjection {
    private val _factory: ContentViewModel.Factory = ContentViewModel.Factory(activity)
    override fun provideLifecycleOwner(): LifecycleOwner = activity
    override fun provideViewModel(): ContentViewModel =
        ViewModelProviders.of(activity, _factory)[ContentViewModel::class.java]
}