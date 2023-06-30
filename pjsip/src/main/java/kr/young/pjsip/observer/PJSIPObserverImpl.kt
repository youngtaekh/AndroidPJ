package kr.young.pjsip.observer

import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallEventListener
import kr.young.pjsip.model.MessageInfo
import kr.young.pjsip.model.RegistrationInfo
import org.pjsip.pjsua2.*

class PJSIPObserverImpl private constructor(): PJSIPPublisher {
    private object Holder {
        val INSTANCE = PJSIPObserverImpl()
    }

    companion object {
        private const val TAG = "PJSIPObserverImpl"
        val instance: PJSIPObserverImpl by lazy { Holder.INSTANCE }

//        @JvmStatic fun getInstance(): PJSIPObserverImpl {
//            return instance
//        }
    }

    private var observers: MutableList<PJSIPObserver> = mutableListOf()
    private var registerObservers: MutableList<PJSIPObserver.Register> = mutableListOf()
    private var callObservers: MutableList<PJSIPObserver.Call> = mutableListOf()
    private var messageObservers: MutableList<PJSIPObserver.Message> = mutableListOf()

    override fun add(observer: PJSIPObserver) {
        observers.add(observer)
    }

    override fun remove(observer: PJSIPObserver) {
        observers.remove(observer)
    }

    override fun add(observer: PJSIPObserver.Register) {
        registerObservers.add(observer)
    }

    override fun remove(observer: PJSIPObserver.Register) {
        registerObservers.remove(observer)
    }

    override fun add(observer: PJSIPObserver.Call) {
        callObservers.add(observer)
    }

    override fun remove(observer: PJSIPObserver.Call) {
        callObservers.remove(observer)
    }

    override fun add(observer: PJSIPObserver.Message) {
        messageObservers.add(observer)
    }

    override fun remove(observer: PJSIPObserver.Message) {
        messageObservers.remove(observer)
    }

    override fun onIncomingCallObserver(callInfo: CallInfo) {
        for (observer in this.registerObservers) {
            observer.onIncomingCall(callInfo)
        }
    }

    override fun onRegStartedObserver(onRegStartedParam: OnRegStartedParam?) {
        for (observer in this.registerObservers) {
            observer.onRegStarted(onRegStartedParam)
        }
    }

    override fun onRegistrationSuccessObserver(registrationInfo: RegistrationInfo) {
        for (observer in this.registerObservers) {
            observer.onRegistrationSuccess(registrationInfo)
        }
    }

    override fun onRegistrationFailedObserver(registrationInfo: RegistrationInfo) {
        for (observer in this.registerObservers) {
            observer.onRegistrationFailed(registrationInfo)
        }
    }

    override fun onUnRegistrationSuccessObserver(registrationInfo: RegistrationInfo) {
        for (observer in this.registerObservers) {
            observer.onUnRegistrationSuccess(registrationInfo)
        }
    }

    override fun onUnRegistrationFailedObserver(registrationInfo: RegistrationInfo) {
        for (observer in this.registerObservers) {
            observer.onUnRegistrationFailed(registrationInfo)
        }
    }

    override fun onOutgoingCallObserver(callInfo: CallInfo) {
        for (observer in this.callObservers) {
            observer.onOutgoingCall(callInfo)
        }
    }

    override fun onConnectedCallObserver(callInfo: CallInfo) {
        for (observer in this.callObservers) {
            observer.onConnectedCall(callInfo)
        }
    }

    override fun onTerminatedCallObserver(callInfo: CallInfo) {
        i(TAG, "onTerminatedCallObserver size ${callObservers.size}")
        val cp = mutableListOf <PJSIPObserver.Call>()
        cp.addAll(this.callObservers)
        for (observer in cp) {
            observer.onTerminatedCall(callInfo)
        }
    }

    override fun onCallStateObserver(callInfo: CallInfo, wholeMsg: String?) {
        for (observer in this.callObservers) {
            observer.onCallState(callInfo, wholeMsg)
        }
    }

    override fun onCallMediaStateObserver() {
        for (observer in this.callObservers) {
            observer.onCallMediaState()
        }
    }

    override fun onInstantMessageObserver(messageInfo: MessageInfo) {
        for (observer in this.messageObservers) {
            observer.onInstantMessage(messageInfo)
        }
    }

    override fun onInstantMessageStatusObserver(onInstantMessageStatusParam: OnInstantMessageStatusParam?) {
        for (observer in this.messageObservers) {
            observer.onInstantMessageStatus(onInstantMessageStatusParam)
        }
    }

    override fun onIncomingSubscribeObserver(onIncomingSubscribeParam: OnIncomingSubscribeParam?) {
        for (observer in this.observers) {
            observer.onIncomingSubscribe(onIncomingSubscribeParam)
        }
    }

    override fun onTypingIndicationObserver(onTypingIndicationParam: OnTypingIndicationParam?) {
        for (observer in this.observers) {
            observer.onTypingIndication(onTypingIndicationParam)
        }
    }

    override fun onMwiInfoObserver(onMwiInfoParam: OnMwiInfoParam?) {
        for (observer in this.observers) {
            observer.onMwiInfo(onMwiInfoParam)
        }
    }
}