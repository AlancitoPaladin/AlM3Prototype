package com.itsm.prototype

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("android:visibility")
    fun setVisibility(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: ImageView, url: String?) {
        // TODO: Use Glide or Coil to load images
        // Glide.with(view.context).load(url).into(view)
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(view: ImageView, resource: Int) {
        view.setImageResource(resource)
    }
}