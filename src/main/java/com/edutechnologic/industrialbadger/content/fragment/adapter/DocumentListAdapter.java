//package com.edutechnologic.industrialbadger.content.fragment.adapter;
//
//
//import android.content.Context;
//import android.graphics.drawable.Drawable;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
//import com.edutechnologic.industrialbadger.content.R;
//
//import java.util.List;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.core.graphics.drawable.DrawableCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
///**
// * Created by H.D. "Chip" McCullough on 11/29/2018.
// */
//public class DocumentListAdapter extends RecyclerView.Adapter<DocumentListAdapter.ContentHolder> {
//    private static final String TAG = DocumentListAdapter.class.getSimpleName();
//    private static final String HMT_DOWNLOAD_TAG = "Select or Download Item %d";
//    private static final String HMT_OPEN_TAG = "Select Item %d";
//
//    private LayoutInflater mLayoutInflater;
//    private List<DocumentMap> mContent;
//
//    private OnAdapterInteractionListener mListener;
//
//
//    class ContentHolder extends RecyclerView.ViewHolder {
//        private final View mViewRoot;
//
//        private DocumentMap mDocumentMap;
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
//        void bindDocumentMap(DocumentMap map, int i) {
//            // Log.d(TAG, "bindDocumentMap");
//            mDocumentMap = map;
//
//            if (map.isLeaf()) {
//                Drawable file = DrawableCompat.wrap(ContextCompat.getDrawable(getContext(), R.drawable.ic_file));
//                file.setTint(ContextCompat.getColor(getContext(), R.color.colorAccent));
//                getFileTypeIcon().setImageResource(R.drawable.ic_file);
//            } else {
//                getFileTypeIcon().setImageResource(R.drawable.ic_directory);
//            }
//
//            getFileName().setText(map.getFilename());
//
//            if (map.isDownloaded() || !map.isLeaf()) {
//                getDownload().setVisibility(View.GONE);
//                if (DeviceUtil.DeviceIsHMT()) {
//                    getContentDescriptor().setVisibility(View.VISIBLE);
//                    getContentDescriptor().setText(getContext().getString(R.string.hmt_content_list_item_open_descriptor, i));
//                    mViewRoot.setContentDescription(getContext().getString(R.string.hf_content_list_open, i));
//                }
//            } else {
//                getDownload().setOnClickListener(new OnClickDownloadListener());
//                if (DeviceUtil.DeviceIsHMT()) {
//                    getContentDescriptor().setVisibility(View.VISIBLE);
//                    getContentDescriptor().setText(getContext().getString(R.string.hmt_content_list_item_download_descriptor, i));
//                    mViewRoot.setContentDescription(getContext().getString(R.string.hf_content_list_open, i));
//                    getDownload().setContentDescription(getContext().getString(R.string.hf_content_list_download, i));
//                }
//            }
//        }
//
//        ImageView getFileTypeIcon() {
//            return mViewRoot.findViewById(R.id.file_icon);
//        }
//
//        TextView getFileName() {
//            return mViewRoot.findViewById(R.id.file_name);
//        }
//
//        TextView getContentDescriptor() {
//            return mViewRoot.findViewById(R.id.hmt_content_descriptor);
//        }
//
//        TextView getDownload() {
//            return mViewRoot.findViewById(R.id.download);
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
//
//                if (mDocumentMap.isLeaf())
//                    if (mDocumentMap.isDownloaded())
//                        mListener.openFile(mDocumentMap);
//                    else
//                        mListener.openFileFromRemote(mDocumentMap);
//                else
//                    mListener.openSubDirectory(mDocumentMap);
//            }
//        }
//
//        private class OnClickDownloadListener implements View.OnClickListener {
//            private final String TAG = OnClickDownloadListener.class.getSimpleName();
//
//            /**
//             * Called when a view has been clicked.
//             *
//             * @param v The view that was clicked.
//             */
//            @Override
//            public void onClick(View v) {
//                // Log.d(TAG, "onClick");
//
//                if (mListener.downloadFile(mDocumentMap))
//                    getDownload().setVisibility(View.GONE);
//            }
//        }
//    }
//
//    public DocumentListAdapter(@NonNull Context context) {
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
//    public void setContent(List<DocumentMap> content) {
//        // Log.d(TAG, "setContent");
//
//        mContent = content;
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
//                        R.layout.document_list_item,
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
//        if (mContent != null) {
//            holder.bindDocumentMap(mContent.get(position), position);
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
//        return mContent == null ? 0 : mContent.size();
//    }
//
//    public interface OnAdapterInteractionListener {
//        boolean downloadFile(DocumentMap file);
//
//        void openFile(DocumentMap file);
//
//        void openFileFromRemote(DocumentMap file);
//
//        void openSubDirectory(DocumentMap directory);
//    }
//}
