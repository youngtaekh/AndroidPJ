package kr.young.androidpj.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kr.young.androidpj.R
import kr.young.androidpj.databinding.FragmentMainBinding
import kr.young.androidpj.util.NetworkUtil
import kr.young.common.TouchEffect
import kr.young.common.UtilLog.Companion.d
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

    private lateinit var networkUtil: NetworkUtil

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

        binding.etCounterpart.setText(MainViewModel.COUNTERPART)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        networkUtil = NetworkUtil(requireContext())
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Message)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Call)
    }

    override fun onPause() {
        super.onPause()
        networkUtil.release()
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Message)
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Call)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_register -> { viewModel.startRegistration() }
            R.id.tv_unregister -> { viewModel.stopRegistration() }
            R.id.tv_refresh -> { viewModel.refreshRegistration() }
            R.id.iv_call -> { viewModel.makeCall(binding.etCounterpart.text.toString()) }
            R.id.tv_accept -> { viewModel.answerCall() }
            R.id.tv_decline -> { viewModel.declineCall() }
            R.id.tv_busy -> { viewModel.busyCall() }
            R.id.tv_ringing -> { viewModel.ringingCall() }
            R.id.tv_update -> { viewModel.updateCall() }
            R.id.tv_re_invite -> { viewModel.reInviteCall() }
            R.id.tv_end -> { viewModel.endCall() }
            R.id.tv_add -> { viewModel.addBuddy(binding.etCounterpart.text.toString()) }
            R.id.tv_del -> { viewModel.deleteBuddy() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        TouchEffect.alpha(v!!, event)
        return false
    }

    override fun onIncomingCall(callInfo: CallInfo) {
        d(TAG, "onIncomingCall")
        requireActivity().runOnUiThread { viewToggle(isRegister = true) }
        requireActivity().runOnUiThread {
            binding.clCall.visibility = VISIBLE
            binding.tvCounterpart.text = callInfo.remoteContact
            binding.tvAccept.visibility = VISIBLE
            binding.tvDecline.visibility = VISIBLE
            binding.tvBusy.visibility = VISIBLE
            binding.tvRinging.visibility = VISIBLE
            binding.tvUpdate.visibility = GONE
            binding.tvReInvite.visibility = GONE
            binding.tvEnd.visibility = GONE
        }
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
        requireActivity().runOnUiThread {
            binding.clCall.visibility = VISIBLE
            binding.tvCounterpart.text = callInfo.remoteContact
            binding.tvAccept.visibility = GONE
            binding.tvDecline.visibility = GONE
            binding.tvBusy.visibility = GONE
            binding.tvRinging.visibility = GONE
            binding.tvUpdate.visibility = VISIBLE
            binding.tvReInvite.visibility = GONE
            binding.tvEnd.setText(R.string.cancel)
            binding.tvEnd.visibility = VISIBLE
        }
    }

    override fun onConnectedCall(callInfo: CallInfo) {
        d(TAG, "onConnectedCall")
        requireActivity().runOnUiThread {
            binding.clCall.visibility = VISIBLE
            binding.tvCounterpart.text = callInfo.remoteContact
            binding.tvAccept.visibility = GONE
            binding.tvDecline.visibility = GONE
            binding.tvBusy.visibility = GONE
            binding.tvRinging.visibility = GONE
            binding.tvUpdate.visibility = VISIBLE
            binding.tvReInvite.visibility = VISIBLE
            binding.tvEnd.setText(R.string.end)
            binding.tvEnd.visibility = VISIBLE
        }
    }

    override fun onTerminatedCall(callInfo: CallInfo) {
        d(TAG, "onTerminatedCall")
        requireActivity().runOnUiThread {
            binding.clCall.visibility = GONE
        }
    }

    override fun onCallState(callInfo: CallInfo, wholeMsg: String?) {
        d(TAG, "onCallState($callInfo)")
    }

    override fun onCallMediaState() {
        d(TAG, "onCallMediaState")
    }

    override fun onInstantMessage(messageInfo: MessageInfo) {
        d(TAG, "onInstantMessage($messageInfo)")
    }

    override fun onInstantMessageStatus(onInstantMessageStatusParam: OnInstantMessageStatusParam?) {
        d(TAG, "onInstantMessageStatus($onInstantMessageStatusParam)")
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

    companion object {
        private const val TAG = "MainFragment"
        fun newInstance() = MainFragment()
    }
}