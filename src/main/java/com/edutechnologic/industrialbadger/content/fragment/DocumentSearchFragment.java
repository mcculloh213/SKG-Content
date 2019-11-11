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
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.edutechnologic.industrialbadger.base.listener.OnRequestUiThreadListener;
//import com.edutechnologic.industrialbadger.content.R;
//import com.edutechnologic.industrialbadger.content.fragment.adapter.DocumentSearchAdapter;
//import com.edutechnologic.industrialbadger.database.entities.DiscoveryQuery;
//
//import java.util.List;
//
///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link DocumentSearchFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link DocumentSearchFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class DocumentSearchFragment extends Fragment {
//    public static final String NAME = "fragment:ContentSearch";
//    private static final String TAG = DocumentSearchFragment.class.getSimpleName();
//
//    private View mFragmentRoot;
//    private DocumentSearchAdapter mAdapter;
//
//    private OnFragmentInteractionListener mListener;
//
//    public DocumentSearchFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @return A new instance of fragment DocumentSearchFragment.
//     */
//    public static DocumentSearchFragment newInstance() {
//        // Log.d(TAG, "newInstance");
//        return new DocumentSearchFragment();
//    }
//
//    //region Activity --> Fragment
//    public void setResultsHeader(String query) {
//        // Log.d(TAG, "setResultsHeader");
//
//        if (isAdded())
//            mListener.delegateToUiThread(
//                    new SetSearchResultHeaderRunnable(
//                            getString(R.string.content_search_header,
//                                    mAdapter.getItemCount(),
//                                    query)
//                    )
//            );
//    }
//
//    public void receiveResults(List<DiscoveryQuery> results) {
//        // Log.d(TAG, "receiveResults");
//
//        if (isAdded()) mAdapter.setResults(results);
//    }
//
//    public void setProgressIndicatorVisibility(int visibility) {
//        // Log.d(TAG, "setProgressIndicatorVisibility");
//
//        if (isAdded())
//            mListener.delegateToUiThread(new SetProgressIndicatorVisibilityRunnable(visibility));
//    }
//    //endregion
//
//    //region View Getters
//    private TextView getSearchResultHeader() {
//        return mFragmentRoot.findViewById(R.id.search_result_header);
//    }
//
//    private RecyclerView getRecyclerView() {
//        return mFragmentRoot.findViewById(R.id.recycler_view);
//    }
//
//    private FrameLayout getProgressIndicator() {
//        return mFragmentRoot.findViewById(R.id.search_indicator_container);
//    }
//    //endregion
//
//    //region Fragment Lifecycle Methods
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//
//        mAdapter = new DocumentSearchAdapter(context);
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
//                R.layout.fragment_document_search, container, false
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
//
//        detachUiListeners();
//
//        mListener = null;
//        mAdapter = null;
//    }
//    //endregion
//
//    //region Private Methods
//    private void initializeRecyclerView() {
//        // Log.d(TAG, "initializeRecyclerView");
//
//        RecyclerView recyclerView = getRecyclerView();
//        LinearLayoutManager manager = new LinearLayoutManager(getContext());
//        manager.setOrientation(RecyclerView.VERTICAL);
//
//        recyclerView.setLayoutManager(manager);
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
//    }
//
//    private void detachUiListeners() {
//        // Log.d(TAG, "detachUiListeners");
//    }
//    //endregion
//
//    //region UI Listeners & Runnables
//    private class SetProgressIndicatorVisibilityRunnable implements Runnable {
//        private final String TAG = SetProgressIndicatorVisibilityRunnable.class.getSimpleName();
//
//        private int mVisibilityState;
//
//        SetProgressIndicatorVisibilityRunnable(int state) {
//            mVisibilityState = state;
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
//            getProgressIndicator().setVisibility(mVisibilityState);
//        }
//    }
//
//    private class SetSearchResultHeaderRunnable implements Runnable {
//        private final String TAG = SetSearchResultHeaderRunnable.class.getSimpleName();
//
//        private String mHeaderText;
//
//        SetSearchResultHeaderRunnable(String text) {
//            mHeaderText = text;
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
//            getSearchResultHeader().setText(mHeaderText);
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
//            DocumentSearchAdapter.OnAdapterInteractionListener {
//
//        @Override
//        void getFile(String filename);
//    }
//}
