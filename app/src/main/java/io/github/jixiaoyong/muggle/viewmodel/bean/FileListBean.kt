package io.github.jixiaoyong.muggle.viewmodel.bean

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-27
 * description: todo
 */
data class FileListBean(var fileName: String = "", var fileContent: String = "", var fileType: String = "",
                        var fileState: Int = 0, var syncToCloud: Boolean = false, var fileData: String = "")