package io.github.jixiaoyong.muggle.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-21
 * description: todo
 */
@BindingAdapter("imageFromUrl")
fun bindingImageFromUrl(imageView: ImageView, imageUrl: String?) {
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(imageView.context)
                .load(imageUrl)
                .into(imageView)
    }
}

@BindingAdapter("onRefreshListener")
fun bindingOnRefreshListener(swipe: SwipeRefreshLayout, listener: SwipeRefreshLayout.OnRefreshListener?) {
    swipe.setOnRefreshListener(listener)
}

@BindingAdapter("refreshing")
fun setRefreshing(view: SwipeRefreshLayout, refreshing: Boolean) {
    if (refreshing != view.isRefreshing) {
        view.isRefreshing = refreshing
    }
}