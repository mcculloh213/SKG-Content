package ktx.sovereign.content.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ktx.sovereign.content.contract.ContentContract

abstract class ContentPresenterFragment : Fragment(), ContentContract.View {
    protected var delegate: ContentContract.Delegate? = null
    protected var presenter: ContentContract.Presenter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ContentContract.Delegate) {
            delegate = context
        } else {
            throw RuntimeException("$context must implement ContentContract.Delegate")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter?.onViewCreated()
    }
    override fun displayState(state: ContentContract.State) { }
    override fun displayIndex(state: ContentContract.State) { }
    override fun displayDetails() { }
    override fun registerPresenter(presenter: ContentContract.Presenter) {
        this@ContentPresenterFragment.presenter = presenter
    }
}