package io.github.jixiaoyong.muggle.fragment.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import io.github.jixiaoyong.muggle.R

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-27
 * description: todo
 */
abstract class DataBindingBaseFragment<T : ViewDataBinding, V : ViewModel> : Fragment() {

    protected lateinit var dataBinding: T
    protected lateinit var viewModel: V
    protected abstract val viewModelClass: Class<V>
    protected lateinit var compatActivity: AppCompatActivity // compatActivity object
    protected lateinit var toolbar: Toolbar

    var toolbarTitle = ""
    var displayHomeAsUpEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        viewModel = ViewModelProviders.of(this).get(viewModelClass)

        compatActivity = requireActivity() as AppCompatActivity

        initView()

        dataBinding.lifecycleOwner = viewLifecycleOwner
        return dataBinding.root
    }

    protected open fun initView() {
        toolbar = dataBinding.root.findViewById(R.id.toolbar)
        toolbar.title = toolbarTitle
        compatActivity.setSupportActionBar(toolbar)
        compatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(displayHomeAsUpEnabled)
    }

    abstract fun getLayoutId(): Int
}
