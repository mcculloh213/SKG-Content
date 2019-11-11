package com.edutechnologic.industrialbadger.content;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.edutechnologic.industrialbadger.base.viewmodel.WebBrowserViewModel;
import com.edutechnologic.industrialbadger.base.widget.BrowserClient;


public class HyperTextMarkupLanguageViewerActivity extends AbstractViewerActivity implements
        BrowserClient.OnBrowserClientEventListener,
        SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = HyperTextMarkupLanguageViewerActivity.class.getSimpleName();

    private ViewGroup mActivityRoot;
    private WebBrowserViewModel mViewModel;

    //region View Getters
    private Button getCloseView() {
        return mActivityRoot.findViewById(R.id.close_view);
    }

    private TextView getWebViewTitle() {
        return mActivityRoot.findViewById(R.id.activity_title);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return mActivityRoot.findViewById(R.id.refresh_control);
    }

    private WebView getWebView() {
        return mActivityRoot.findViewById(R.id.webview);
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portable_document_format_viewer);

        mActivityRoot = findViewById(R.id.activity_root);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        initializeViewModel();
        registerUiListeners();

        handleIntent(getIntent());
    }

    @Override
    public void onBackPressed() {
        // Log.d(TAG, "onBackPressed");
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_no_animation, R.anim.activity_slide_to_bottom);
    }

    //region Private Methods
    private void initializeViewModel() {
        // Log.d(TAG, "initializeViewModel");
        mViewModel = ViewModelProviders.of(this).get(WebBrowserViewModel.class);
        mViewModel.isRefreshing().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean refreshing) {
                if (refreshing == null) return;
                getSwipeRefreshLayout().setRefreshing(refreshing);
            }
        });
        mViewModel.getTitle().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String title) {
                getWebViewTitle().setText(title);
            }
        });
        mViewModel.getUri().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String uri) {
                if (uri == null) return;
                navigateTo(uri);
            }
        });
    }

    private void registerUiListeners() {
        // Log.d(TAG, "registerUiListeners");
        getCloseView().setOnClickListener(new OnClickCloseViewListener());
        getSwipeRefreshLayout().setOnRefreshListener(this);
    }

    private void handleIntent(@NonNull Intent intent) {
        // Log.d(TAG, "handleIntent");
        mViewModel.setTitle(intent.getStringExtra(ARG_FILENAME));
        mViewModel.setUri(intent.getStringExtra(ARG_ABSOLUTE_PATH));
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void navigateTo(String uri) {
        // Log.d(TAG, "navigateTo");

        WebSettings settings = getWebView().getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        getWebView().setWebViewClient(new BrowserClient((BrowserClient.OnBrowserClientEventListener)this));
        getWebView().loadUrl(uri);
    }
    //endregion

    //region
    private class OnClickCloseViewListener implements View.OnClickListener {
        private final String TAG = OnClickCloseViewListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onCLick");
            finish();
        }
    }
    //endregion

    //region SwipeRefreshLayout.OnRefreshListener Implementation
    /**
     * Called when a swipe gesture triggers a refresh.
     */
    @Override
    public void onRefresh() {
        // Log.d(TAG, "onRefresh");

        mViewModel.setNavigating(true);
        mViewModel.setRefreshing(true);
        getWebView().loadUrl("javascript:window.location.reload(true)");
    }
    //endregion

    //region BrowserClient.OnBrowserClientEventListener Implementation
    @Override
    public void handleError(WebView view, WebResourceRequest request, WebResourceError error) {
        // Log.d(TAG, "handleError");
    }

    @Override
    public void handleExternalUriIntent(Intent intent) {
        // Log.d(TAG, "handleExternalUriIntent");
    }

    @Override
    public void handleHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        // Log.d(TAG, "handleHttpError");
    }

    @Override
    public void handlePageStarted(WebView view, String url, Bitmap favicon) {
        // Log.d(TAG, "handlePageStarted");
    }

    @Override
    public void handlePageFinished(WebView view, String url) {
        // Log.d(TAG, "handlePageFinished");

        mViewModel.setNavigating(false);
        mViewModel.setRefreshing(false);
    }
    //endregion
}
