package com.edutechnologic.industrialbadger.content.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.edutechnologic.industrialbadger.content.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Created by H.D. "Chip" McCullough on 11/29/2018.
 */
public class SyncContentDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    public static final String NAME = "fragment:SyncContentDialog";
    private static final String TAG = SyncContentDialogFragment.class.getSimpleName();

    private OnOptionsItemSelectedListener mListener;

    public static SyncContentDialogFragment newInstance() {
        // Log.d(TAG, "newInstance");
        return new SyncContentDialogFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // Log.d(TAG, "onAttach");
        super.onAttach(context);
        if (context instanceof OnOptionsItemSelectedListener) {
            mListener = (OnOptionsItemSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement OnOptionsItemSelectedListener");
        }
    }

    @Override
    public @NonNull Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Log.d(TAG, "onCreateDialog");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.content_sync_dialog_title)
                .setMessage("Would you like to check for content updates and new content?")
                .setIcon(R.drawable.ic_sync)
                .setPositiveButton("Confirm", this)
                .setNegativeButton("Cancel", this)
                .setCancelable(true);

        return builder.create();
    }

    /**
     * This method will be invoked when a button in the dialog is clicked.
     *
     * @param dialog the dialog that received the click
     * @param which  the button that was clicked (ex.
     *               {@link DialogInterface#BUTTON_POSITIVE}) or the position
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Log.d(TAG, "onClick");
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mListener.onConfirmSyncContent();
                dialog.dismiss();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            default:
                break;
        }
        dialog.dismiss();
    }

    public interface OnOptionsItemSelectedListener {
        void onConfirmSyncContent();
    }
}
