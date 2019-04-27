package io.github.jixiaoyong.muggle.adapter

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import io.github.jixiaoyong.muggle.R

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
                .placeholder(R.mipmap.ic_launcher_round)
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

@BindingAdapter("setVisible")
fun bindingSetVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}