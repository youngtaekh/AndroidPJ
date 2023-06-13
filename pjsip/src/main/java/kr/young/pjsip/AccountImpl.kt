package kr.young.pjsip

import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.model.CallModel
import kr.young.pjsip.model.MessageInfo
import kr.young.pjsip.model.RegistrationInfo
import kr.young.pjsip.observer.PJSIPObserverImpl
import kr.young.pjsip.util.CustomHeader
import org.pjsip.pjsua2.*
import org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_RINGING

class AccountImpl(
    val accountConfig: AccountConfig,
    private val endpoint: Endpoint
): Account() {
    private val observerImpl: PJSIPObserverImpl = PJSIPObserverImpl.instance
    var buddyImpl: BuddyImpl? = null

    fun addBuddy(buddyConfig: BuddyConfig) {
        buddyImpl = BuddyImpl(buddyConfig)
        buddyImpl!!.create(this, buddyConfig)
        buddyImpl!!.subscribePresence(buddyConfig.subscribe)
    }

    fun deleteBuddy() {
        if (buddyImpl != null) {
            buddyImpl!!.delete()
        }
    }

    fun sendBuddy(msg: String) {
        if (buddyImpl != null) {
            val param = SendInstantMessageParam()
            param.contentType = "text/plain"
            param.content = msg
            buddyImpl!!.sendInstantMessage(param)
        }
    }

    override fun onIncomingCall(prm: OnIncomingCallParam?) {
        i(TAG, "onIncomingCall")
        val call = CallEventListener(this, prm!!.callId, endpoint)
        //Send 180 Ringing
        val callParam = CallOpParam()
        callParam.statusCode = PJSIP_SC_RINGING
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "ringing call"))
        call.answer(callParam)
        CallManager.instance.setCall(call)
        val model = CallModel(call.info.remoteContact)
        model.incoming = true
        CallManager.instance.callModel = model
        observerImpl.onIncomingCallObserver(call.info)
    }

    override fun onRegStarted(prm: OnRegStartedParam?) {
        i(TAG, "onRegStarted")
        observerImpl.onRegStartedObserver(prm)
    }

    override fun onRegState(prm: OnRegStateParam) {
        i(TAG, "onRegState")
        val info = RegistrationInfo(prm.code, prm.status, prm.expiration, prm.reason)
        i(TAG, "onRegState code : " + info.code)
        i(TAG, "onRegState status : " + info.status)
        i(TAG, "onRegState expiration : " + info.expiration)
        i(TAG, "onRegState reason : " + info.reason)
        if (prm.code == 200) {
            if (info.expiration == 0L) {
                CallManager.instance.registrationModel.registered = false
                observerImpl.onUnRegistrationSuccessObserver(info)
            } else {
                CallManager.instance.registrationModel.registered = true
                observerImpl.onRegistrationSuccessObserver(info)
            }
        } else {
            if (info.expiration == 0L) {
                observerImpl.onUnRegistrationFailedObserver(info)
            } else {
                observerImpl.onRegistrationFailedObserver(info)
            }
        }
    }

    override fun onIncomingSubscribe(prm: OnIncomingSubscribeParam?) {
        i(TAG, "onIncomingSubscribe")
        observerImpl.onIncomingSubscribeObserver(prm)
    }

    override fun onInstantMessage(prm: OnInstantMessageParam?) {
        i(TAG, "onInstantMessage")
        val messageInfo = MessageInfo(prm!!.fromUri, prm.msgBody)
        println("======== Incoming pager ======== ")
        println("From     : " + messageInfo.from)
        println("To       : " + prm.toUri)
        println("Contact  : " + prm.contactUri)
        println("MimeType : " + prm.contentType)
        println("Body     : " + messageInfo.message)
        observerImpl.onInstantMessageObserver(messageInfo)
    }

    override fun onInstantMessageStatus(prm: OnInstantMessageStatusParam?) {
        i(TAG, "onInstantMessageStatus")
        observerImpl.onInstantMessageStatusObserver(prm)
    }

    override fun onTypingIndication(prm: OnTypingIndicationParam?) {
        i(TAG, "onTypingIndication")
        observerImpl.onTypingIndicationObserver(prm)
    }

    override fun onMwiInfo(prm: OnMwiInfoParam?) {
        i(TAG, "onMwiInfo")
        observerImpl.onMwiInfoObserver(prm)
    }

    companion object {
        private const val TAG = "AccountImpl"
    }
}