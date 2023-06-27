package kr.young.androidpj

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kr.young.androidpj.databinding.ActivityMainBinding
import kr.young.androidpj.ui.main.MainFragment
import kr.young.common.UtilLog.Companion.i
import kr.young.common.UtilLog.Companion.w

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

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            i(TAG, "token is ${task.result}")
        })
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}