package com.edutechnologic.industrialbadger.content.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.edutechnologic.industrialbadger.base.fragment.BaseFragment
import com.edutechnologic.industrialbadger.content.R
import kotlinx.android.synthetic.main.fragment_media.*

class MediaFragment : BaseFragment() {
    companion object {
        @JvmField val ARG_PATH: String = "com.industrialbadger.media.arg.PATH"
        @JvmStatic fun newInstance(path: String): MediaFragment {
            return MediaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PATH, path)
                }
            }
        }
    }

    private var path: String = ""
    private val controller: MediaController by lazy {
        MediaController(context)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.apply {
            onHandleArguments(this)
        }
    }

    override fun onHandleArguments(args: Bundle) {
        super.onHandleArguments(args)
        path = args.getString(ARG_PATH) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            onRestoreInstanceState(this)
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        path = state.getString(ARG_PATH)
                ?: arguments?.getString(ARG_PATH)
                        ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mBaseListener.setTitle("Media Player")
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onStart() {
        super.onStart()
        controller.setAnchorView(video_surface)
        video_surface.apply {
            setVideoPath("https://s3.us-east-2.amazonaws.com/edutechnologic.blob/media/HC10+Collaborative+Robot.mp4")
            setMediaController(controller)
            setOnPreparedListener {
                frame_scrim.visibility = View.GONE
                start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        video_surface.apply {
            if (isPlaying && canPause()) {
                pause()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_PATH, path)
    }

    override fun onDetach() {
        super.onDetach()
    }
}