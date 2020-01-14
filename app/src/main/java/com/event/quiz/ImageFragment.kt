package com.event.quiz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment


private const val ARG_PATH = "filepath"
class ImageFragment : DialogFragment() {

    private lateinit var imageView: ImageView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        imageView = ImageView(activity)
        val path = arguments?.getSerializable(ARG_PATH) as String
        Log.d("QUERY", "path: $path")
        if(path.isNotBlank()){
            val bitmap = getScaledBitmap(path, requireActivity())
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.setImageDrawable(null)
        }
        return imageView
    }

    companion object {
        fun newInstance(filePath: String): ImageFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PATH, filePath)
            }
            return ImageFragment().apply {
                arguments = args
                setStyle(STYLE_NO_TITLE, 0)
            }
        }
    }
}