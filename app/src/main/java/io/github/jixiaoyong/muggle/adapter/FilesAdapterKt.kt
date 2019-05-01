package io.github.jixiaoyong.muggle.adapter

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.jixiaoyong.muggle.AppApplication
import io.github.jixiaoyong.muggle.Constants
import io.github.jixiaoyong.muggle.FileEntity
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.activity.MainActivity
import io.github.jixiaoyong.muggle.activity.MainActivity.selectRepo
import io.github.jixiaoyong.muggle.activity.MainActivity.userInfo
import io.github.jixiaoyong.muggle.api.bean.Committer
import io.github.jixiaoyong.muggle.api.bean.CreateFileBody
import io.github.jixiaoyong.muggle.api.bean.RepoContent
import io.github.jixiaoyong.muggle.api.bean.UpdateFileBody
import io.github.jixiaoyong.muggle.databinding.ItemFileListDatabindingBinding
import io.github.jixiaoyong.muggle.fragment.EditorFragment
import io.github.jixiaoyong.muggle.utils.FileUtils
import io.github.jixiaoyong.muggle.utils.GitUtils
import io.github.jixiaoyong.muggle.utils.Logger
import io.github.jixiaoyong.muggle.viewmodel.bean.FileListBean
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FilesAdapterKt(entityList: List<FileEntity>?) : RecyclerView.Adapter<FilesAdapterKt.ViewHolder>() {
    private val dataSet = entityList?.toMutableList() ?: arrayListOf()
    private var context: AppCompatActivity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (context == null) {
            context = parent.context as MainActivity
        }
        val dataBinding = DataBindingUtil.inflate<ItemFileListDatabindingBinding>(LayoutInflater.from(context),
                R.layout.item_file_list_databinding, parent, false)
        return ViewHolder(dataBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position >= dataSet.size) {
            return
        }

        val entity = dataSet[position]
        val fileName = entity.name
        val fileListBean = FileListBean(entity.name)

        val content = FileUtils.readContentFromFile(File(entity.absolutePath), false)
        fileListBean.fileContent =
                if (content.isEmpty()) {
                    ""
                } else {
                    if (content.length > 500) content.substring(0, 500) else content

                }

        fileListBean.fileData = DateUtils.getRelativeTimeSpanString(entity.lastModified,
                System.currentTimeMillis(), DateUtils.FORMAT_ABBREV_ALL.toLong()).toString()
        fileListBean.fileState = entity.isSynced

        holder.dataBinding.root.setOnClickListener {
            val fragment = EditorFragment()

            val args = Bundle()
            args.putBoolean(Constants.BUNDLE_KEY_SAVED, true)
            args.putBoolean(Constants.BUNDLE_KEY_FROM_FILE, true)
            args.putString(Constants.BUNDLE_KEY_FILE_NAME,
                    FileUtils.stripExtension(entity.name))
            args.putString(Constants.BUNDLE_KEY_FILE_PATH, entity.absolutePath)

            fragment.arguments = args
            context!!.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
        }

        holder.dataBinding.root.setOnLongClickListener {
            val deleteDialog = AlertDialog.Builder(context!!)
            deleteDialog.setTitle(context!!.getString(R.string.dialog_message_delete_files_confirm))
            deleteDialog.setMessage(context!!.getString(R.string.delete_local_tips))
            deleteDialog.setNegativeButton(R.string.cancel
            ) { dialog, _ -> dialog.cancel() }
            deleteDialog.setPositiveButton(R.string.menu_item_delete
            ) { _, _ ->
                val deleteResult = FileUtils.deleteFile(entity.absolutePath)
                if (deleteResult) {
                    Toast.makeText(context, context!!.getString(R.string.delete_success), Toast.LENGTH_SHORT).show()
                    dataSet.removeAt(position)
                    notifyItemRemoved(position)
                }
            }
            deleteDialog.show()
            true
        }

        holder.dataBinding.fileUpdateGithub.visibility = when (entity.isSynced) {
            0 -> {
                fileListBean.fileType = "已同步"
                holder.dataBinding.fileType.setBackgroundResource(R.drawable.ic_file_synced)
                View.GONE
            }
            -1 -> {
                fileListBean.fileType = "待上传"
                holder.dataBinding.fileUpdateGithub.setBackgroundResource(R.drawable.ic_upload)
                holder.dataBinding.fileType.setBackgroundResource(R.drawable.ic_file_wait_upload)
                View.VISIBLE
            }
            1 -> {
                fileListBean.fileType = "待下载"
                holder.dataBinding.fileUpdateGithub.setBackgroundResource(R.drawable.ic_download)
                holder.dataBinding.fileType.setBackgroundResource(R.drawable.ic_file_to_download)
                View.VISIBLE
            }
            else -> {
                fileListBean.fileType = "本地文件"
                holder.dataBinding.fileUpdateGithub.setBackgroundResource(R.drawable.ic_upload)
                holder.dataBinding.fileType.setBackgroundResource(R.drawable.ic_file_local)
                View.VISIBLE
            }
        }

        if ("" == Constants.token) {
            holder.dataBinding.fileUpdateGithub.visibility = View.GONE
        }

        val githubContent = MainActivity.getGithubRepoConetnt(fileListBean.fileName)

        if (userInfo != null && selectRepo != null
                && holder.dataBinding.fileUpdateGithub.visibility == View.VISIBLE) {
            holder.dataBinding.fileUpdateGithub.setOnClickListener {
                if (githubContent != null) {
                    if (dataSet[position].isSynced == -1) {
                        //本地升级到在线
                        AppApplication.githubApiService.updateFile(selectRepo.owner.login,
                                selectRepo.name, githubContent.path, UpdateFileBody(
                                "update file [$fileName] by Muggle",
                                FileUtils.getByte64EncodeContent(Constants.LOCAL_FILE_PATH + fileName),
                                githubContent.sha,
                                Committer(userInfo.name, userInfo.email)))
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(Schedulers.newThread())
                                .subscribe({ (_, content1) ->
                                    Toast.makeText(holder.itemView.context,
                                            holder.itemView.context.getString(R.string.upgrade_success), Toast.LENGTH_SHORT).show()
                                    Logger.d(content1)
                                    checkVersion()
                                    notifyDataSetChanged()
                                }, { throwable -> Logger.e("error", throwable) })
                    } else if (dataSet[position].isSynced == 1) {
                        //在线升级到本地，覆盖本地
                        val request = Request.Builder().get().url(githubContent.downloadUrl).build()
                        val call = AppApplication.okHttpClient.newCall(request)
                        call.enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                Logger.e("error", e)
                            }

                            @Throws(IOException::class)
                            override fun onResponse(call: Call, response: Response) {
                                try {
                                    context!!.runOnUiThread {
                                        Toast.makeText(context, context!!.getString(R.string.upgrade_success),
                                                Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                                FileUtils.saveFile(Constants.LOCAL_FILE_PATH + githubContent.name,
                                        response.body()!!.byteStream(), true)

                                context!!.runOnUiThread {
                                    checkVersion()
                                    notifyDataSetChanged()
                                }
                            }
                        })
                    }

                } else {
                    //本地新建到在线
                    AppApplication.githubApiService.createNewFile(selectRepo.owner.login,
                            selectRepo.name, fileName, CreateFileBody(
                            "create file [$fileName] by Muggle",
                            FileUtils.getByte64EncodeContent(Constants.LOCAL_FILE_PATH + fileName),
                            Committer(userInfo.name, userInfo.email)))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.newThread())
                            .subscribe({ (_, content1) ->
                                Toast.makeText(holder.itemView.context,
                                        holder.itemView.context.getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
                                Logger.d(content1)

                                MainActivity.selectRepoContent.add(content1)
                                checkVersion()
                                notifyDataSetChanged()
                            }, { throwable -> Logger.e("error", throwable) })
                }
            }
        }

        holder.dataBinding.fileBean = fileListBean

    }

    fun checkVersion() {
        val newDataSet = ArrayList<FileEntity>()
        newDataSet.addAll(dataSet)
        if (newDataSet.size <= 0) {
            return
        }
        for (data in newDataSet) {
            val githubContent = MainActivity.getGithubRepoConetnt(data.name)
            if (githubContent != null) {
                if (githubContent.sha != GitUtils.gitSHA1(data.absolutePath)) {
                    checkLastUpdateTime(githubContent, dataSet.indexOf(data))
                } else {
                    data.isSynced = 0
                    dataSet[dataSet.indexOf(data)] = data
                }
            } else {
                data.isSynced = 2
                dataSet[dataSet.indexOf(data)] = data
            }
        }

    }

    /**
     * @param githubContent
     * @param position
     */
    private fun checkLastUpdateTime(githubContent: RepoContent, position: Int) {
        val entity = dataSet[position]

        val sdf = SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ssZ")
        val formData = sdf.format(Date(entity.lastModified))

        AppApplication.githubApiService.getUserRepoCommit(selectRepo.owner.login,
                selectRepo.name, githubContent.path, formData)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe({ getCommitRespones ->
                    if (getCommitRespones.isNotEmpty()) {
                        Logger.d(githubContent.name + "在线有更新")
                        entity.isSynced = 1
                    } else {
                        Logger.d(githubContent.name + "本地已经是最新的，需要同步到云上")
                        entity.isSynced = -1
                    }
                    dataSet[position] = entity
                    notifyDataSetChanged()
                }, { throwable -> Logger.e("error", throwable) })
    }

    override fun getItemCount(): Int {
        return dataSet.size + 1
    }

    class ViewHolder(val dataBinding: ItemFileListDatabindingBinding)
        : RecyclerView.ViewHolder(dataBinding.root)
}
