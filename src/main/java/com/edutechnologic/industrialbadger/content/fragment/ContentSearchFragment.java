package com.edutechnologic.industrialbadger.content.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

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
import android.view.View;
import android.view.ViewGroup;

import com.edutechnologic.industrialbadger.base.fragment.RecyclerListFragment;
import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
import com.edutechnologic.industrialbadger.base.viewmodel.ContentViewModel;
import com.edutechnologic.industrialbadger.base.widget.ListAdapter;
import com.edutechnologic.industrialbadger.content.R;

import java.util.List;

import ktx.sovereign.database.entity.Content;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentSearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentSearchFragment extends RecyclerListFragment<Content> {
    public static final String NAME = "fragment:ContentSearch";
    private static final String TAG = ContentSearchFragment.class.getSimpleName();
    private static final String MODAL_BODY_FORMATTABLE = "" +
            "No results were found for \"%s\" within your local content. " +
            "Please try refreshing your content and try again.";

    public static final String ARG_QUERY = "com.industrialbadger.content.search#ARG_QUERY";

    private String mContentQuery;

    private ContentViewModel mViewModel;
    private OnFragmentInteractionListener mListener;

    public ContentSearchFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param query
     * @param hasOptionsMenu
     * @return A new instance of fragment ContentSearchFragment.
     */
    public static ContentSearchFragment newInstance(@NonNull String query, boolean hasOptionsMenu) {
        ContentSearchFragment fragment = new ContentSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        args.putBoolean(ARG_HAS_OPTIONS_MENU, hasOptionsMenu);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    protected void onPrepareAdapter(@NonNull Context context) {
        mAdapter = new ContentListAdapter(context);
    }

    /**
     * Sets the {@link Fragment} parameters based on the supplied {@link Bundle} of arguments.
     *
     * @param args The {@code Bundle} arguments set when initializing the {@code Fragment}.
     */
    @Override
    protected void onHandleArguments(@NonNull Bundle args) {
        super.onHandleArguments(args);
        mContentQuery = args.getString(ARG_QUERY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreateViewModel() {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreateViewModel();
        mViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel.class);
        mViewModel.getQueryObservable().observe(ContentSearchFragment.this, new Observer<String>() {
            @Override
            public void onChanged(String query) {
                if (mContentQuery != null && !mContentQuery.equals(query)) {
                    mContentQuery = query;
                    onSearchRequested();
                }
            }
        });
        mViewModel.getSearchResults().observe(ContentSearchFragment.this, new Observer<List<Content>>() {
            @Override
            public void onChanged(List<Content> content) {
                if (content != null) {
                    mListData.clear();
                    mListData.addAll(content);
                }
                if (isAdded()) onSetView();
            }
        });
        if (mContentQuery != null)
            mListener.onExecuteSearch(mContentQuery);
    }

    /**
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        mContentQuery = state.getString(ARG_QUERY);
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
        inflater.inflate(R.menu.menu_content_search, menu);
        mListener.onPrepareSearchManager(this,
                (SearchView)menu.findItem(R.id.menu_search).getActionView());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBaseListener.setTitle("Search Content");
        return super.onCreateView(inflater, container, savedInstanceState);
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
        super.onViewCreated(view, savedInstanceState);
        onSetView();
    }

    private void onSearchRequested() {
        // Log.d(TAG, "onSearchRequested");
        mBaseListener.showLoadingDialog(String.format("Searching: %s", mContentQuery));
        mListener.onExecuteSearch(mContentQuery);
    }

    private void onSetView() {
        mBaseListener.hideLoadingDialog();
        if (mListData.size() == 0) {
            showModal(String.format(MODAL_BODY_FORMATTABLE, mContentQuery));
        } else {
            hideModal();
            mAdapter.setAdapterData(mListData);
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
        super.onSaveInstanceState(outState);
        outState.putString(ARG_QUERY, mContentQuery);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mViewModel.getQueryObservable().removeObservers(ContentSearchFragment.this);
    }

    //region Content Search -- ListAdapter.OnAdapterBoundaryReachedListener
    @Override
    public void onStartReached() {

    }

    @Override
    public void onEndReached(int pos) {

    }
    //endregion

    private class ContentListAdapter extends ListAdapter<Content, ContentListAdapter.ViewHolder> {

        ContentListAdapter(@NonNull Context context) {
            super(context);
        }

        @Override
        protected int getHolderLayoutRes() {
            return DeviceUtil.DeviceIsHMT()
                    ? R.layout.item_list_adapter_hmt
                    : R.layout.item_list_adapter;
        }

        /**
         * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(getHolderLayoutRes(), parent, false));
        }

        protected class ViewHolder extends ListAdapter.ViewHolder {
            private int mPosition;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            protected void bindData(Object item, int pos) {
                mPosition = pos;
                setData(item);
                if (item instanceof Content) {
                    getItemTitle().setText(((Content) item).getTitle());
                    getItemDescription().setText(((Content) item).getToken());
                    if (DeviceUtil.DeviceIsHMT()) {
                        getView().setContentDescription(String.format("hf_no_number|Select Document %s", pos+1));
                        getLabel().setText(String.format("Select Document %s", pos+1));
                    }
                }
            }

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                mListener.onClickViewHolder((Content)getData(), mPosition);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener extends
            ListAdapter.OnAdapterInteractionListener<Content> {
        void onPrepareSearchManager(Fragment fragment, SearchView view);
        void onExecuteSearch(@NonNull String query);
    }
}
