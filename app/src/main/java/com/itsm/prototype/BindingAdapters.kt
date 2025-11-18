package com.itsm.prototype

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, url: String?) {
        if (!url.isNullOrEmpty()) {
            Glide.with(view.context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(view: ImageView, resource: Int) {
        view.setImageResource(resource)
    }
}