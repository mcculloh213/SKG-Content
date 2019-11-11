package com.edutechnologic.industrialbadger.content.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.edutechnologic.industrialbadger.base.fragment.BaseFragment;
import com.edutechnologic.industrialbadger.base.util.StringUtil;
import com.edutechnologic.industrialbadger.base.widget.BrowserClient;
import com.edutechnologic.industrialbadger.content.R;
import com.edutechnologic.industrialbadger.content.task.ReadFileTask;
import com.edutechnologic.industrialbadger.base.util.ZoomLevel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ktx.sovereign.database.entity.Content;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContentViewerFragment extends BaseFragment implements
        BrowserClient.OnBrowserClientEventListener,
        ReadFileTask.OnCompletionListener {
    public  static final String NAME = "fragment:ContentViewer";
    private static final String TAG = ContentViewerFragment.class.getSimpleName();
    private static final String INVERT_COLORS ="javascript: ("
            +"function () { "

            +"var css = 'html {-webkit-filter: invert(100%);' +"
            +"    '-moz-filter: invert(100%);' + "
            +"    '-o-filter: invert(100%);' + "
            +"    '-ms-filter: invert(100%); }',"

            +"head = document.getElementsByTagName('head')[0],"
            +"style = document.createElement('style');"

            +"if (!window.counter) { window.counter = 1;} else  { window.counter ++;"
            +"if (window.counter % 2 == 0) { var css ='html {-webkit-filter: invert(0%); -moz-filter:    invert(0%); -o-filter: invert(0%); -ms-filter: invert(0%); }'}"
            +"};"

            +"style.type = 'text/css';"
            +"if (style.styleSheet){"
            +"style.styleSheet.cssText = css;"
            +"} else {"
            +"style.appendChild(document.createTextNode(css));"
            +"}"

            //injecting the css to the head
            +"head.appendChild(style);"
            +"}());";

    private final JavascriptAction mJavascriptAction = new JavascriptAction();
    private ReadFileTask mReadFileTask;

    public static final String ARG_CONTENT = "com.industrialbadger.content.viewer#ARG_CONTENT";
    public static final String ARG_TEXT = "com.industrialbadger.content.viewer#ARG_TEXT";
    public static final String ARG_IS_INVERTED = "com.industrialbadger.content.viewer#ARG_IS_INVERTED";
    public static final String ARG_TEXT_ZOOM = "com.industrialbadger.content.viewer#ARG_TEXT_ZOOM";


    private Content mContent;
    private String mText;
    private boolean mInverted = false;
    private ZoomLevel mTextZoom = ZoomLevel.ZERO;

    private View mFragmentRoot;

    public ContentViewerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentViewerFragment.
     */
    public static ContentViewerFragment newInstance(Content content) {
        ContentViewerFragment fragment = new ContentViewerFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }

    //region View Getters
    private WebView getWebView() {
        return mFragmentRoot.findViewById(R.id.webview);
    }

    private FloatingActionButton getPageUp() {
        return mFragmentRoot.findViewById(R.id.fab_page_up);
    }

    private FloatingActionButton getPageDown() {
        return mFragmentRoot.findViewById(R.id.fab_page_down);
    }
    //endregion

    @Override
    public void onAttach(@NonNull Context context) {
        // Log.d(TAG, "onAttach");
        super.onAttach(context);
        Bundle args = getArguments();
        if (args != null) onHandleArguments(args);
    }

    /**
     * Sets the {@link Fragment} parameters based on the supplied {@link Bundle} of arguments.
     *
     * @param args The {@code Bundle} arguments set when initializing the {@code Fragment}.
     */
    @Override
    protected void onHandleArguments(@NonNull Bundle args) {
        // Log.d(TAG, "onHandleArguments");
        super.onHandleArguments(args);
        mContent = args.getParcelable(ARG_CONTENT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    /**
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        // Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(state);
        mContent = state.getParcelable(ARG_CONTENT);
        mText = state.getString(ARG_TEXT);
        mInverted = state.getBoolean(ARG_IS_INVERTED, false);
        mTextZoom = ZoomLevel.fromInt(state.getInt(ARG_TEXT_ZOOM, ZoomLevel.ZERO.getValue()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mFragmentRoot = inflater.inflate(
                R.layout.fragment_content_viewer, container, false
        );

        return mFragmentRoot;
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     *
     * @param view               The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        onSetBrowserClient();
        onRegisterUiListeners();
    }

    private void onSetBrowserClient() {
        // Log.d(TAG, "onSetBrowserClient");
        WebView wv = getWebView();
        wv.setWebViewClient(new BrowserClient(this));
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * {@link #setRetainInstance(boolean)} to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after {@link #onCreateView}
     * and before {@link #onViewStateRestored(Bundle)}.
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
        if (mText == null || StringUtil.isNullOrEmpty(mText))
            onLoadFile();
        else
            onLoadWebView(mText);
    }

    public void onReceiveViewModelData(Content content) {
        // Log.d(TAG, "onReceiveViewModelData");
        mContent = content;
        if (mText == null || StringUtil.isNullOrEmpty(mText))
            onLoadFile();
        else
            onLoadWebView(mText);
    }

    public synchronized void onReceiveViewModelData(int zoom) {
        // Log.d(TAG, "onReceiveViewModelData");
        if (isAdded()) {
            mTextZoom = ZoomLevel.fromInt(zoom);
            WebView wv = getWebView();
            wv.getSettings().setTextZoom(zoom);
            wv.loadUrl(mJavascriptAction.jsGetViewportHeight);
        }
    }

    public synchronized void onReceiveViewModelData(boolean invert) {
        // Log.d(TAG, "onReceiveViewModelData");
        if (isAdded()) {
            mInverted = !mInverted;
            WebView wv = getWebView();
            wv.setBackgroundColor(requireContext()
                    .getColor(mInverted ? android.R.color.black : android.R.color.white));
            wv.loadUrl(INVERT_COLORS);
        }
    }

    private void onLoadFile() {
        // Log.d(TAG, "onLoadFile");
        if (mReadFileTask == null || mReadFileTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
            mReadFileTask = new ReadFileTask(requireContext(), this);
            mReadFileTask.execute(mContent);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void onLoadWebView(String contents) {
        // Log.d(TAG, "onLoadWebView");
        WebView wv = getWebView();
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(mJavascriptAction, "JavascriptAction");
        wv.loadData(contents, "text/html", "utf-8");
        wv.getSettings().setTextZoom(mTextZoom.getValue());
        mBaseListener.hideLoadingDialog();
    }

    /**
     *
     */
    @Override
    protected void onRegisterUiListeners() {
        // Log.d(TAG, "onRegisterUiListeners");
        super.onRegisterUiListeners();
        getPageUp().setOnClickListener(new OnClickPageUpListener());
        getPageDown().setOnClickListener(new OnClickPageDownListener());
    }

    private void onScrollToY(int y) {
        // Log.d(TAG, "onScrollToY");
        WebView wv = getWebView();
        wv.scrollTo(wv.getScrollX(), y);
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed in a new instance of its process is
     * restarted.  If a new instance of the fragment later needs to be
     * created, the data you place in the Bundle here will be available
     * in the Bundle given to {@link #onCreate(Bundle)},
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, and
     * {@link #onActivityCreated(Bundle)}.
     *
     * <p>This corresponds to {@link #onSaveInstanceState(Bundle)
     * Activity.onSaveInstanceState(Bundle)} and most of the discussion there
     * applies here as well.  Note however: <em>this method may be called
     * at any time before {@link #onDestroy()}</em>.  There are many situations
     * where a fragment may be mostly torn down (such as when placed on the
     * back stack with no UI showing), but its state will not be saved until
     * its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_CONTENT, mContent);
        outState.putString(ARG_TEXT, mText);
        outState.putBoolean(ARG_IS_INVERTED, mInverted);
        outState.putInt(ARG_TEXT_ZOOM, mTextZoom.getValue());
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    //region ContentDetailFragment -- UI Listeners
    private class OnClickPageUpListener implements View.OnClickListener {
        private final String TAG = OnClickPageUpListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            WebView view = getWebView();
            int page = (int) Math.floor((view.getScrollY() - view.getHeight()) * view.getScaleY())
                    + (int)Math.ceil(((float)view.getSettings().getTextZoom()) * 0.5f);
            onScrollToY(page < 0 ? 0 : page);
        }
    }

    private class OnClickPageDownListener implements View.OnClickListener {
        private final String TAG = OnClickPageDownListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            WebView view = getWebView();
            int page = (int) Math.floor((view.getScrollY() + view.getHeight()) * view.getScaleY())
                    - (int)Math.floor(((float)view.getSettings().getTextZoom()) * 0.5f);
            int max = (int) mJavascriptAction.getViewportHeight() - view.getHeight();
            onScrollToY(page > max ? max : page);
        }
    }
    //endregion

    //region ContentViewerFragment -- ReadFileTask.OnCompletionListener
    @Override
    public void onReadResults(String contents) {
        // Log.d(TAG, "onReadResults");
        mText = contents;
        onLoadWebView(mText);
        mReadFileTask = null;
    }
    //endregion

    //region ContentViewerFragment -- ActivityProgressBarListener
    /**
     * Displays the <em>determinate</em> {@link ProgressBar} at the top of the screen and sets
     * the text.
     *
     * @param message Message to display to the user.
     */
    @Override
    public void showProgressBar(String message) {
        mBaseListener.showProgressBar(message);
    }

    /**
     * Updates the <em>determinate</em> {@link ProgressBar}'s current progress.
     *
     * @param progress The current progress.
     */
    @Override
    public void updateProgress(int progress) {
        mBaseListener.updateProgress(progress);
    }

    /**
     * Updates the <em>determinate</em> {@link ProgressBar}'s current progress.
     *
     * @param progress The current progress.
     */
    @Override
    public void updateProgress(double progress) {
        mBaseListener.updateProgress(progress);
    }

    /**
     * Hides the <em>determinate</em> {@link ProgressBar} and resets values for next use.
     */
    @Override
    public void hideProgressBar() {
        mBaseListener.hideProgressBar();
    }

    /**
     * Displays the {@link ProgressBar} in the {@link AppCompatActivity}.
     */
    @Override
    public void showLoadingDialog() {
        // Log.d(TAG, "showLoadingDialog");
        mBaseListener.showLoadingDialog();
    }

    /**
     * Displays the {@link ProgressBar} in the {@link AppCompatActivity}.
     *
     * @param message The {@link String} message to display with the {@code ProgressBar}.
     */
    @Override
    public void showLoadingDialog(String message) {
        // Log.d(TAG, "showLoadingDialog");
        mBaseListener.showLoadingDialog(message);
    }

    /**
     * Displays the {@link ProgressBar} in the {@link AppCompatActivity}.
     *
     * @param stringResId The {@code String} Resource ID to display with the {@code ProgressBar}.
     */
    @Override
    public void showLoadingDialog(int stringResId) {
        // Log.d(TAG, "showLoadingDialog");
        mBaseListener.showLoadingDialog(stringResId);
    }

    /**
     * Hides the {@link ProgressBar} in the {@link AppCompatActivity}.
     */
    @Override
    public void hideLoadingDialog() {
        // Log.d(TAG, "hideLoadingDialog");
        mBaseListener.hideLoadingDialog();
    }

    //region Content Detail Fragment -- Browser Client OnBrowserClientEventListener
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
        view.loadUrl(mJavascriptAction.jsGetViewportHeight);

        view.setBackgroundColor(requireContext()
                .getColor(mInverted ? android.R.color.black : android.R.color.white));
        if (mInverted) view.loadUrl(INVERT_COLORS);
    }
    //endregion

    //region JavaScript Actions
    /**
     * Crash Course:
     *   https://www.lewuathe.com/android/javascript/use-javascriptinterface-on-android.html
     */
    private class JavascriptAction {

        private float mViewportHeight;

        final String jsGetViewportHeight = "javascript:JavascriptAction.calculateViewportHeight(document.body.scrollHeight)";

        float getViewportHeight() {
            return mViewportHeight;
        }

        @JavascriptInterface
        public void calculateViewportHeight(final float height) {
            mViewportHeight = (height * getResources().getDisplayMetrics().density);
        }
    }
    //endregion
}
