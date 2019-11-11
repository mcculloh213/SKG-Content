package com.edutechnologic.industrialbadger.content;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by H.D. "Chip" McCullough on 12/3/2018.
 */
public abstract class AbstractViewerActivity extends AppCompatActivity {
    private static final String TAG = AbstractViewerActivity.class.getSimpleName();

    public static final String ARG_ABSOLUTE_PATH = "com.industrialbadger.content.file#ARG_FILE_ABSOLUTE_PATH";
    public static final String ARG_FILENAME = "com.industrialbadger.content.file#ARG_FILENAME";
}
