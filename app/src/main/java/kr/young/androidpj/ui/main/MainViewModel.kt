package kr.young.androidpj.ui.main

import android.content.Context.AUDIO_SERVICE
import android.media.AudioManager
import androidx.lifecycle.ViewModel
import kr.young.common.ApplicationUtil
import kr.young.common.UtilLog.Companion.d
import kr.young.pjsip.CallManager
import kr.young.pjsip.UserAgent

class MainViewModel : ViewModel() {
    private val manager = CallManager.instance

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
        manager.startRegistration(
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
        manager.stopRegistration()
    }

    fun refreshRegistration() {
        manager.onNetworkChanged()
    }

    fun makeCall(counterpart: String) {
        manager.makeCall(counterpart, SIP_DOMAIN)
    }

    fun answerCall() {
        d(TAG, "answerCall")
        manager.answerCall()
    }

    fun declineCall() {
        d(TAG, "declineCall")
        manager.declineCall()
    }

    fun busyCall() {
        d(TAG, "busyCall")
        manager.busyCall()
    }

    fun ringingCall() {
        manager.ringingCall()
    }

    fun updateCall() {
        d(TAG, "updateCall")
        manager.updateCall()
//        callManager.sendRequest()
    }

    fun reInviteCall() {
        d(TAG, "reInviteCall")
        manager.reInviteCall()
    }

    fun endCall() {
        d(TAG, "endCall")
        manager.endCall()
    }

    fun mute(): Boolean {
        manager.callModel!!.mute = !manager.callModel!!.mute
        manager.mute(manager.callModel!!.mute)
        return manager.callModel!!.mute
    }

    fun speaker(on: Boolean? = null): Boolean {
        val c = ApplicationUtil.getContext()
        val audioManager = c!!.getSystemService(AUDIO_SERVICE) as AudioManager
        if (on == null) {
            manager.callModel!!.speaker = !manager.callModel!!.speaker
        } else {
            manager.callModel!!.speaker = on
        }
        audioManager.isSpeakerphoneOn = manager.callModel!!.speaker
        return manager.callModel!!.speaker
    }

    fun addBuddy(id: String) {
        d(TAG, "addBuddy")
        manager.setBuddy("sip:$id@$SIP_DOMAIN")
    }

    fun sendMessage(msg: String) {
        d(TAG, "sendMessage")
        manager.sendInstanceMessage(msg)
    }

    fun deleteBuddy() {
        d(TAG, "deleteBuddy")
        manager.deleteBuddy()
    }

    companion object {
        private const val TAG = "MainViewModel"
//        const val OUTBOUND_ADDRESS = "sip:sip.linphone.org"
//        const val OUTBOUND_PORT = "5061"
        const val OUTBOUND_ADDRESS = "sip:hongcafew-pbx.peoplev.net"
        const val OUTBOUND_PORT = "5479"
//        const val REGISTRATION_DURATION = "900"
        val transport = UserAgent.TransportType.TLS

        const val USER_ID = "sip:1000004@hongcafew-pbx.peoplev.net"
        const val SIP_DOMAIN = "hongcafew-pbx.peoplev.net"
        const val COUNTERPART = "1000005"
        const val USER_NAME = "everareen"
        const val PASSWORD = "1234"

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