package kr.young.pjsip

import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.model.RegistrationInfo
import kr.young.pjsip.observer.PJSIPObserver
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnRegStartedParam

class Register private constructor(): PJSIPObserver.Register {
    private object Holder {
        val INSTANCE = Register()
    }

    var userAgent: UserAgent? = null
    var accountImpl: AccountImpl? = null
    var endPointImpl: EndPointImpl? = null

    fun init(outboundProxyAddress: String,
             userId: String,
             password: String,
             stunAddress: String,
             turnAddress: String,
             turnUserName: String,
             turnPassword: String,
             type: UserAgent.TransportType
    ) {
        i(TAG, "init()")
        System.loadLibrary("pjsua2")

        userAgent = UserAgent.instance
        userAgent!!.init(
            outboundProxyAddress = outboundProxyAddress,
            userId = userId,
            userPassword = password,
            stunServer = stunAddress,
            turnServer = turnAddress,
            turnUserName = turnUserName,
            turnPassword = turnPassword,
            type = type)

        endPointImpl = userAgent!!.endPointImpl
    }

    fun release() {
        i(TAG, "release()")
        userAgent!!.release()
        userAgent = null
    }

    fun start() {
        i(TAG, "start()")
        userAgent!!.start()

        accountImpl = userAgent!!.accountImpl
    }

    fun stop() {
        i(TAG, "stop()")
        userAgent!!.stop()
    }

    companion object {
        private const val TAG = "Register"
        val instance: Register by lazy { Holder.INSTANCE }
    }

    override fun onIncomingCall(
        call: CallEventListener,
        onIncomingCallParam: OnIncomingCallParam?
    ) {

    }

    override fun onRegStarted(onRegStartedParam: OnRegStartedParam?) {
        TODO("Not yet implemented")
    }

    override fun onRegistrationSuccess(registrationInfo: RegistrationInfo) {
        TODO("Not yet implemented")
    }

    override fun onRegistrationFailed(registrationInfo: RegistrationInfo) {
        TODO("Not yet implemented")
    }

    override fun onUnRegistrationSuccess(registrationInfo: RegistrationInfo) {
        TODO("Not yet implemented")
    }

    override fun onUnRegistrationFailed(registrationInfo: RegistrationInfo) {
        TODO("Not yet implemented")
    }
}