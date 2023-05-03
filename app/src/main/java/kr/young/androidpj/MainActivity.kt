package kr.young.androidpj

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import kr.young.androidpj.databinding.ActivityMainBinding
import kr.young.androidpj.ui.main.MainFragment

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}