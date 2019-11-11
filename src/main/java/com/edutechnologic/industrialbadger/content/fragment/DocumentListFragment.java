//package com.edutechnologic.industrialbadger.content.fragment;
//
//import android.content.Context;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.DividerItemDecoration;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.edutechnologic.industrialbadger.base.listener.OnRequestUiThreadListener;
//import com.edutechnologic.industrialbadger.base.widget.EmptyRecyclerView;
//import com.edutechnologic.industrialbadger.content.R;
//import com.edutechnologic.industrialbadger.content.fragment.adapter.DocumentListAdapter;
//import com.edutechnologic.industrialbadger.database.entities.DocumentMap;
//
//import java.lang.ref.WeakReference;
//import java.util.List;
//
///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link DocumentListFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link DocumentListFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class DocumentListFragment extends Fragment {
//    public static final String NAME = "fragment:ContentList";
//    private static final String TAG = DocumentListFragment.class.getSimpleName();
//
//    private View mFragmentRoot;
//    private DocumentListAdapter mAdapter;
//
//    private OnFragmentInteractionListener mListener;
//
//    public DocumentListFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @return A new instance of fragment DocumentListFragment.
//     */
//    public static DocumentListFragment newInstance() {
//        // Log.d(TAG, "newInstance");
//        return new DocumentListFragment();
//    }
//
//    //region Activity --> Fragment
//    public void enableUpDirectory() {
//        // Log.d(TAG, "enableUpDirectory");
//
//        mListener.delegateToUiThread(new EnableUpDirectoryRunnable());
//    }
//
//    public void disableUpDirectory() {
//        // Log.d(TAG, "disableUpDirectory");
//
//        mListener.delegateToUiThread(new DisableUpDirectoryRunnable());
//    }
//
//    public void updateDirectoryPath(String path) {
//        // Log.d(TAG, "updateDirectoryPath");
//
//        mListener.delegateToUiThread(new UpdateDirectoryPathRunnable(path));
//    }
//
//    public void receiveContent(List<DocumentMap> content) {
//        // Log.d(TAG, "receiveContent");
//
//        if (isAdded()) mAdapter.setContent(content);
//    }
//    //endregion
//
//    //region
//    private TextView getUpDirectory() {
//        return mFragmentRoot.findViewById(R.id.btn_directory_up);
//    }
//
//    private TextView getDirectoryPath() {
//        return mFragmentRoot.findViewById(R.id.directory_path);
//    }
//
//    private EmptyRecyclerView getRecyclerView() {
//        return mFragmentRoot.findViewById(R.id.recycler_view);
//    }
//
//    private TextView getEmptyView() {
//        return mFragmentRoot.findViewById(R.id.recycler_view_empty);
//    }
//    //endregion
//
//    //region Fragment Lifecycle Methods
//    @Override
//    public void onAttach(Context context) {
//        // Log.d(TAG, "onAttach");
//        super.onAttach(context);
//
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement DocumentListFragment.OnFragmentInteractionListener");
//        }
//
//        mAdapter = new DocumentListAdapter(context);
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        // Log.d(TAG, "onCreate");
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Log.d(TAG, "onCreateView");
//
//        // Inflate the layout for this fragment
//        mFragmentRoot = inflater.inflate(
//                R.layout.fragment_document_list, container, false
//        );
//
//        initializeRecyclerView();
//        registerUiListeners();
//
//        return mFragmentRoot;
//    }
//
//    @Override
//    public void onDetach() {
//        // Log.d(TAG, "onDetach");
//        super.onDetach();
//        mListener = null;
//        mAdapter = null;
//    }
//    //endregion
//
//    //region
//    private void initializeRecyclerView() {
//        // Log.d(TAG, "initializeRecyclerView");
//
//        EmptyRecyclerView recyclerView = getRecyclerView();
//        LinearLayoutManager manager = new LinearLayoutManager(getContext());
//        manager.setOrientation(RecyclerView.VERTICAL);
//
//        recyclerView.setEmptyView(getEmptyView());
//        recyclerView.setLayoutManager(manager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        recyclerView.addItemDecoration(
//                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)
//        );
//        recyclerView.setAdapter(mAdapter);
//    }
//
//    private void registerUiListeners() {
//        // Log.d(TAG, "registerUiListeners");
//        getUpDirectory().setOnClickListener(new OnClickUpDirectoryListener());
//    }
//    //endregion
//
//    //region UI
//    private class OnClickUpDirectoryListener implements View.OnClickListener {
//        private final String TAG = OnClickUpDirectoryListener.class.getSimpleName();
//
//        /**
//         * Called when a view has been clicked.
//         *
//         * @param v The view that was clicked.
//         */
//        @Override
//        public void onClick(View v) {
//            // Log.d(TAG, "onClick");
//            mListener.upDirectory();
//        }
//    }
//
//    private class DisableUpDirectoryRunnable implements Runnable {
//        private final String TAG = DisableUpDirectoryRunnable.class.getSimpleName();
//
//        /**
//         * When an object implementing interface <code>Runnable</code> is used
//         * to create a thread, starting the thread causes the object's
//         * <code>run</code> method to be called in that separately executing
//         * thread.
//         * <p>
//         * The general contract of the method <code>run</code> is that it may
//         * take any action whatsoever.
//         *
//         * @see Thread#run()
//         */
//        @Override
//        public void run() {
//            // Log.d(TAG, "run");
//            getUpDirectory().setEnabled(false);
//        }
//    }
//
//    private class EnableUpDirectoryRunnable implements Runnable {
//        private final String TAG = EnableUpDirectoryRunnable.class.getSimpleName();
//
//        /**
//         * When an object implementing interface <code>Runnable</code> is used
//         * to create a thread, starting the thread causes the object's
//         * <code>run</code> method to be called in that separately executing
//         * thread.
//         * <p>
//         * The general contract of the method <code>run</code> is that it may
//         * take any action whatsoever.
//         *
//         * @see Thread#run()
//         */
//        @Override
//        public void run() {
//            // Log.d(TAG, "run");
//            getUpDirectory().setEnabled(true);
//        }
//    }
//
//    private class UpdateDirectoryPathRunnable implements Runnable {
//        private final String TAG = UpdateDirectoryPathRunnable.class.getSimpleName();
//
//        private WeakReference<String> mPathRef;
//
//        UpdateDirectoryPathRunnable(String path) {
//            mPathRef = new WeakReference<>(path);
//        }
//
//        /**
//         * When an object implementing interface <code>Runnable</code> is used
//         * to create a thread, starting the thread causes the object's
//         * <code>run</code> method to be called in that separately executing
//         * thread.
//         * <p>
//         * The general contract of the method <code>run</code> is that it may
//         * take any action whatsoever.
//         *
//         * @see Thread#run()
//         */
//        @Override
//        public void run() {
//            // Log.d(TAG, "run");
//            getDirectoryPath().setText(mPathRef.get());
//            getEmptyView().setText(getString(R.string.content_list_empty, mPathRef.get()));
//        }
//    }
//    //endregion
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener extends
//            OnRequestUiThreadListener,
//            DocumentListAdapter.OnAdapterInteractionListener {
//
//        void upDirectory();
//
//        @Override
//        boolean downloadFile(DocumentMap file);
//
//        @Override
//        void openFile(DocumentMap file);
//
//        @Override
//        void openFileFromRemote(DocumentMap file);
//
//        @Override
//        void openSubDirectory(DocumentMap directory);
//    }
//}
