package kr.young.pjsip

import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.model.RegistrationInfo
import kr.young.pjsip.observer.PJSIPObserver
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnRegStartedParam

class Register private constructor() {

    private var userAgent = UserAgent.instance

    fun release() {
        userAgent.release()
    }

    fun start(
        outboundProxyAddress: String,
        userId: String,
        userPassword: String,
        stunServer: String,
        turnServer: String,
        turnUserName: String,
        turnPassword: String,
        type: UserAgent.TransportType
    ) {
        i(TAG, "start()")

        userAgent.init(
            outboundProxyAddress = outboundProxyAddress,
            userId = userId,
            userPassword = userPassword,
            stunServer = stunServer,
            turnServer = turnServer,
            turnUserName = turnUserName,
            turnPassword = turnPassword,
            type = type
        )

        userAgent.start()
    }

    fun stop() {
        i(TAG, "stop()")
        userAgent.stop()
    }

    private object Holder {
        val INSTANCE = Register()
    }

    companion object {
        private const val TAG = "Register"
        val instance: Register by lazy { Holder.INSTANCE }
    }
}