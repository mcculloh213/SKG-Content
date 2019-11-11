package com.edutechnologic.industrialbadger.content.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.edutechnologic.industrialbadger.base.fragment.BaseFragment;
import com.edutechnologic.industrialbadger.base.viewmodel.ContentViewModel;
import com.edutechnologic.industrialbadger.content.R;
import com.edutechnologic.industrialbadger.content.fragment.adapter.ContentListAdapter;

import java.util.ArrayList;
import java.util.List;

import ktx.sovereign.database.entity.Content;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentListFragment extends BaseFragment {
    public static final String NAME = "fragment:ContentList";
    private static final String TAG = ContentListFragment.class.getSimpleName();

    public static final String ARG_HAS_OPTIONS_MENU = "com.industrialbadger.content.list#ARG_HAS_OPTIONS_MENU";
    public static final String ARG_CONTENT_LIST = "com.industrialbadger.content.list#ARG_CONTENT_LIST";
    public static final String ARG_VIEWER_LAYOUT = "com.industrialbadger.content.list#ARG_VIEWER_LAYOUT";

    private boolean mHasOptionsMenu = false;
    private final ArrayList<Content> mContent = new ArrayList<>();
    private @LayoutRes int mViewerLayout = R.layout.fragment_content_list;

    private View mContentListViewRoot;
    private ContentListAdapter mAdapter;

    private ContentViewModel mViewModel;
    private OnFragmentInteractionListener mListener;

    private RecyclerView getContentRecyclerView() {
        return mContentListViewRoot.findViewById(R.id.content_recycler_view);
    }

    public ContentListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentListFragment.
     */
    public static ContentListFragment newInstance() {
        return new ContentListFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentListFragment.
     */
    public static ContentListFragment newInstance(boolean hasOptionsMenu) {
        ContentListFragment fragment = new ContentListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_OPTIONS_MENU, hasOptionsMenu);
        fragment.setArguments(args);
        return fragment;
    }

    //region Content List Fragment -- Fragment Lifecycle Methods
    /**
     * Called when a fragment is first attached to its context. {@link Fragment#onCreate(Bundle)}
     * will be called after this.
     *
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        // Log.d(TAG, "onAttach");
        super.onAttach(context);
        Bundle args = getArguments();

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        if (args != null) onHandleArguments(args);
        mAdapter = new ContentListAdapter(context);
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
        setHasOptionsMenu(mHasOptionsMenu);
    }

    /**
     * Called to do initial creation of a fragment. This is called after
     * {@link Fragment#onAttach(Context)} and before
     * {@link Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     *
     * Note that this can be called while the fragment's activity is still in the process of being
     * created. As such, you can not rely on things like the activity's content view hierarchy
     * being initialized at this point. If you want to do work once the activity itself is
     * created, see {@link Fragment#onActivityCreated(Bundle)}.
     *
     * Any restored child fragments will be created before the base
     * {@link Fragment#onCreate(Bundle)} method returns.
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreateViewModel() {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreateViewModel();
        mViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel.class);
        mViewModel.getContent().observe(ContentListFragment.this, new Observer<List<Content>>() {
            @Override
            public void onChanged(List<Content> content) {
                onReceiveViewModelData(content);
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
        mViewerLayout = state.getInt(ARG_VIEWER_LAYOUT, R.layout.fragment_content_list);
        if (state.containsKey(ARG_CONTENT_LIST)) {
            List<Content> content = state.getParcelableArrayList(ARG_CONTENT_LIST);
            if (content != null) {
                mContent.clear();
                mContent.addAll(content);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mContentListViewRoot = inflater.inflate(
                R.layout.fragment_content_list, container, false
        );

        mBaseListener.setTitle("Content");

        return mContentListViewRoot;
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
        int size = menu.size();
        inflater.inflate(R.menu.menu_content_list, menu);
        for (int i = 0; i < menu.size() - size; i++)
            onTintMenuItem(menu.getItem(i), android.R.attr.textColorPrimary);
        mListener.onPrepareSearchManager(this, (SearchView)menu.findItem(R.id.menu_search).getActionView());
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
        onPrepareRecyclerView();
    }

    private void onPrepareRecyclerView() {
        // Log.d(TAG, "onPrepareRecyclerView");
        getContentRecyclerView().setAdapter(mAdapter);
        mAdapter.setAdapterContent(mContent);
    }

    public void onReceiveViewModelData(List<Content> content) {
        // Log.d(TAG, "onReceiveViewModelData");
        mContent.clear();
        mContent.addAll(content);
        mAdapter.setAdapterContent(mContent);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Log.d(TAG, "onOptionsItemSelected");
        if (item.getItemId() == R.id.menu_sync) {
            mListener.onSyncContent();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
        outState.putInt(ARG_VIEWER_LAYOUT, mViewerLayout);
        outState.putParcelableArrayList(ARG_CONTENT_LIST, mContent);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAdapter = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener extends
            ContentListAdapter.OnAdapterInteractionListener {
        void onPrepareSearchManager(Fragment fragment, SearchView view);
        void onSyncContent();
    }
}
