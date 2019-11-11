//package com.edutechnologic.industrialbadger.content.fragment.adapter;
//
//import android.content.Context;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
//import com.edutechnologic.industrialbadger.content.R;
//import com.edutechnologic.industrialbadger.database.entities.DiscoveryQuery;
//
//import java.util.List;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
///**
// * Created by H.D. "Chip" McCullough on 12/2/2018.
// */
//public class DocumentSearchAdapter extends RecyclerView.Adapter<DocumentSearchAdapter.ContentHolder> {
//    private static final String TAG = DocumentListAdapter.class.getSimpleName();
//    private static final String HMT_TAG = "Select Item %d";
//
//    private LayoutInflater mLayoutInflater;
//    private List<DiscoveryQuery> mResults;
//
//    private OnAdapterInteractionListener mListener;
//
//    class ContentHolder extends RecyclerView.ViewHolder {
//        private final View mViewRoot;
//
//        private DiscoveryQuery mResult;
//
//        ContentHolder(@NonNull View itemView) {
//            super(itemView);
//            mViewRoot = itemView;
//            mViewRoot.setOnClickListener(new OnClickContentHolderListener());
//        }
//
//        private Context getContext() {
//            return mViewRoot.getContext();
//        }
//
//        void bindResult(DiscoveryQuery result, int i) {
//            // Log.d(TAG, "bindResult");
//            mResult = result;
//
//            getFilename().setText(result.getFilename());
//
//            if (DeviceUtil.DeviceIsHMT()) {
//                getContentDescriptor().setVisibility(View.VISIBLE);
//                getContentDescriptor().setText(getContext().getString(R.string.hmt_content_list_item_open_descriptor, i));
//                mViewRoot.setContentDescription(getContext().getString(R.string.hf_content_list_open, i));
//            }
//        }
//
//        TextView getFilename() {
//            return mViewRoot.findViewById(R.id.file_name);
//        }
//
//        TextView getContentDescriptor() {
//            return mViewRoot.findViewById(R.id.hmt_content_descriptor);
//        }
//
//        private class OnClickContentHolderListener implements View.OnClickListener {
//            private final String TAG = OnClickContentHolderListener.class.getSimpleName();
//
//            /**
//             * Called when a view has been clicked.
//             *
//             * @param v The view that was clicked.
//             */
//            @Override
//            public void onClick(View v) {
//                // Log.d(TAG, "onClick");
//                mListener.getFile(mResult.getFilename());
//            }
//        }
//    }
//
//    public DocumentSearchAdapter(@NonNull Context context) {
//        mLayoutInflater = LayoutInflater.from(context);
//
//        if (context instanceof OnAdapterInteractionListener) {
//            mListener = (OnAdapterInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement DocumentListAdapter.OnAdapterInteractionListener");
//        }
//    }
//
//    public void setResults(List<DiscoveryQuery> results) {
//        // Log.d(TAG, "setContent");
//
//        mResults = results;
//        notifyDataSetChanged();
//    }
//
//    /**
//     * Called when RecyclerView needs a new {@link RecyclerView.ViewHolder} of the given type to represent
//     * an item.
//     * <p>
//     * This new ViewHolder should be constructed with a new View that can represent the items
//     * of the given type. You can either create a new View manually or inflate it from an XML
//     * layout file.
//     * <p>
//     * The new ViewHolder will be used to display items of the adapter using
//     * {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)}. Since it will be re-used to display
//     * different items in the data set, it is a good idea to cache references to sub views of
//     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
//     *
//     * @param parent   The ViewGroup into which the new View will be added after it is bound to
//     *                 an adapter position.
//     * @param viewType The view type of the new View.
//     * @return A new ViewHolder that holds a View of the given view type.
//     * @see #getItemViewType(int)
//     * @see #onBindViewHolder(RecyclerView.ViewHolder, int)
//     */
//    @NonNull
//    @Override
//    public ContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        // Log.d(TAG, "onCreateViewHolder");
//
//        return new ContentHolder(
//                mLayoutInflater.inflate(
//                        R.layout.item_document_search_list,
//                        parent,
//                        false
//                )
//        );
//    }
//
//    /**
//     * Called by RecyclerView to display the data at the specified position. This method should
//     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
//     * position.
//     * <p>
//     * Note that unlike {@link ListView}, RecyclerView will not call this method
//     * again if the position of the item changes in the data set unless the item itself is
//     * invalidated or the new position cannot be determined. For this reason, you should only
//     * use the <code>position</code> parameter while acquiring the related data item inside
//     * this method and should not keep a copy of it. If you need the position of an item later
//     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
//     * have the updated adapter position.
//     * <p>
//     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
//     * handle efficient partial bind.
//     *
//     * @param holder   The ViewHolder which should be updated to represent the contents of the
//     *                 item at the given position in the data set.
//     * @param position The position of the item within the adapter's data set.
//     */
//    @Override
//    public void onBindViewHolder(@NonNull ContentHolder holder, int position) {
//        // Log.d(TAG, "onBindViewHolder");
//
//        if (mResults != null) {
//            holder.bindResult(mResults.get(position), position);
//        }
//    }
//
//    /**
//     * Returns the total number of items in the data set held by the adapter.
//     *
//     * @return The total number of items in this adapter.
//     */
//    @Override
//    public int getItemCount() {
//        return mResults == null ? 0 : mResults.size();
//    }
//
//    public interface OnAdapterInteractionListener {
//        void getFile(String filename);
//    }
//}
