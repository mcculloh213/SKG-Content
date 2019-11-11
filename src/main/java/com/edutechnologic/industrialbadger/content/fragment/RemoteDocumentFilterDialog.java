package com.edutechnologic.industrialbadger.content.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.DialogFragment;

import com.edutechnologic.industrialbadger.base.util.DeviceUtil;
import com.edutechnologic.industrialbadger.content.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by H.D. "Chip" McCullough on 4/23/2019.
 */
public class RemoteDocumentFilterDialog extends DialogFragment {
    public static final String NAME = "fragment.dialog:RemoteDocumentFilter";
    private static final String TAG = RemoteDocumentFilterDialog.class.getSimpleName();

    private static final String ARG_SORT_ORDER = "com.industrialbadger.content.remote#ARG_SORT_ORDER";
    private static final String ARG_AVAILABLE_EXTENSIONS = "com.industrialbadger.content.remote#ARG_AVAILABLE_EXTENSIONS";
    private static final String ARG_APPLIED_EXTENSIONS = "com.industrialbadger.content.remote#ARG_APPLIED_EXTENSIONS";

    private static final String SORT_ASCENDING = "ASC";
    private static final String SORT_DESCENDING = "DESC";

    private WeakReference<View> mDialogRoot;
    private static State mState;
    private final List<String> mAvailableExtensions = new ArrayList<>();

    private Callback mListener;

    private RemoteDocumentFilterDialog() {

    }

    public static Filter getFilter(@NonNull Context context) {
        return getFilterState(context).toFilter();
    }

    public static DialogFragment newInstance(ArrayList<String> availableExtensionFilters) {
        DialogFragment dialog = new RemoteDocumentFilterDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_AVAILABLE_EXTENSIONS, availableExtensionFilters);
        dialog.setArguments(args);
        return dialog;
    }

    private static State getFilterState(@NonNull Context context) {
        if (mState == null) {
            synchronized (RemoteDocumentFilterDialog.class) {
                if (mState == null) {
                    mState = new State(context);
                }
            }
        }
        return mState;
    }

    private ChipGroup getSortGroup() {
        return getDialogView().findViewById(R.id.group_sort);
    }

    private ChipGroup getFilterGroup() {
        return getDialogView().findViewById(R.id.group_filter);
    }

    private Chip getChipSortAscending() {
        return getDialogView().findViewById(R.id.chip_sort_ascending);
    }

    private Chip getChipSortDescending() {
        return getDialogView().findViewById(R.id.chip_sort_descending);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Log.d(TAG, "onAttach");
        super.onAttach(context);
        Bundle args = getArguments();
        if (context instanceof Callback)
            mListener = (Callback)context;
        else
            throw new RuntimeException(context.toString() + "" +
                    " must implement RemoteDocumentFilterDialog.ICallback");
        mState = getFilterState(context);
        mDialogRoot = new WeakReference<>(LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_remote_document_filter, null));
        if (args != null) onReceiveArguments(args);
    }

    private void onReceiveArguments(@NonNull Bundle args) {
        List<String> extensionFilters = args.getStringArrayList(ARG_AVAILABLE_EXTENSIONS);
        if (extensionFilters != null && !extensionFilters.isEmpty()) {
            mAvailableExtensions.clear();
            mAvailableExtensions.addAll(extensionFilters);
        }
    }

    /**
     * Override to build your own custom Dialog container.  This is typically
     * used to show an AlertDialog instead of a generic Dialog; when doing so,
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} does not need
     * to be implemented since the AlertDialog takes care of its own content.
     *
     * <p>This method will be called after {@link #onCreate(Bundle)} and
     * before {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.  The
     * default implementation simply instantiates and returns a {@link Dialog}
     * class.
     *
     * <p><em>Note: DialogFragment own the {@link Dialog#setOnCancelListener
     * Dialog.setOnCancelListener} and {@link Dialog#setOnDismissListener
     * Dialog.setOnDismissListener} callbacks.  You must not set them yourself.</em>
     * To find out about these events, override {@link #onCancel(DialogInterface)}
     * and {@link #onDismiss(DialogInterface)}.</p>
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return Return a new Dialog instance to be displayed by the Fragment.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateDialog");
        final TypedValue value = new TypedValue();
        final Drawable drawable = requireContext().getDrawable(R.drawable.ic_filter_list);
        if (drawable != null) {
            final Drawable wrapped = DrawableCompat.wrap(drawable);
            requireContext().getTheme().resolveAttribute(R.attr.colorAccent, value, true);
            drawable.mutate();
            DrawableCompat.setTint(wrapped, value.data);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(requireActivity())
                .setIcon(drawable)
                .setTitle("Filter Documents")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.applyFilter(mState.apply());
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mState.clear();
                        getSortGroup().invalidate();
                        getFilterGroup().invalidate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);

        dialog.setView(getDialogView());
        onPopulateFilters();
        onRegisterUiListeners();
        return dialog.create();
    }

    private void onPopulateFilters() {
        if (mState.mSortOrder.equals(SORT_DESCENDING))
            getChipSortDescending().setChecked(true);
        else
            getChipSortAscending().setChecked(true);
        ChipGroup extGroup = getFilterGroup();
        for (String ext : mAvailableExtensions)
            extGroup.addView(new FileExtensionChip(requireContext(), ext));
    }

    private void onRegisterUiListeners() {
        getSortGroup().setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                if (checkedId == R.id.chip_sort_ascending)
                    mState.setSortOrder(SORT_DESCENDING);
                else
                    mState.setSortOrder(SORT_ASCENDING);
            }
        });
    }

    private View getDialogView() {
        return mDialogRoot.get();
    }

    private class FileExtensionChip extends Chip implements
            CompoundButton.OnCheckedChangeListener {

        private final String mFilter;
        private final ChipDrawable mSelectedDrawable;

        public FileExtensionChip(@NonNull Context context, @NonNull String filter) {
            this(context, null, filter);
        }

        public FileExtensionChip(Context context, AttributeSet attrs, @NonNull String filter) {
            super(context, attrs, R.attr.chipStyle);
            mFilter = filter;
            mSelectedDrawable = ChipDrawable.createFromResource(context, R.xml.chip_file_extension);
            init(context);
            if (mState.containsFilter(mFilter))
                setChecked(true);
        }

        private void init(@NonNull Context context) {
            mSelectedDrawable.setText(mFilter);
            mSelectedDrawable.setTextAppearanceResource(R.style.TextAppearance_Base_Chip_Filter);
            setCheckable(true);
            setChipDrawable(mSelectedDrawable);
            setChecked(false);
            setOnCheckedChangeListener(this);
            if (DeviceUtil.DeviceIsHMT())
                setContentDescription(String.format("hf_no_number|%s", mFilter.substring(1)));
        }

        public String getFilter() {
            return mFilter;
        }

        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)
                setViewChecked();
            else
                setViewUnchecked();
        }

        private void setViewChecked() {
            setChecked(true);
            mState.addFilter(mFilter);
        }

        private void setViewUnchecked() {
            setChecked(false);
            mState.removeFilter(mFilter);
        }
    }

    private static class State {

        private String mSortOrder;
        private final HashSet<String> mExtensionFilters = new HashSet<>();
        private final WeakReference<SharedPreferences> mPrefsRef;

        private State(@NonNull Context context) {
            SharedPreferences prefs = context.getSharedPreferences("_RemoteContent.Filter", Context.MODE_PRIVATE);
            mSortOrder = prefs.getString(ARG_SORT_ORDER, SORT_ASCENDING);
            mExtensionFilters.addAll(prefs.getStringSet(ARG_APPLIED_EXTENSIONS, new HashSet<String>()));
            mPrefsRef = new WeakReference<>(prefs);
        }

        private Filter toFilter() {
            return new Filter(mSortOrder, mExtensionFilters);
        }

        private void setSortOrder(@NonNull String order) {
            mSortOrder = order;
        }

        private void addFilter(@NonNull String filter) {
            mExtensionFilters.add(filter);
        }

        private void removeFilter(@NonNull String filter) {
            mExtensionFilters.remove(filter);
        }

        private boolean containsFilter(@NonNull String filter) {
            return mExtensionFilters.contains(filter);
        }

        private void clear() {
            mSortOrder = SORT_ASCENDING;
            mExtensionFilters.clear();
        }

        private Filter apply() {
            SharedPreferences.Editor editor = getFilterPreferences().edit();
            editor.putString(ARG_SORT_ORDER, mSortOrder);
            editor.putStringSet(ARG_APPLIED_EXTENSIONS, mExtensionFilters);
            editor.apply();
            return toFilter();
        }

        private SharedPreferences getFilterPreferences() {
            return mPrefsRef.get();
        }
    }

    public static class Filter {

        private final String mSortOrder;
        private final Set<String> mExtensionFilters = new HashSet<>();

        private Filter(String order, Collection<String> extensionFilters) {
            mSortOrder = order;
            mExtensionFilters.addAll(extensionFilters);
        }

        public String getSortOrder() {
            return mSortOrder;
        }

        public Set<String> getExtensionFilters() {
            return mExtensionFilters;
        }
    }

    public interface Callback {
        void applyFilter(Filter extFilter);
    }
}
