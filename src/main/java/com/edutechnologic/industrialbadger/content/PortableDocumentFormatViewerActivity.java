package com.edutechnologic.industrialbadger.content;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.edutechnologic.industrialbadger.base.viewmodel.PortableDocumentFormatViewerViewModel;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.util.Locale;

public class PortableDocumentFormatViewerActivity extends AbstractViewerActivity implements
        OnLoadCompleteListener,
        OnPageChangeListener,
        OnRenderListener {
    private static final String TAG = PortableDocumentFormatViewerActivity.class.getSimpleName();

    private ViewGroup mActivityRoot;
    private PortableDocumentFormatViewerViewModel mViewModel;

    //region View Getters
    private TextView getCurrentPage() {
        return mActivityRoot.findViewById(R.id.page_index);
    }

    private TextView getPageCount() {
        return mActivityRoot.findViewById(R.id.page_count);
    }

    private PDFView getPdfView() {
        return mActivityRoot.findViewById(R.id.pdf_view);
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portable_document_format_viewer);

        mActivityRoot = findViewById(R.id.activity_root);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        initializeViewModel();
        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        // Log.d(TAG, "onBackPressed");
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_slide_to_bottom);
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     *
     * <p>If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * {@link #getSupportParentActivityIntent()} for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method {@link #onPrepareSupportNavigateUpTaskStack(TaskStackBuilder)}
     * to supply those arguments.</p>
     *
     * <p>See <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and
     * Back Stack</a> from the developer guide and
     * <a href="{@docRoot}design/patterns/navigation.html">Navigation</a> from the design guide
     * for more information about navigating within your app.</p>
     *
     * <p>See the {@link TaskStackBuilder} class and the Activity methods
     * {@link #getSupportParentActivityIntent()}, {@link #supportShouldUpRecreateTask(Intent)}, and
     * {@link #supportNavigateUpTo(Intent)} for help implementing custom Up navigation.</p>
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_slide_to_bottom);
        finish();
        return true;
    }

    //region Private Methods
    private void initializeViewModel() {
        // Log.d(TAG, "initializeViewModel");
        mViewModel = ViewModelProviders.of(this).get(PortableDocumentFormatViewerViewModel.class);
        mViewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String title) {
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null)
                    actionBar.setTitle(title);
                else
                    setTitle(title);
            }
        });
        mViewModel.getUri().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                if (uri == null) return;
                openPdf(uri);
            }
        });
        mViewModel.getPageIndex().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer index) {
                if (index == null) return;
                getCurrentPage().setText(String.format("Page %s", index + 1));
            }
        });
        mViewModel.getPageCount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer count) {
                if (count == null) return;
                getPageCount().setText(String.format("%s", count));
            }
        });
    }

    private void handleIntent(@NonNull Intent intent) {
        // Log.d(TAG, "handleIntent");
        mViewModel.setUri(intent.getData());
        mViewModel.setTitle(intent.getStringExtra(ARG_FILENAME));
        mViewModel.setAbsolutePath(intent.getStringExtra(ARG_ABSOLUTE_PATH));
    }

    private void openPdf(Uri uri) {
        // Log.d(TAG, "openPdf");

        mViewModel.setPageIndex(0);
        getPdfView().fromUri(uri)
                .autoSpacing(true)
                .defaultPage(0)
                .enableSwipe(true)
                .onLoad(this)
                .onPageChange(this)
                .onRender(this)
                .pageFling(true)
                .pageFitPolicy(FitPolicy.HEIGHT)
                .pageSnap(true)
                .swipeHorizontal(true)
                .load();
    }
    //endregion

    //region PDF View OnLoadCompleteListener
    /**
     * Called when the PDF is loaded
     *
     * @param nbPages the number of pages in this PDF file
     */
    @Override
    public void loadComplete(int nbPages) {
        // Log.d(TAG, "loadComplete");
        mViewModel.setPageCount(nbPages);
    }
    //endregion

    //region PDF View OnPageChangeListener
    /**
     * Called when the user use swipe to change page
     *
     * @param page      the new page displayed, starting from 0
     * @param pageCount the total page count
     */
    @Override
    public void onPageChanged(int page, int pageCount) {
        // Log.d(TAG, "onPageChanged");
        mViewModel.setPageIndex(page);
    }
    //endregion

    //region PDF View OnRenderListener
    /**
     * Called only once, when document is rendered
     *
     * @param nbPages    number of pages
     */
    @Override
    public void onInitiallyRendered(int nbPages) {
        // Log.d(TAG, "onInitiallyRendered");
        getPdfView().jumpTo(mViewModel.getRawPageIndex());
    }
    //endregion
}
