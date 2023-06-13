package kr.young.androidpj.ui.main

import androidx.lifecycle.ViewModel
import kr.young.common.UtilLog.Companion.d
import kr.young.pjsip.CallManager
import kr.young.pjsip.UserAgent

class MainViewModel : ViewModel() {
    private val callManager = CallManager.instance

    private fun getOutbound(): String {
        return "$OUTBOUND_ADDRESS:$OUTBOUND_PORT"
    }

    private fun getStun(): String {
        return "$STUN_ADDRESS:$STUN_PORT"
    }

    private fun getTurn(): String {
        return "$TURN_ADDRESS:$TURN_PORT"
    }

    fun startRegistration() {
        callManager.startRegistration(
            getOutbound(),
            USER_ID,
            PASSWORD,
            getStun(),
            getTurn(),
            TURN_ID,
            TURN_PASSWORD,
            transport
        )
    }

    fun stopRegistration() {
        callManager.stopRegistration()
    }

    fun refreshRegistration() {
        callManager.onNetworkChanged()
    }

    fun makeCall(counterpart: String) {
        callManager.makeCall(counterpart, SIP_DOMAIN)
    }

    fun answerCall() {
        d(TAG, "answerCall")
        callManager.answerCall()
    }

    fun declineCall() {
        d(TAG, "declineCall")
        callManager.declineCall()
    }

    fun busyCall() {
        d(TAG, "busyCall")
        callManager.busyCall()
    }

    fun ringingCall() {
        callManager.ringingCall()
    }

    fun updateCall() {
        d(TAG, "updateCall")
        callManager.updateCall()
//        callManager.sendRequest()
    }

    fun reInviteCall() {
        d(TAG, "reInviteCall")
        callManager.reInviteCall()
    }

    fun endCall() {
        d(TAG, "endCall")
        callManager.endCall()
    }

    fun addBuddy(id: String) {
        d(TAG, "addBuddy")
        callManager.setBuddy("sip:$id@$SIP_DOMAIN")
    }

    fun deleteBuddy() {
        d(TAG, "deleteBuddy")
//        callManager.deleteBuddy()
        callManager.sendInstanceMessage("Test Message")
    }

    companion object {
        private const val TAG = "MainViewModel"
//        const val OUTBOUND_ADDRESS = "sip:sip.linphone.org"
//        const val OUTBOUND_PORT = "5061"
        const val OUTBOUND_ADDRESS = "sip:hongcafew-pbx.peoplev.net"
        const val OUTBOUND_PORT = "5479"
        const val REGISTRATION_DURATION = "900"
        val transport = UserAgent.TransportType.TLS

        const val USER_ID = "sip:1000004@hongcafew-pbx.peoplev.net"
        const val SIP_DOMAIN = "hongcafew-pbx.peoplev.net"
        const val COUNTERPART = "1000005"
        const val USER_NAME = "everareen"
        const val PASSWORD = "1234"
//        const val USER_ID = "sip:1000005@hongcafew-pbx.peoplev.net"
//        const val SIP_DOMAIN = "sip.linphone.org"
//        const val COUNTERPART = "1000004"
//        const val USER_NAME = "youngtaek.people"
//        const val PASSWORD = "1234"

//        const val USER_ID = "sip:everareen@sip.linphone.org"
//        const val SIP_DOMAIN = "sip.linphone.org"
//        const val COUNTERPART = "youngtaek.people"
//        const val USER_NAME = "everareen"
//        const val PASSWORD = "lidue638"
//        const val USER_ID = "sip:youngtaek.people@sip.linphone.org"
//        const val SIP_DOMAIN = "sip.linphone.org"
//        const val COUNTERPART = "everareen"
//        const val USER_NAME = "youngtaek.people"
//        const val PASSWORD = "lidue638"

        const val STUN_ADDRESS = "stun.linphone.org"
        const val STUN_PORT = "3478"
        const val TURN_ADDRESS = "numb.viagenie.ca"
        const val TURN_PORT = "80"
        const val TURN_ID = "webrtc@live.com"
        const val TURN_PASSWORD = "muazkh"
    }
}