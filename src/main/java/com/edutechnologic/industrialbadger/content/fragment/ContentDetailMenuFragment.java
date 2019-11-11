package com.edutechnologic.industrialbadger.content.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.edutechnologic.industrialbadger.base.fragment.BaseFragment;
import com.edutechnologic.industrialbadger.base.viewmodel.ContentViewModel;
import com.edutechnologic.industrialbadger.content.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ContentDetailMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ContentDetailMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentDetailMenuFragment extends BaseFragment {
    public static final String NAME = "fragment:ContentDetailMenu";
    private static final String TAG = ContentDetailMenuFragment.class.getSimpleName();

    public static final String ARG_MENU_OPEN = "com.industrialbadger.content.menu#ARG_MENU_OPEN";
    private boolean mMenuOpen = false;

    private ContentViewModel mViewModel;
    private View mFragmentRoot;
    private OnFragmentInteractionListener mListener;

    private LinearLayout getInvertColorsContainer() {
        return mFragmentRoot.findViewById(R.id.invert_container);
    }

    private FloatingActionButton getInvertColors() {
        return mFragmentRoot.findViewById(R.id.invert_control);
    }

    private LinearLayout getCloseContainer() {
        return mFragmentRoot.findViewById(R.id.close_container);
    }

    private FloatingActionButton getClose() {
        return mFragmentRoot.findViewById(R.id.close_control);
    }

    private LinearLayout getZoomOutContainer() {
        return mFragmentRoot.findViewById(R.id.zoom_out_container);
    }

    private FloatingActionButton getZoomOut() {
        return mFragmentRoot.findViewById(R.id.zoom_out_control);
    }

    private LinearLayout getZoomInContainer() {
        return mFragmentRoot.findViewById(R.id.zoom_in_container);
    }

    private FloatingActionButton getZoomIn() {
        return mFragmentRoot.findViewById(R.id.zoom_in_control);
    }

    private LinearLayout getMenuContainer() {
        return  mFragmentRoot.findViewById(R.id.widget_zoom_container);
    }

    private FloatingActionButton getMenu() {
        return  mFragmentRoot.findViewById(R.id.widget_zoom_control);
    }

    public ContentDetailMenuFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContentDetailMenuFragment.
     */
    public static ContentDetailMenuFragment newInstance() {
        return new ContentDetailMenuFragment();
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
    public void onCreate(Bundle savedInstanceState) {
        // Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreateViewModel() {
        // Log.d(TAG, "onCreateViewModel");
        super.onCreateViewModel();
        mViewModel = ViewModelProviders.of(requireActivity()).get(ContentViewModel.class);
        mViewModel.isMenuEnabled().observe(ContentDetailMenuFragment.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enabled) {
                if (enabled == null) enabled = false;
                mViewModel.setMenuOpen(enabled);
                onReceiveViewModelData(enabled);
            }
        });
        mViewModel.getZoomLevel().observe(ContentDetailMenuFragment.this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer zoom) {
                if (zoom == null) zoom = 100;
                mViewModel.setCurrentZoom(zoom);
            }
        });
        mViewModel.isInvertEnabled().observe(ContentDetailMenuFragment.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean enabled) {
                if (enabled == null) enabled = false;
                mViewModel.setInverted(enabled);
            }
        });
    }

    /**
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle state) {
        super.onRestoreInstanceState(state);
        mMenuOpen = state.getBoolean(ARG_MENU_OPEN, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        mFragmentRoot = inflater.inflate(
                R.layout.fragment_content_detail_menu, container, false
        );

        return mFragmentRoot;
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
        onRegisterUiListeners();
    }

    private void onSetView() {
        // Log.d(TAG, "onSetView");
        requireActivity().runOnUiThread(
                mMenuOpen ? new OpenMenuRunnable() : new CloseMenuRunnable()
        );
    }

    /**
     *
     */
    @Override
    protected void onRegisterUiListeners() {
        // Log.d(TAG, "onRegisterUiListeners");
        super.onRegisterUiListeners();
        getMenu().setOnClickListener(new OnClickMenuListener());
        getInvertColors().setOnClickListener(new OnClickInvertColorsListener());
        getZoomIn().setOnClickListener(new OnClickZoomInListener());
        getZoomOut().setOnClickListener(new OnClickZoomOutListener());
        getClose().setOnClickListener(new OnClickCloseListener());
    }

    public void onReceiveViewModelData(boolean isMenuOpen) {
        // Log.d(TAG, "onReceiveViewModelData");
        mMenuOpen = isMenuOpen;
        if (isAdded()) onSetView();
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
     * <p>This corresponds to {@link Activity#onSaveInstanceState(Bundle)
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
        outState.putBoolean(ARG_MENU_OPEN, mMenuOpen);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //region ContentDetailMenuFragment -- UI Listeners
    private class OnClickMenuListener implements View.OnClickListener {
        private final String TAG = OnClickMenuListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickMenu();
        }
    }

    private class OnClickInvertColorsListener implements View.OnClickListener {
        private final String TAG = OnClickInvertColorsListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickInvertColors();
        }
    }

    private class OnClickZoomInListener implements View.OnClickListener {
        private final String TAG = OnClickZoomInListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickZoomIn();
        }
    }

    private class OnClickZoomOutListener implements View.OnClickListener {
        private final String TAG = OnClickZoomOutListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickZoomOut();
        }
    }

    private class OnClickCloseListener implements View.OnClickListener {
        private final String TAG = OnClickCloseListener.class.getSimpleName();

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            // Log.d(TAG, "onClick");
            mListener.onClickClose();
        }
    }

    private class OpenMenuAnimatorListener implements Animator.AnimatorListener {
        private final String TAG = OpenMenuAnimatorListener.class.getSimpleName();

        private final WeakReference<View> mViewRef;

        OpenMenuAnimatorListener(View view) {
            mViewRef = new WeakReference<>(view);
        }

        /**
         * <p>Notifies the start of the animation.</p>
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animator animation) {
            // Log.d(TAG, "onAnimationStart");
            mViewRef.get().setVisibility(View.VISIBLE);
        }

        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            // Log.d(TAG, "onAnimationEnd");
        }

        /**
         * <p>Notifies the cancellation of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which was canceled.
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            // Log.d(TAG, "onAnimationCancel");
        }

        /**
         * <p>Notifies the repetition of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            // Log.d(TAG, "onAnimationRepeat");
        }
    }

    private class CloseMenuAnimatorListener implements Animator.AnimatorListener {
        private final String TAG = CloseMenuAnimatorListener.class.getSimpleName();

        private final WeakReference<View> mViewRef;

        CloseMenuAnimatorListener(View view) {
            mViewRef = new WeakReference<>(view);
        }

        /**
         * <p>Notifies the start of the animation.</p>
         *
         * @param animation The started animation.
         */
        @Override
        public void onAnimationStart(Animator animation) {
            // Log.d(TAG, "onAnimationStart");
        }

        /**
         * <p>Notifies the end of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which reached its end.
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            // Log.d(TAG, "onAnimationEnd");
            mViewRef.get().setVisibility(View.GONE);
        }

        /**
         * <p>Notifies the cancellation of the animation. This callback is not invoked
         * for animations with repeat count set to INFINITE.</p>
         *
         * @param animation The animation which was canceled.
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            // Log.d(TAG, "onAnimationCancel");
        }

        /**
         * <p>Notifies the repetition of the animation.</p>
         *
         * @param animation The animation which was repeated.
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            // Log.d(TAG, "onAnimationRepeat");
        }
    }
    //endregion

    //region ContentDetailMenuFragment -- Runnables
    private class OpenMenuRunnable implements Runnable {
        private final String TAG = OpenMenuRunnable.class.getSimpleName();

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            // Log.d(TAG, "run");

            getInvertColorsContainer().animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new OpenMenuAnimatorListener(getInvertColorsContainer()))
                    .start();
            getCloseContainer().animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new OpenMenuAnimatorListener(getCloseContainer()))
                    .start();
            getZoomOutContainer().animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new OpenMenuAnimatorListener(getZoomOutContainer()))
                    .start();
            getZoomInContainer().animate()
                    .alpha(1)
                    .translationY(0)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new OpenMenuAnimatorListener(getZoomInContainer()))
                    .start();

            mMenuOpen = true;
        }
    }

    private class CloseMenuRunnable implements Runnable {
        private final String TAG = CloseMenuRunnable.class.getSimpleName();
        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            // Log.d(TAG, "run");

            getInvertColorsContainer().animate()
                    .alpha(0)
                    .translationYBy(-250)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new CloseMenuAnimatorListener(getInvertColorsContainer()))
                    .start();
            getCloseContainer().animate()
                    .alpha(0)
                    .translationYBy(-250)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new CloseMenuAnimatorListener(getCloseContainer()))
                    .start();
            getZoomOutContainer().animate()
                    .alpha(0)
                    .translationYBy(-175)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new CloseMenuAnimatorListener(getZoomOutContainer()))
                    .start();
            getZoomInContainer().animate()
                    .alpha(0)
                    .translationYBy(-100)
                    .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime))
                    .setListener(new CloseMenuAnimatorListener(getZoomInContainer()))
                    .start();

            mMenuOpen = false;
        }
    }
    //endregion

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
    public interface OnFragmentInteractionListener {
        void onClickMenu();

        void onClickInvertColors();

        void onClickZoomIn();

        void onClickZoomOut();

        void onClickClose();
    }
}
