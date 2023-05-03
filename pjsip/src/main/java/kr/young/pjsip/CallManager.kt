package kr.young.pjsip

import kr.young.common.UtilLog.Companion.d
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.SdpSession
import org.pjsip.pjsua2.pjsip_status_code
import org.pjsip.pjsua2.pjsip_status_code.PJSIP_SC_OK
import org.pjsip.pjsua2.pjsua_call_flag.PJSUA_CALL_NO_SDP_OFFER
import kotlin.reflect.typeOf

class CallManager private constructor() {

    private var call: CallEventListener? = null

    private var register = Register.instance
    private var userAgent = UserAgent.instance

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

    fun makeCall(counterpart: String) {
        call = CallEventListener(userAgent.accountImpl!!, -1, userAgent.endPointImpl!!)
        val callParam = CallOpParam(true)
        call?.makeCall("sip:$counterpart@sip.linphone.org", callParam)
    }

    fun answerCall() {
        val callParam = CallOpParam()
        callParam.statusCode = PJSIP_SC_OK
        call?.answer(callParam)
    }

    fun busyCall() {
        val callParam = CallOpParam()
        callParam.statusCode = pjsip_status_code.PJSIP_SC_BUSY_EVERYWHERE
        call?.hangup(callParam)
    }

    fun declineCall() {
        val callParam = CallOpParam()
        callParam.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
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
        call?.reinvite(CallOpParam(true))
    }

    fun endCall() {
        val callParam = CallOpParam()
        call?.hangup(callParam)
    }

    fun setCall(call: CallEventListener?) {
        this.call = call
    }

    fun getCall(): CallEventListener? {
        return this.call
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