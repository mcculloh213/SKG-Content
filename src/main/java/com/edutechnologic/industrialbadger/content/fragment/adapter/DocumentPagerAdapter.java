//package com.edutechnologic.industrialbadger.content.fragment.adapter;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.ViewGroup;
//
//import com.edutechnologic.industrialbadger.content.fragment.DocumentListFragment;
//import com.edutechnologic.industrialbadger.content.fragment.DocumentSearchFragment;
//
//import java.util.ArrayList;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentManager;
//import androidx.fragment.app.FragmentPagerAdapter;
//
///**
// * Created by H.D. "Chip" McCullough on 12/2/2018.
// */
//public class DocumentPagerAdapter extends FragmentPagerAdapter {
//    private static final String TAG = DocumentPagerAdapter.class.getSimpleName();
//
//    private static final ArrayList<ViewPagerFragment> mContentFragments = new ArrayList<>();
//    private final SparseArray<Fragment> mRegisteredFragments = new SparseArray<>();
//
//    public DocumentPagerAdapter(@NonNull FragmentManager fm) {
//        super(fm);
//    }
//
//    /**
//     * Return the Fragment associated with a specified position.
//     *
//     * @param position
//     */
//    @Override
//    public Fragment getItem(int position) {
//        // Log.d(TAG, "getItem");
//        return createFragment(mContentFragments.get(position));
//    }
//
//    @NonNull
//    @Override
//    public Object instantiateItem(@NonNull ViewGroup container, int position) {
//        // Log.d(TAG, "instantiateItem");
//        Fragment fragment = (Fragment) super.instantiateItem(container, position);
//        mRegisteredFragments.put(position, fragment);
//        return fragment;
//    }
//
//    @Override
//    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        // Log.d(TAG, "destroyItem");
//        mRegisteredFragments.remove(position);
//        super.destroyItem(container, position, object);
//    }
//
//    /**
//     * Return the number of views available.
//     */
//    @Override
//    public int getCount() {
//        // Log.d(TAG, "getCount");
//        return mContentFragments.size();
//    }
//
//    /**
//     * This method may be called by the ViewPager to obtain a title string
//     * to describe the specified page. This method may return null
//     * indicating no title for this page. The default implementation returns
//     * null.
//     *
//     * @param position The position of the title requested
//     * @return A title for the requested page
//     */
//    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        // Log.d(TAG, "getPageTitle");
//        return mContentFragments.get(position).getTitle();
//    }
//
//    public Fragment getRegisteredFragment(int position) {
//        // Log.d(TAG, "getRegisteredFragment");
//        return mRegisteredFragments.get(position);
//    }
//
//    private Fragment createFragment(ViewPagerFragment item) {
//        // Log.d(TAG, "createFragment");
//
//        switch (item.getName()) {
//            case DocumentListFragment.NAME:
//                return DocumentListFragment.newInstance();
//            case DocumentSearchFragment.NAME:
//                return DocumentSearchFragment.newInstance();
//            default:
//                break;
//        }
//
//        return null;
//    }
//
//    public static class ViewPagerFragment {
//        private final String mName;
//        private final String mTitle;
//        private final Bundle mArgs;
//
//        ViewPagerFragment(@NonNull String name, @NonNull String title, Bundle args) {
//            mName = name;
//            mTitle = title;
//            mArgs = args;
//        }
//
//        String getName() {
//            return mName;
//        }
//
//        String getTitle() {
//            return mTitle;
//        }
//
//        Bundle getArguments() {
//            return mArgs;
//        }
//    }
//
//    static {
//        mContentFragments.add(new ViewPagerFragment(DocumentListFragment.NAME, "Google Cloud Storage", null));
//        mContentFragments.add(new ViewPagerFragment(DocumentSearchFragment.NAME, "Search", null));
//    }
//}
