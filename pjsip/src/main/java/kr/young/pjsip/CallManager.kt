package kr.young.pjsip

import kr.young.common.UtilLog
import kr.young.common.UtilLog.Companion.d
import kr.young.pjsip.model.CallModel
import kr.young.pjsip.model.RegistrationModel
import kr.young.pjsip.util.CustomHeader
import org.pjsip.pjsua2.*
import org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_OK

class CallManager private constructor() {

    private var call: CallEventListener? = null
    var callModel: CallModel? = null

    private var register = Register.instance
    private var userAgent = UserAgent.instance
    var registrationModel = RegistrationModel()

    private var buddy: BuddyImpl? = null

    fun release() {
        register.release()
    }

    fun startRegistration(
        outboundProxyAddress: String,
        userId: String,
        password: String,
        stunAddress: String,
        turnAddress: String,
        turnUserName: String,
        turnPassword: String,
        type: UserAgent.TransportType
    ) {
        d(TAG, "startRegistration")

        register.start(
            outboundProxyAddress = outboundProxyAddress,
            userId = userId,
            userPassword = password,
            stunServer = stunAddress,
            turnServer = turnAddress,
            turnUserName = turnUserName,
            turnPassword = turnPassword,
            type = type
        )
    }

    fun stopRegistration() {
        register.stop()
    }

    fun makeCall(counterpart: String, sipDomain: String) {
        callModel = CallModel(counterpart)
        call = CallEventListener(userAgent.accountImpl!!, -1, userAgent.endPointImpl!!)
        val callParam = CallOpParam(true)
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "make call"))
        call?.makeCall("sip:$counterpart@$sipDomain", callParam)
    }

    fun answerCall() {
        val callParam = CallOpParam()
        callParam.statusCode = PJSIP_SC_OK
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "answer call"))
        call?.answer(callParam)
    }

    fun busyCall() {
        val callParam = CallOpParam()
        callParam.statusCode = pjsip_status_code.PJSIP_SC_BUSY_EVERYWHERE
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "busy call"))
        call?.hangup(callParam)
    }

    fun ringingCall() {
        val callParam = CallOpParam()
        callParam.statusCode = pjsip_status_code.PJSIP_SC_RINGING
        call?.answer(callParam)
    }

    fun declineCall() {
        val callParam = CallOpParam()
        callParam.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "decline call"))
        call?.hangup(callParam)
    }

    fun updateCall() {
        val param = CallOpParam(true)
        val sdp = SdpSession()
        sdp.wholeSdp = ""
        param.sdp.wholeSdp = ""
        param.txOption.msgBody = ""
        d(TAG, "sdp ${param.sdp.wholeSdp}")
        d(TAG, "msgBody ${param.txOption.msgBody}")
        call?.update(param)
    }

    fun reInviteCall() {
        val callParam = CallOpParam(true)
        //No work
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "reInvite call"))
        call?.reinvite(callParam)
    }

    fun sendRequest() {
        val requestParam = CallSendRequestParam()
        requestParam.method = "INFO"
        call?.sendRequest(requestParam)
    }

    fun endCall() {
        val callParam = CallOpParam()
        callParam.txOption.headers = SipHeaderVector(arrayOf())
        callParam.txOption.headers.add(CustomHeader.make("Custom-Header", "hangup call"))
        call?.hangup(callParam)
    }

    fun onNetworkChanged() {
        if (!registrationModel.registered) return

        userAgent.onNetworkChanged()

        if (callModel != null) {
            UtilLog.i(TAG, "outgoing - ${instance.callModel!!.outgoing}")
            UtilLog.i(TAG, "incoming - ${instance.callModel!!.incoming}")
            UtilLog.i(TAG, "connected - ${instance.callModel!!.connected}")
            UtilLog.i(TAG, "terminated - ${instance.callModel!!.terminated}")
            if (
                !callModel!!.outgoing &&
                callModel!!.incoming &&
                !callModel!!.connected &&
                !callModel!!.terminated
            ) {
                ringingCall()
            }
        }
    }

    fun setCall(call: CallEventListener?) {
        this.call = call
    }

    fun getCall(): CallEventListener? {
        return this.call
    }

    fun setBuddy(uri: String, isSub: Boolean = false) {
        val buddyConfig = BuddyConfig()
        buddyConfig.uri = uri
        buddyConfig.subscribe = isSub

        userAgent.accountImpl!!.addBuddy(buddyConfig)
    }

    fun deleteBuddy() {
        userAgent.accountImpl!!.deleteBuddy()
    }

    fun sendInstanceMessage(msg: String) {
        userAgent.accountImpl!!.sendBuddy(msg)
    }

    init {
        System.loadLibrary("pjsua2")
    }

    private object Holder {
        val INSTANCE = CallManager()
    }

    companion object {
        private const val TAG = "CallManager"
        val instance: CallManager by lazy { Holder.INSTANCE }
    }
}