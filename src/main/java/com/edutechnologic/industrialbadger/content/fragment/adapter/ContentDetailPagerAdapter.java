package com.edutechnologic.industrialbadger.content.fragment.adapter;

import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.edutechnologic.industrialbadger.content.fragment.ContentViewerFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import ktx.sovereign.database.entity.Content;

/**
 * Created by H.D. "Chip" McCullough on 3/11/2019.
 */
public class ContentDetailPagerAdapter extends FragmentStatePagerAdapter {
    private static final String TAG = ContentDetailPagerAdapter.class.getSimpleName();

    private final ArrayList<Content> mContentList = new ArrayList<>();
    private final SparseArray<ContentViewerFragment> mRegisteredFragments = new SparseArray<>();

    public ContentDetailPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    public void onReceiveData(List<Content> content) {
        // Log.d(TAG, "onReceiveData");
        if (content != null) {
            mContentList.clear();
            mContentList.addAll(content);
            notifyDataSetChanged();
        }
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return ContentViewerFragment.newInstance(mContentList.get(position));
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mContentList.size();
    }

    @Override
    public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position) {
        // Log.d(TAG, "instantiateItem");
        ContentViewerFragment fragment = (ContentViewerFragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        // Log.d(TAG, "destroyItem");
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public ContentViewerFragment getRegisteredFragment(int position) {
        return mRegisteredFragments.get(position);
    }
}
