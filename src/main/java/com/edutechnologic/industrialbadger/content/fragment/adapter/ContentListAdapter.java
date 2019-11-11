package com.edutechnologic.industrialbadger.content.fragment.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
import com.edutechnologic.industrialbadger.content.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ktx.sovereign.database.entity.Content;

/**
 * Created by H.D. "Chip" McCullough on 3/11/2019.
 */
public class ContentListAdapter extends RecyclerView.Adapter<ContentListAdapter.ContentViewHolder> {
    private static final String TAG = ContentListAdapter.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private List<Content> mContent;

    private OnAdapterInteractionListener mListener;

    class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final String TAG = ContentViewHolder.class.getSimpleName();
        private static final String DESCRIPTION = "Document %s";
        private static final String HMT_DESCRIPTION = "Select Document %s";
        private static final String HMT_TAG = "hf_no_number|Select Document %s";

        private final View mView;
        private Content mContent;
        private int mPosition;

        private TextView getContentTitle() {
            return mView.findViewById(R.id.content_title);
        }

        private TextView getContentDescription() {
            return mView.findViewById(R.id.content_tag);
        }

        ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(this);
        }

        void bindContent(Content content, int index) {
            // Log.d(TAG, "bindContent");
            mContent = content;
            mPosition = index;
            String displayname;
            if (mContent.getTitle().lastIndexOf('.') > 0) {
                displayname = mContent.getTitle().substring(0, mContent.getTitle().lastIndexOf('.'));
            } else {
                displayname = mContent.getTitle();
            }
            getContentTitle().setText(displayname);
            if (DeviceUtil.DeviceIsHMT()) {
                getContentDescription().setText(String.format(HMT_DESCRIPTION, index+1));
                mView.setContentDescription(String.format(HMT_TAG, index+1));
            } else {
                getContentDescription().setText(String.format(DESCRIPTION, index+1));
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickContent(mContent, mPosition);
        }
    }

    public ContentListAdapter(@NonNull Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        if (context instanceof OnAdapterInteractionListener)
            mListener = (OnAdapterInteractionListener)context;
        else
            throw new RuntimeException(context.toString()
                    + " must implement ContentListAdapter.OnAdapterInteractionListener");
    }

    public void setAdapterContent(List<Content> content) {
        // Log.d(TAG, "setAdapterContent");
        mContent = content;
        notifyDataSetChanged();
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
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Log.d(TAG, "onCreateView");
        return new ContentViewHolder(
                mLayoutInflater.inflate(R.layout.item_content_list, parent, false)
        );
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link RecyclerView.ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link RecyclerView.ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     * Override {@link #onBindViewHolder(RecyclerView.ViewHolder, int, List)} instead if Adapter can
     * handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        // Log.d(TAG, "onBindViewHolder");
        holder.bindContent(mContent.get(position), position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mContent == null ? 0 : mContent.size();
    }

    public interface OnAdapterInteractionListener {
        void onClickContent(Content content, int position);
    }
}
