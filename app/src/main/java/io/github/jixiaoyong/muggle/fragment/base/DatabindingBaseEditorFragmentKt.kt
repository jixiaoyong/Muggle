package io.github.jixiaoyong.muggle.fragment.base

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.task.SaveFileTask

abstract class DatabindingBaseEditorFragmentKt<T : ViewDataBinding, V : ViewModel> : Fragment() {

    protected lateinit var dataBinding: T
    protected lateinit var viewModel: V
    protected abstract val viewModelClass: Class<V>
    protected lateinit var mContext: AppCompatActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootPath = rootPath.replace("\${app_name}", getString(R.string.app_name))
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        viewModel = ViewModelProviders.of(this).get(viewModelClass)

        val view = dataBinding.root
        mContext = requireActivity() as AppCompatActivity
        initView()
        dataBinding.lifecycleOwner = viewLifecycleOwner
        return view
    }

    protected open fun initView() {
        if (!fromFile) {
            isFileSaved = false
            fileName = null
            filePath = null
            fileContent = ""
            currentContent = ""
        }
    }

    @JvmOverloads
    protected fun showSaveFileDialog(forceRewrite: Boolean, goBack: Boolean = false) {
        val saveDialog = AlertDialog.Builder(mContext)
        saveDialog.setTitle(R.string.dialog_title_save_file)

        val inflater = mContext.layoutInflater
        val view = inflater.inflate(R.layout.dialog_save_file, null)
        val fileNameET = view.findViewById<EditText>(R.id.file_name)

        saveDialog.setView(view)
        saveDialog.setNeutralButton(R.string.dialog_btn_discard) { dialog, which ->
            isFileSaved = true
            isContentChanged = false
            requireActivity().onBackPressed()
        }
        saveDialog.setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
        saveDialog.setPositiveButton(R.string.dialog_btn_save
        ) { dialog, which ->
            fileName = fileNameET.text.toString()
            filePath = "$rootPath$fileName.md"
            SaveFileTask(mContext, filePath, fileName,
                    currentContent, forceRewrite, SaveFileTask.Response { result ->
                // change isFileSaved value to true if save success
                isFileSaved = result!!
                isContentChanged = !result
                if (goBack) {
                    try {
                        val activity = requireActivity()
                        activity?.onBackPressed()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }).execute()
        }

        saveDialog.show()
    }

    abstract fun getLayoutId(): Int;

    companion object {
        @JvmStatic
        protected var isFileSaved = false
        @JvmStatic
        protected var isContentChanged = false
        @JvmStatic
        protected var fromFile = false
        @JvmStatic
        protected var fileName: String? = null
        @JvmStatic
        protected var filePath: String? = null
        @JvmStatic
        protected var fileContent: String? = null
        @JvmStatic
        protected var currentContent: String? = null
        @JvmStatic
        protected var rootPath = Environment.getExternalStorageDirectory().toString() + "/\${app_name}/"
    }
}
