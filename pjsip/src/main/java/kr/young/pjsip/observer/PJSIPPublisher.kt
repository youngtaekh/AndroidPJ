package kr.young.pjsip.observer

import kr.young.pjsip.CallEventListener
import kr.young.pjsip.model.MessageInfo
import kr.young.pjsip.model.RegistrationInfo
import org.pjsip.pjsua2.*

interface PJSIPPublisher {
    fun add(observer: PJSIPObserver)
    fun remove(observer: PJSIPObserver)
    fun add(observer: PJSIPObserver.Register)
    fun remove(observer: PJSIPObserver.Register)
    fun add(observer: PJSIPObserver.Call)
    fun remove(observer: PJSIPObserver.Call)
    fun add(observer: PJSIPObserver.Message)
    fun remove(observer: PJSIPObserver.Message)

    fun onIncomingCallObserver(callInfo: CallInfo)
    fun onRegStartedObserver(onRegStartedParam: OnRegStartedParam?)
    fun onRegistrationSuccessObserver(registrationInfo: RegistrationInfo)
    fun onRegistrationFailedObserver(registrationInfo: RegistrationInfo)
    fun onUnRegistrationSuccessObserver(registrationInfo: RegistrationInfo)
    fun onUnRegistrationFailedObserver(registrationInfo: RegistrationInfo)

    fun onOutgoingCallObserver(callInfo: CallInfo)
    fun onConnectedCallObserver(callInfo: CallInfo)
    fun onTerminatedCallObserver(callInfo: CallInfo)
    fun onCallStateObserver(callInfo: CallInfo, wholeMsg: String?)
    fun onCallMediaStateObserver()

    fun onInstantMessageObserver(messageInfo: MessageInfo)
    fun onInstantMessageStatusObserver(onInstantMessageStatusParam: OnInstantMessageStatusParam?)

    fun onIncomingSubscribeObserver(onIncomingSubscribeParam: OnIncomingSubscribeParam?)
    fun onTypingIndicationObserver(onTypingIndicationParam: OnTypingIndicationParam?)
    fun onMwiInfoObserver(onMwiInfoParam: OnMwiInfoParam?)
}