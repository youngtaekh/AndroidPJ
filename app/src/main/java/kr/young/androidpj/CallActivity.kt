package kr.young.androidpj

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import androidx.databinding.DataBindingUtil
import kr.young.androidpj.databinding.ActivityCallBinding
import kr.young.common.TouchEffect
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager
import kr.young.pjsip.observer.PJSIPObserver
import kr.young.pjsip.observer.PJSIPObserverImpl
import org.pjsip.pjsua2.CallInfo

class CallActivity : AppCompatActivity(), OnClickListener, OnTouchListener, PJSIPObserver.Call {
    private lateinit var binding: ActivityCallBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call)

        i(TAG, "onCreate")

        PJSIPObserverImpl.instance.add(this)

        binding.tvName.text = CallManager.instance.callModel?.counterpart
        binding.ivEnd.setOnTouchListener(this)
        binding.ivEnd.setOnClickListener(this)
        binding.ivMute.setOnTouchListener(this)
        binding.ivMute.setOnClickListener(this)
        binding.ivSpeaker.setOnTouchListener(this)
        binding.ivSpeaker.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        i(TAG, "onDestroy")
        PJSIPObserverImpl.instance.remove(this)
    }

    companion object {
        private const val TAG = "CallActivity"
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_end -> { CallManager.instance.endCall() }
            R.id.iv_speaker -> { speaker() }
            R.id.iv_mute -> { mute() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TouchEffect.alpha(v!!, event)
        return false
    }

    override fun onOutgoingCall(callInfo: CallInfo) {}

    override fun onConnectedCall(callInfo: CallInfo) {
        i(TAG, "onConnectedCall")
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTerminatedCall(callInfo: CallInfo) {
        i(TAG, "onTerminatedCall")
        CallManager.instance.callModel!!.speaker = false
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = false

        binding.ivMute.setOnClickListener(null)
        binding.ivMute.setOnTouchListener(null)
        binding.ivSpeaker.setOnClickListener(null)
        binding.ivSpeaker.setOnTouchListener(null)
        finish()
    }

    override fun onCallState(callInfo: CallInfo, wholeMsg: String?) {}

    override fun onCallMediaState() {}

    private fun mute() {
        val manager = CallManager.instance
        val model = manager.callModel!!
        model.mute = !model.mute
        i(TAG, "mute(${model.mute})")
        manager.mute(model.mute)
        if (model.mute) {
            binding.ivMute.setImageResource(R.drawable.round_mute_off_24)
        } else {
            binding.ivMute.setImageResource(R.drawable.round_mute_24)
        }
    }

    private fun speaker() {
        val manager = CallManager.instance
        val model = manager.callModel!!
        model.speaker = !model.speaker
        i(TAG, "speaker(${model.speaker})")
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.isSpeakerphoneOn = model.speaker
        if (model.speaker) {
            binding.ivSpeaker.setImageResource(R.drawable.round_speaker_off_24)
        } else {
            binding.ivSpeaker.setImageResource(R.drawable.round_speaker_24)
        }
    }
}