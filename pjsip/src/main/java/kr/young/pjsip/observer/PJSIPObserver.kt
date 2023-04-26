package kr.young.pjsip.observer

import kr.young.pjsip.CallEventListener
import kr.young.pjsip.model.MessageInfo
import kr.young.pjsip.model.RegistrationInfo
import org.pjsip.pjsua2.*

interface PJSIPObserver {
    interface Register {
        fun onIncomingCall(call: CallEventListener, onIncomingCallParam: OnIncomingCallParam?)
        fun onRegStarted(onRegStartedParam: OnRegStartedParam?)
        fun onRegistrationSuccess(registrationInfo: RegistrationInfo)
        fun onRegistrationFailed(registrationInfo: RegistrationInfo)
        fun onUnRegistrationSuccess(registrationInfo: RegistrationInfo)
        fun onUnRegistrationFailed(registrationInfo: RegistrationInfo)
    }
    interface Call {
        fun onCallState(callInfo: CallInfo, wholeMsg: String?)
        fun onCallMediaState()
    }
    interface Message {
        fun onInstantMessage(messageInfo: MessageInfo)
        fun onInstantMessageStatus(onInstantMessageStatusParam: OnInstantMessageStatusParam?)
    }
    fun onIncomingSubscribe(onIncomingSubscribeParam: OnIncomingSubscribeParam?)
    fun onTypingIndication(onTypingIndicationParam: OnTypingIndicationParam?)
    fun onMwiInfo(onMwiInfoParam: OnMwiInfoParam?)
}