package kr.young.androidpj.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kr.young.androidpj.CallService
import kr.young.androidpj.R
import kr.young.androidpj.ReceiveActivity
import kr.young.androidpj.databinding.FragmentMainBinding
import kr.young.common.TouchEffect
import kr.young.common.UtilLog.Companion.d
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager
import kr.young.pjsip.model.MessageInfo
import kr.young.pjsip.model.RegistrationInfo
import kr.young.pjsip.observer.PJSIPObserver
import kr.young.pjsip.observer.PJSIPObserverImpl
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.OnInstantMessageStatusParam
import org.pjsip.pjsua2.OnRegStartedParam

class MainFragment: Fragment(),
    OnClickListener,
    OnTouchListener,
    PJSIPObserver.Register,
    PJSIPObserver.Message,
    PJSIPObserver.Call {

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        // TODO: Use the ViewModel
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        i(TAG, "onCreateView")

        binding = FragmentMainBinding.inflate(LayoutInflater.from(container!!.context), container, false)

        binding.tvRegister.setOnClickListener(this)
        binding.tvRegister.setOnTouchListener(this)
        binding.tvUnregister.setOnClickListener(this)
        binding.tvUnregister.setOnTouchListener(this)
        binding.tvRefresh.setOnClickListener(this)
        binding.tvRefresh.setOnTouchListener(this)
        binding.ivCall.setOnClickListener(this)
        binding.ivCall.setOnTouchListener(this)
        binding.ivMessage.setOnClickListener(this)
        binding.ivMessage.setOnTouchListener(this)
        binding.tvAccept.setOnClickListener(this)
        binding.tvAccept.setOnTouchListener(this)
        binding.tvDecline.setOnClickListener(this)
        binding.tvDecline.setOnTouchListener(this)
        binding.tvBusy.setOnClickListener(this)
        binding.tvBusy.setOnTouchListener(this)
        binding.tvRinging.setOnClickListener(this)
        binding.tvRinging.setOnTouchListener(this)
        binding.tvUpdate.setOnClickListener(this)
        binding.tvUpdate.setOnTouchListener(this)
        binding.tvReInvite.setOnClickListener(this)
        binding.tvReInvite.setOnTouchListener(this)
        binding.tvEnd.setOnClickListener(this)
        binding.tvEnd.setOnTouchListener(this)
        binding.tvAdd.setOnClickListener(this)
        binding.tvAdd.setOnTouchListener(this)
        binding.tvDel.setOnClickListener(this)
        binding.tvDel.setOnTouchListener(this)
        binding.tvMute.setOnClickListener(this)
        binding.tvMute.setOnTouchListener(this)
        binding.tvSpeaker.setOnClickListener(this)
        binding.tvSpeaker.setOnTouchListener(this)

        binding.clCall.visibility = GONE
        binding.etCounterpart.setText(MainViewModel.COUNTERPART)

        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Message)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Call)

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewToggle(CallManager.instance.registrationModel.registered)
        if (CallManager.instance.registrationModel.registered) {
            callView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Message)
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Call)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_register -> { viewModel.startRegistration() }
            R.id.tv_unregister -> { viewModel.stopRegistration() }
            R.id.tv_refresh -> { viewModel.refreshRegistration() }
            R.id.iv_call -> { startCall() }
            R.id.iv_message -> { viewModel.sendMessage("Test Msg") }
            R.id.tv_accept -> { viewModel.answerCall() }
            R.id.tv_decline -> { viewModel.declineCall() }
            R.id.tv_busy -> { viewModel.busyCall() }
            R.id.tv_ringing -> { viewModel.ringingCall() }
            R.id.tv_update -> { viewModel.updateCall() }
            R.id.tv_re_invite -> { viewModel.reInviteCall() }
            R.id.tv_end -> { viewModel.endCall() }
            R.id.tv_add -> { viewModel.addBuddy(binding.etCounterpart.text.toString()) }
            R.id.tv_del -> { viewModel.deleteBuddy() }
            R.id.tv_mute -> { mute() }
            R.id.tv_speaker -> { speaker() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TouchEffect.alpha(v!!, event)
        return false
    }

    override fun onIncomingCall(callInfo: CallInfo) {
        d(TAG, "onIncomingCall")
        requireActivity().runOnUiThread {
            viewToggle(isRegister = true)
            callView()
        }
        val intent = Intent(context, ReceiveActivity::class.java)
        intent.flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun onRegStarted(onRegStartedParam: OnRegStartedParam?) {
        d(TAG, "onRegStarted($onRegStartedParam)")
    }

    override fun onRegistrationSuccess(registrationInfo: RegistrationInfo) {
        d(TAG, "onRegistrationSuccess($registrationInfo)")
        requireActivity().runOnUiThread { viewToggle(isRegister = true) }
    }

    override fun onRegistrationFailed(registrationInfo: RegistrationInfo) {
        d(TAG, "onRegistrationFailed($registrationInfo)")
//        networkUtil.release()
        requireActivity().runOnUiThread { viewToggle(isRegister = false) }
    }

    override fun onUnRegistrationSuccess(registrationInfo: RegistrationInfo) {
        d(TAG, "onUnRegistrationSuccess($registrationInfo)")
//        networkUtil.release()
        requireActivity().runOnUiThread { viewToggle(isRegister = false) }
    }

    override fun onUnRegistrationFailed(registrationInfo: RegistrationInfo) {
        d(TAG, "onUnRegistrationFailed($registrationInfo)")
    }

    override fun onOutgoingCall(callInfo: CallInfo) {
        d(TAG, "onOutgoingCall")
        requireActivity().runOnUiThread { callView() }
    }

    override fun onConnectedCall(callInfo: CallInfo) {
        d(TAG, "onConnectedCall")
        requireActivity().runOnUiThread { callView() }
    }

    override fun onTerminatedCall(callInfo: CallInfo) {
        d(TAG, "onTerminatedCall")
        requireActivity().runOnUiThread { callView() }
    }

    override fun onCallState(callInfo: CallInfo, wholeMsg: String?) {
        d(TAG, "onCallState($callInfo)")
    }

    override fun onCallMediaState() {
        d(TAG, "onCallMediaState")
    }

    override fun onInstantMessage(messageInfo: MessageInfo) {
        d(TAG, "onInstantMessage($messageInfo)")
        requireActivity().runOnUiThread {
            binding.tvBuddy.text = messageInfo.message
        }
    }

    override fun onInstantMessageStatus(onInstantMessageStatusParam: OnInstantMessageStatusParam?) {
        d(TAG, "onInstantMessageStatus($onInstantMessageStatusParam)")
    }

    private fun startCall() {
        viewModel.makeCall(binding.etCounterpart.text.toString())
        requireContext().startForegroundService(Intent(requireContext(), CallService::class.java))
    }

    private fun mute() {
        if (viewModel.mute()) {
            binding.tvMute.setText(R.string.mute_off)
        } else {
            binding.tvMute.setText(R.string.mute_on)
        }
    }

    private fun speaker() {
        if (viewModel.speaker()) {
            binding.tvSpeaker.setText(R.string.speaker_off)
        } else {
            binding.tvSpeaker.setText(R.string.speaker_on)
        }
    }

    private fun viewToggle(isRegister: Boolean) {
        if (isRegister) {
            binding.tvRegister.visibility = GONE
            binding.tvUnregister.visibility = VISIBLE
            binding.tvRefresh.visibility = VISIBLE
            binding.ivCall.visibility = VISIBLE
            binding.ivMessage.visibility = VISIBLE
        } else {
            binding.tvRegister.visibility = VISIBLE
            binding.tvUnregister.visibility = GONE
            binding.tvRefresh.visibility = GONE
            binding.ivCall.visibility = GONE
            binding.ivMessage.visibility = GONE
        }
    }

    private fun callView() {
        val terminated = CallManager.instance.callModel?.terminated != false
        val connected = !terminated && CallManager.instance.callModel?.connected == true
        val outgoing = !terminated && !connected && CallManager.instance.callModel?.outgoing == true
        val incoming = !terminated && !connected && CallManager.instance.callModel?.incoming == true

        i(TAG, "terminated $terminated")
        i(TAG, "connected $connected")
        i(TAG, "outgoing $outgoing")
        i(TAG, "incoming $incoming")

        binding.clCall.visibility = if (terminated) GONE else VISIBLE
        binding.ivCall.visibility = if (terminated) VISIBLE else GONE
        binding.llIncoming.visibility = if (incoming) VISIBLE else GONE
        binding.llOutgoing.visibility = if (outgoing || connected) VISIBLE else GONE
        binding.tvUpdate.visibility = if (outgoing || connected) VISIBLE else GONE
        binding.tvReInvite.visibility = if (connected) VISIBLE else GONE
        binding.llMedia.visibility = if (outgoing || connected) VISIBLE else GONE
        binding.tvEnd.visibility = if (outgoing || connected) VISIBLE else GONE
        binding.tvEnd.setText(if (connected) R.string.end else R.string.cancel)
        if (terminated) {
            binding.tvMute.setText(R.string.mute_on)
            viewModel.speaker(false)
            binding.tvSpeaker.setText(R.string.speaker_on)
        }
    }

    companion object {
        private const val TAG = "MainFragment"
        fun newInstance() = MainFragment()
    }
}