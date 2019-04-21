package io.github.jixiaoyong.muggle.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.api.bean.Repo

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-21
 * description: todo
 */
class ReposListAdapter(private val listener: (position: Int) -> Unit) : ListAdapter<Repo, ReposListAdapter.VH>(ReposDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_github_repos, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val repo = getItem(position)
        holder.textView.text = repo.name
        holder.itemView.setOnClickListener {
            listener.invoke(position)
        }

        holder.repoIcon.setBackgroundResource(if (repo.private) R.drawable.ic_lock else R.drawable.ic_open)
    }

    class VH(val view: View)
        : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textview)
        val repoIcon: ImageView = view.findViewById(R.id.repo_icon)
    }
}

class ReposDiffCallback : DiffUtil.ItemCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

}