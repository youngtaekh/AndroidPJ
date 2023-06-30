package kr.young.androidpj

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.*
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import kr.young.androidpj.databinding.ActivityReceiveBinding
import kr.young.common.TouchEffect
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager
import kr.young.pjsip.observer.PJSIPObserver
import kr.young.pjsip.observer.PJSIPObserverImpl
import org.pjsip.pjsua2.CallInfo

class ReceiveActivity : AppCompatActivity(), PJSIPObserver.Call, OnClickListener, OnTouchListener {
    private lateinit var binding: ActivityReceiveBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receive)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            i(TAG, "setTurnScreenOn")
            setTurnScreenOn(true)
        } else {
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        if (!pm.isInteractive) {
            val wl = pm.newWakeLock(
                SCREEN_BRIGHT_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP or ON_AFTER_RELEASE,
                "AndroidPJ:receiveCallLock"
            )
            wl.acquire(3000) //set your time in milliseconds
        }

        i(TAG, "onCreate")

        PJSIPObserverImpl.instance.add(this)

        binding.tvName.text = CallManager.instance.callModel?.counterpart

        binding.ivEnd.setOnTouchListener(this)
        binding.ivEnd.setOnClickListener(this)
        binding.ivAnswer.setOnTouchListener(this)
        binding.ivAnswer.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PJSIPObserverImpl.instance.remove(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_end -> { CallManager.instance.declineCall() }
            R.id.iv_answer -> { CallManager.instance.answerCall() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TouchEffect.alpha(v!!, event)
        return false
    }

    override fun onOutgoingCall(callInfo: CallInfo) {
        i(TAG, "onOutgoingCall")
    }

    override fun onConnectedCall(callInfo: CallInfo) {
        i(TAG, "onConnectedCall")
        finish()
    }

    override fun onTerminatedCall(callInfo: CallInfo) {
        i(TAG, "onTerminatedCall")
        finish()
    }

    override fun onCallState(callInfo: CallInfo, wholeMsg: String?) {
        i(TAG, "onCallState")
    }

    override fun onCallMediaState() {
        i(TAG, "onCallMediaState")
    }

    companion object {
        private const val TAG = "ReceiveActivity"
    }
}