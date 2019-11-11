package com.edutechnologic.industrialbadger.content.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edutechnologic.industrialbadger.base.fragment.BaseFragment;
import com.edutechnologic.industrialbadger.base.viewmodel.ContentViewModel;
import com.edutechnologic.industrialbadger.content.R;
import com.edutechnologic.industrialbadger.content.fragment.adapter.ContentDetailPagerAdapter;
import com.edutechnologic.industrialbadger.base.util.ZoomLevel;
import com.edutechnologic.industrialbadger.base.widget.ZoomLevelView;

import java.util.ArrayList;
import java.util.List;

import ktx.sovereign.database.entity.Content;

/**
 *
 */
public class ContentDetailFragment extends BaseFragment implements
        ZoomLevelView.OnClickSlotListener,
        ViewPager.OnPageChangeListener {
    public static final String NAME = "fragment:ContentDetail";
    private static final String TAG = ContentDetailFragment.class.getSimpleName();

    public static final String ARG_HAS_OPTIONS_MENU = "com.industrialbadger.content.detail#ARG_HAS_OPTIONS_MENU";
    public static final String ARG_CONTENT_LIST = "com.industrialbadger.content.detail#ARG_CONTENT_LIST";
    public static final String ARG_CURRENT_INDEX = "com.industrialbadger.content.detail#ARG_CURRENT_INDEX";
    public static final String ARG_ZOOM_LEVEL = "com.industrialbadger.content.detail#ARG_ZOOM_LEVEL";
    public static final String ARG_IS_INVERTED = "com.industrialbadger.content.detail#ARG_IS_INVERTED";
    public static final String ARG_VIEWER_LAYOUT = "com.industrialbadger.content.detail#ARG_VIEWER_LAYOUT";

    private boolean mHasOptionsMenu = false;
    private final ArrayList<Content> mContentList = new ArrayList<>();
    private int mCurrentIndex = 0;
    private ZoomLevel mZoomLevel = ZoomLevel.ZERO;
    private boolean mIsInverted = false;
    @LayoutRes private int mViewerLayout = R.layout.fragment_content_detail;

    private ContentViewModel mViewModel;
    private View mContentDetailViewRoot;
    private ContentDetailPagerAdapter mAdapter;

    private ViewPager getViewPager() {
        return mContentDetailViewRoot.findViewById(R.id.content_view_pager);
    }

    private ZoomLevelView getZoomLevelView() {
        return mContentDetailViewRoot.findViewById(R.id.zoom_level_indicator);
    }

    private View getPageLeft() {
        return mContentDetailViewRoot.findViewById(R.id.control_page_left);
    }

    private View getPageRight() {
        return mContentDetailViewRoot.findViewById(R.id.control_page_right);
    }

    public ContentDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentDetailFragment.
     */
    public static ContentDetailFragment newInstance(ArrayList<Content> content, int index) {
        ContentDetailFragment fragment = new ContentDetailFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CONTENT_LIST, content);
        args.putInt(ARG_CURRENT_INDEX, index);
        fragment.setArguments(args);
        return fragment;
    }

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
        mHasOptionsMenu = args.getBoolean(ARG_HAS_OPTIONS_MENU, false);
        mCurrentIndex = args.getInt(ARG_CURRENT_INDEX, 0);
        mViewerLayout = args.getInt(ARG_VIEWER_LAYOUT, R.layout.fragment_content_detail);
        if (args.containsKey(ARG_CONTENT_LIST)) {
            List<Content> content = args.getParcelableArrayList(ARG_CONTENT_LIST);
            if (content != null) {
                mContentList.clear();
                mContentList.addAll(content);
            }
        }
        setHasOptionsMenu(mHasOptionsMenu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreateViewModel() {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreateViewModel();
        mViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel.class);
        mViewModel.isMenuEnabled().observe(ContentDetailFragment.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enabled) {
                if (enabled == null) enabled = false;
                mViewModel.setMenuOpen(enabled);
            }
        });
        mViewModel.getZoomLevel().observe(ContentDetailFragment.this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer zoom) {
                if (zoom == null) zoom = 100;
                mViewModel.setCurrentZoom(zoom);
                onReceiveViewModelData(zoom);
            }
        });
        mViewModel.isInvertEnabled().observe(ContentDetailFragment.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enabled) {
                if (enabled == null) enabled = false;
                mViewModel.setInverted(enabled);
                onReceiveViewModelData(enabled);
            }
        });
    }

    /**
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        // Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(state);
        mHasOptionsMenu = state.getBoolean(ARG_HAS_OPTIONS_MENU, false);
        mCurrentIndex = state.getInt(ARG_CURRENT_INDEX, 0);
        mZoomLevel = ZoomLevel.fromInt(state.getInt(ARG_ZOOM_LEVEL, 100));
        mIsInverted = state.getBoolean(ARG_IS_INVERTED, false);
        mViewerLayout = state.getInt(ARG_VIEWER_LAYOUT, R.layout.fragment_content_list);
        if (state.containsKey(ARG_CONTENT_LIST)) {
            List<Content> content = state.getParcelableArrayList(ARG_CONTENT_LIST);
            if (content != null) {
                mContentList.clear();
                mContentList.addAll(content);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContentDetailViewRoot = inflater.inflate(
                R.layout.fragment_content_detail, container, false
        );

        return mContentDetailViewRoot;
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        // Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
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
        if (mContentDetailViewRoot == null) mContentDetailViewRoot = getView();
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
        // Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        if (mAdapter == null) onPrepareViewPager();
        getZoomLevelView().setOnClickSlotListener(this);
    }

    private synchronized void onPrepareViewPager() {
        // Log.d(TAG, "onPrepareViewPager");
        if (isAdded() && mAdapter == null) {
            mAdapter = new ContentDetailPagerAdapter(getChildFragmentManager());
            mAdapter.onReceiveData(mContentList);
            getViewPager().setAdapter(mAdapter);
            getViewPager().addOnPageChangeListener(this);
            getViewPager().setCurrentItem(mCurrentIndex);
            mBaseListener.setTitle(mContentList.get(mCurrentIndex).getTitle());
        }
    }

    public void onReceiveViewModelData(List<Content> content) {
        // Log.d(TAG, "onReceiveViewModelData");
        if (mAdapter == null) onPrepareViewPager();
        mContentList.clear();
        mContentList.addAll(content);
        mAdapter.onReceiveData(mContentList);
    }

    public void onReceiveViewModelData(int zoom) {
        // Log.d(TAG, "onReceiveViewModelData");
        if (mAdapter == null) onPrepareViewPager();
        mZoomLevel = ZoomLevel.fromInt(zoom);
        getZoomLevelView().setCurrentZoomLevel(mZoomLevel);
        ContentViewerFragment fragment = mAdapter.getRegisteredFragment(getViewPager().getCurrentItem());
        if (fragment != null && fragment.isAdded()) fragment.onReceiveViewModelData(zoom);
    }

    public void onReceiveViewModelData(boolean invert) {
        // Log.d(TAG, "onReceiveViewModelData");
        if (mAdapter == null) onPrepareViewPager();
        mIsInverted = invert;
        ContentViewerFragment fragment = mAdapter.getRegisteredFragment(getViewPager().getCurrentItem());
        if (fragment != null && fragment.isAdded()) fragment.onReceiveViewModelData(mIsInverted);
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
     * <p>This corresponds to {@code Activity#onSaveInstanceState(Bundle)
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
        outState.putBoolean(ARG_HAS_OPTIONS_MENU, mHasOptionsMenu);
        outState.putInt(ARG_CURRENT_INDEX, mCurrentIndex);
        outState.putInt(ARG_ZOOM_LEVEL, mZoomLevel.getValue());
        outState.putBoolean(ARG_IS_INVERTED, mIsInverted);
        outState.putInt(ARG_VIEWER_LAYOUT, mViewerLayout);
        outState.putParcelableArrayList(ARG_CONTENT_LIST, mContentList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAdapter = null;
    }

    //region ContentDetailFragment -- UI Listeners
    private class OnClickPageLeftListener implements View.OnClickListener {
        private final String TAG = OnClickPageLeftListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
        }
    }

    private class OnClickPageRightListener implements View.OnClickListener {
        private final String TAG = OnClickPageRightListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
        }
    }
    //endregion

    //region ContentDetailFragment -- ViewPager.OnPageChangedListener
    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position             Position index of the first page currently being displayed.
     *                             Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // Log.d(TAG, "onPageScrolled");
    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
        // Log.d(TAG, "onPageSelected");
        ContentViewerFragment fragment = mAdapter.getRegisteredFragment(position);
        mBaseListener.setTitle(mContentList.get(position).getTitle());
        if (fragment != null && fragment.isAdded()) {
            fragment.onReceiveViewModelData(mZoomLevel.getValue());
//            fragment.onReceiveViewModelData(mIsInverted);
            fragment.onReceiveViewModelData(mContentList.get(position));
        }
    }

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPager#SCROLL_STATE_IDLE
     * @see ViewPager#SCROLL_STATE_DRAGGING
     * @see ViewPager#SCROLL_STATE_SETTLING
     */
    @Override
    public void onPageScrollStateChanged(int state) {
        // Log.d(TAG, "onPageScrollStateChanged");
    }
    //endregion

    @Override
    public void onClickSlot(int value) {
        // Log.d(TAG, "onClickSlot");
        int level = ZoomLevel.ZERO.getValue();
        switch (value) {
            case 1:
                level = ZoomLevel.ONE.getValue();
                break;
            case 2:
                level = ZoomLevel.TWO.getValue();
                break;
            case 3:
                level = ZoomLevel.THREE.getValue();
                break;
            case 4:
                level = ZoomLevel.FOUR.getValue();
                break;
            case 5:
                level = ZoomLevel.FIVE.getValue();
                break;
            case 6:
                level = ZoomLevel.SIX.getValue();
                break;
            case 7:
                level = ZoomLevel.SEVEN.getValue();
                break;
            case 8:
                level = ZoomLevel.EIGHT.getValue();
                break;
            case 9:
                level = ZoomLevel.NINE.getValue();
                break;
            case 10:
                level = ZoomLevel.TEN.getValue();
                break;
            default:
                break;
        }
        mViewModel.setZoomLevel(level);
    }
}
