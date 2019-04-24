package io.github.jixiaoyong.muggle.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import io.github.jixiaoyong.muggle.R
import io.github.jixiaoyong.muggle.databinding.ActivityTestBinding
import kotlinx.android.synthetic.main.activity_test.*
import kotlin.concurrent.thread

/**
 * author: jixiaoyong
 * email: jixiaoyong1995@gmail.com
 * website: https://jixiaoyong.github.io
 * date: 2019-04-24
 * description: todo
 */
class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        val binding = DataBindingUtil.setContentView<ActivityTestBinding>(this, R.layout.activity_test)
        // Java代码
        // ActivityViewModelBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_view_model)

        binding.viewModel = userViewModel
        // java代码
        // binding.setViewModel(userViewModel)

        // 让xml内绑定的LiveData和Observer建立连接，也正是因为这段代码，让LiveData能感知Activity的生命周期
        binding.lifecycleOwner = this

        bt_vm.setOnClickListener {
            val user = User("李四", 22, 1)
            thread {
                while (user.age <= 100) {
                    user.age++
                    userViewModel.user.postValue(user)
                    Thread.sleep(1_000)
                }
            }
        }
    }

}

class UserViewModel : ViewModel() {
    val user = MutableLiveData<User>()
}

data class User(val name: String, var age: Int, val sex: Int)

