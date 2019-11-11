package com.edutechnologic.industrialbadger.content.fragment;


import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.edutechnologic.industrialbadger.base.fragment.RecyclerGridFragment;
import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
import com.edutechnologic.industrialbadger.base.util.MimeType;
import com.edutechnologic.industrialbadger.base.viewmodel.ContentViewModel;
import com.edutechnologic.industrialbadger.base.widget.GridAdapter;
import com.edutechnologic.industrialbadger.content.R;
import com.google.android.material.button.MaterialButton;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ktx.sovereign.database.entity.Document;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FileExplorerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileExplorerFragment extends RecyclerGridFragment<Document> {
    private static final String TAG = FileExplorerFragment.class.getSimpleName();
    private static final int TYPE_DIR = 0;
    private static final int TYPE_FILE = 1;
    private static final int MODE_EXPLORER = 0;
    private static final int MODE_SEARCH = 1;

    @IntDef({TYPE_DIR, TYPE_FILE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RemoteType {}

    public static final String ARG_CURRENT_DIRECTORY = "com.industrialbadger.content#ARG_CURRENT_DIRECTORY";
    public static final String ARG_QUERY = "com.industrialbadger.content.search#ARG_QUERY";

    private String mRemoteQuery;
    private int mMode = MODE_EXPLORER;
    private final List<String> mDirectoryBranch = new ArrayList<>();

    private ArrayAdapter<String> mSpinnerAdapter;
    private ContentViewModel mViewModel;
    private OnFragmentInteractionListener mListener;

    private MaterialButton getButtonDirectoryUp() {
        return findViewById(R.id.btn_directory_up);
    }

    private Spinner getSpinnerDirectoryList() {
        return findViewById(R.id.spinner_directory_list);
    }

    private MaterialButton getButtonFilter() {
        return findViewById(R.id.btn_filter);
    }

    public FileExplorerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileExplorerFragment.
     */
    public static Fragment newInstance(boolean hasOptionsMenu) {
        FileExplorerFragment fragment = new FileExplorerFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_HAS_OPTIONS_MENU, hasOptionsMenu);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FileExplorerFragment.
     */
    public static Fragment newInstance(@NonNull String query, boolean hasOptionsMenu) {
        FileExplorerFragment fragment = new FileExplorerFragment();
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
        mAdapter = new FileExplorerAdapter(context);
        mSpinnerAdapter = new ArrayAdapter<>(
                context,
                R.layout.item_spinner_directory,
                mDirectoryBranch
        );
    }

    @Override
    protected void onCreateViewModel() {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreateViewModel();
//        mViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel.class);
//        mViewModel.getRemoteDirectory().observe(FileExplorerFragment.this, new Observer<List<RemoteDocument>>() {
//            @Override
//            public void onChanged(List<RemoteDocument> documents) {
//                if (mMode == MODE_EXPLORER) {
//                    if (documents != null) {
//                        mListData.clear();
//                        mListData.addAll(documents);
//                    }
//                    if (isAdded()) onSetView();
//                }
//            }
//        });
//        mViewModel.getRemoteSearchResults().observe(FileExplorerFragment.this, new Observer<List<RemoteDocument>>() {
//            @Override
//            public void onChanged(List<RemoteDocument> documents) {
//                if (isAdded() && mMode == MODE_SEARCH) {
//                    if (documents != null) {
//                        mListData.clear();
//                        mListData.addAll(documents);
//                    }
//                    onSetView();
//                }
//            }
//        });
        mViewModel.getQueryObservable().observe(FileExplorerFragment.this, new Observer<String>() {
            @Override
            public void onChanged(String query) {
                if (query != null && !StringUtils.isEmpty(query)
                        && (!query.equals(mRemoteQuery) || mMode == MODE_EXPLORER)) {
                    mMode = MODE_SEARCH;
                    mRemoteQuery = query;

                    mListener.onExecuteSearch(mRemoteQuery);
                }
            }
        });
        mViewModel.getRemoteRoot();
    }

    /**
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        mRemoteQuery = state.getString(ARG_QUERY);
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
        int size = menu.size();
        inflater.inflate(R.menu.menu_file_explorer, menu);
        for (int i = 0; i < menu.size() - size; i++)
            onTintMenuItem(menu.getItem(i), android.R.attr.textColorPrimary);
        mListener.onPrepareSearchManager(FileExplorerFragment.this,
                (SearchView)menu.findItem(R.id.menu_search).getActionView());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBaseListener.setTitle("Remote Documents");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getRecyclerFragmentLayout() {
        return R.layout.fragment_file_explorer;
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
        onRegisterUiListeners();
        getSpinnerDirectoryList().setAdapter(mSpinnerAdapter);
        onSetView();
    }

    /**
     *
     */
    @Override
    protected void onRegisterUiListeners() {
        super.onRegisterUiListeners();
        getButtonDirectoryUp().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMode == MODE_EXPLORER) {
                    mViewModel.getParentDirectory();
                } else if (mMode == MODE_SEARCH) {
                    mViewModel.refreshRemoteDirectory();
                    mMode = MODE_EXPLORER;
                }
            }
        });
        getButtonFilter().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> extensions = new ArrayList<>(mViewModel.getAvailableFileExtensions());
                DialogFragment dialog = RemoteDocumentFilterDialog.newInstance(extensions);
                dialog.show(getChildFragmentManager(), RemoteDocumentFilterDialog.NAME);
            }
        });
    }

    private void onSetView() {
        mAdapter.setAdapterData(mListData);
        mDirectoryBranch.clear();
//        mDirectoryBranch.addAll(Arrays.asList(mViewModel.getCurrentDirectory().replace("/motoman", "").split("/")));
        if (mDirectoryBranch.size() > 0) {
            mDirectoryBranch.remove(0);
        }
        mDirectoryBranch.add(0, "/");
        getSpinnerDirectoryList().setSelection(mDirectoryBranch.size() - 1);
        mSpinnerAdapter.notifyDataSetChanged();

        if (mMode == MODE_SEARCH) {
            MaterialButton btn = getButtonDirectoryUp();
            btn.setIconResource(R.drawable.ic_close);
            btn.setText("Cancel");
            btn.setContentDescription("hf_no_number|Cancel");
            getSpinnerDirectoryList().setEnabled(false);
        } else if (mMode == MODE_EXPLORER) {
            MaterialButton btn = getButtonDirectoryUp();
            btn.setIconResource(R.drawable.ic_arrow_up);
            btn.setText("Go Up");
            btn.setContentDescription("hf_no_number|Go Up");
            getSpinnerDirectoryList().setEnabled(true);
        }
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
        return super.onOptionsItemSelected(item);
    }

    private void onSearchRequested() {
        // Log.d(TAG, "onSearchRequested");
        mBaseListener.showLoadingDialog(String.format("Searching: %s", mRemoteQuery));
        mListener.onExecuteSearch(mRemoteQuery);
    }

    //region
    @Override
    public void onStartReached() {

    }

    @Override
    public void onEndReached(int pos) {

    }
    //endregion

    private class FileExplorerAdapter extends GridAdapter<Document, FileExplorerAdapter.ViewHolder> {

        FileExplorerAdapter(@NonNull Context context) {
            super(context);
        }

        @Override
        protected int getHolderLayoutRes() {
            return R.layout.item_grid_chip_adapter;
        }

        private int getHolderLayoutRes(int mode) {
            return mode == TYPE_DIR
                    ? DeviceUtil.DeviceIsHMT() ? R.layout.item_grid_chip_adapter_hmt : R.layout.item_grid_chip_adapter
                    : DeviceUtil.DeviceIsHMT() ? R.layout.item_grid_adapter_hmt : R.layout.item_grid_adapter;
        }

        /**
         * Return the view type of the item at <code>position</code> for the purposes
         * of view recycling.
         *
         * <p>The default implementation of this method returns 0, making the assumption of
         * a single view type for the adapter. Unlike ListView adapters, types need not
         * be contiguous. Consider using id resources to uniquely identify item view types.
         *
         * @param position position to query
         * @return integer value identifying the type of the view needed to represent the item at
         * <code>position</code>. Type codes need not be contiguous.
         */
        @Override
        public int getItemViewType(int position) {
            return 0;
//            return mAdapterData.get(position).getFileType().equals("directory")
//                    ? TYPE_DIR
//                    : TYPE_FILE;
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
                    .inflate(getHolderLayoutRes(viewType), parent, false));
        }

        protected class ViewHolder extends GridAdapter.ViewHolder {
            private int mPosition;
            private final int DESCRIPTION_LENGTH_THRESHOLD = 10;
            private final String CONTENT_DESCRIPTION_FORMATTABLE = "hf_no_number|hf_show_text|Select %s";
            private final String DEFAULT_DESCRIPTION_FORMATTABLE = "Document %s";

            private ImageView getItemThumbnail() {
                return (ImageView)findViewById(R.id.item_thumbnail);
            }

            private ImageView getItemIcon() {
                return (ImageView)findViewById(R.id.item_icon);
            }

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void bindData(Object item, int pos) {
                super.bindData(item, pos);
                mPosition = pos;
                if (item instanceof Document) {
                    Document document = (Document) item;
                    getItemTitle().setText(document.getFilename());
                    if (!document.getMime().equals("directory"))
                        setFileView(document);
                    if (DeviceUtil.DeviceIsHMT()) {
                        getView().setContentDescription(makeContentDescription(document, pos));
                        getLabel().setText(getDescription(document, pos));
                    }
                }
            }

            private String makeContentDescription(Document document, int pos) {
                return String.format(CONTENT_DESCRIPTION_FORMATTABLE, getDescription(document, pos));
            }

            private String getDescription(Document document, int pos) {
                String name = document.getFilename();
                if (name.length() > DESCRIPTION_LENGTH_THRESHOLD)
                    return String.format(DEFAULT_DESCRIPTION_FORMATTABLE, pos+1);
                else
                    return name.contains(".") ? name.substring(0, name.indexOf('.')) : name;
            }

            private void setFileView(Document document) {
                MimeType type = MimeType.tryParseExtension(document.getMime());
                getItemThumbnail().setImageResource(type.getIconRes());
                getItemIcon().setImageResource(type.getIconRes());
            }

            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                mListener.onClickViewHolder((Document)getData());
            }
        }
    }

    private class DirectoryBranchSpinnerAdapter implements SpinnerAdapter {

        /**
         * Gets a {@link View} that displays in the drop down popup
         * the data at the specified position in the data set.
         *
         * @param position    index of the item whose view we want.
         * @param convertView the old view to reuse, if possible. Note: You should
         *                    check that this view is non-null and of an appropriate type before
         *                    using. If it is not possible to convert this view to display the
         *                    correct data, this method can create a new view.
         * @param parent      the parent that this view will eventually be attached to
         * @return a {@link View} corresponding to the data at the
         * specified position.
         */
        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        /**
         * Register an observer that is called when changes happen to the data used by this adapter.
         *
         * @param observer the object that gets notified when the data set changes.
         */
        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        /**
         * Unregister an observer that has previously been registered with this
         * adapter via {@link #registerDataSetObserver}.
         *
         * @param observer the object to unregister.
         */
        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return mDirectoryBranch.size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return mDirectoryBranch.get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Indicates whether the item ids are stable across changes to the
         * underlying data.
         *
         * @return True if the same id always refers to the same object.
         */
        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }

        /**
         * Get the type of View that will be created by {@link #getView} for the specified item.
         *
         * @param position The position of the item within the adapter's data set whose view type we
         *                 want.
         * @return An integer representing the type of View. Two views should share the same type if one
         * can be converted to the other in {@link #getView}. Note: Integers must be in the
         * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
         * also be returned.
         * @see #IGNORE_ITEM_VIEW_TYPE
         */
        @Override
        public int getItemViewType(int position) {
            return 1;
        }

        /**
         * <p>
         * Returns the number of types of Views that will be created by
         * {@link #getView}. Each type represents a set of views that can be
         * converted in {@link #getView}. If the adapter always returns the same
         * type of View for all items, this method should return 1.
         * </p>
         * <p>
         * This method will only be called when the adapter is set on the {@link AdapterView}.
         * </p>
         *
         * @return The number of types of Views that will be created by this adapter
         */
        @Override
        public int getViewTypeCount() {
            return 1;
        }

        /**
         * @return true if this adapter doesn't contain any data.  This is used to determine
         * whether the empty view should be displayed.  A typical implementation will return
         * getCount() == 0 but since getCount() includes the headers and footers, specialized
         * adapters might want a different behavior.
         */
        @Override
        public boolean isEmpty() {
            return mDirectoryBranch.size() == 0;
        }
    }

    public interface OnFragmentInteractionListener extends
            GridAdapter.OnAdapterInteractionListener<Document> {
        void onPrepareSearchManager(Fragment fragment, SearchView view);
        void onExecuteSearch(@NonNull String query);
    }
}
