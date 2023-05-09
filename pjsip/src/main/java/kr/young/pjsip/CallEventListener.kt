package kr.young.pjsip

import kr.young.common.UtilLog.Companion.d
import kr.young.pjsip.observer.PJSIPObserverImpl
import org.pjsip.pjsua2.*
import org.pjsip.pjsua2.pjsip_role_e.PJSIP_ROLE_UAC

class CallEventListener(
    accountImpl: AccountImpl,
    callId: Int,
    private val endpoint: Endpoint
): Call(accountImpl, callId) {
    var vidWin: VideoWindow? = null
    var vidPrev: VideoPreview? = null

    private val pjsipObserverImpl = PJSIPObserverImpl.instance

    override fun onCallState(callParam: OnCallStateParam?) {
        when (info.state) {
            pjsip_inv_state.PJSIP_INV_STATE_NULL -> { d(TAG, "PJSIP_INV_STATE_NULL") }
            //Send INVITE
            pjsip_inv_state.PJSIP_INV_STATE_CALLING -> { d(TAG, "PJSIP_INV_STATE_CALLING") }
            pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> { onIncomingCall() }
            //Sending & Receive Ringing ...
            pjsip_inv_state.PJSIP_INV_STATE_EARLY -> { early() }
            //Receive Answer
            pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> { onConnectingCall(callParam) }
            //Connected
            pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> { onConnectedCall() }
            pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> { onDisconnectedCall() }
        }

//        pjsipObserverImpl.onCallStateObserver(info, callParam!!.e.body.tsxState.src.rdata.wholeMsg)
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {

        val callMediaInfoVector = info.media

        for (i in callMediaInfoVector.indices) {
            val cmi = callMediaInfoVector[i]
            if (cmi.type == pjmedia_type.PJMEDIA_TYPE_AUDIO && (
                        cmi.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
                                cmi.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD)
            ) {
                // connect ports
                try {
                    val am = getAudioMedia(i)
                    endpoint.audDevManager().captureDevMedia.startTransmit(am)
                    am.startTransmit(endpoint.audDevManager().playbackDevMedia)
                } catch (e: Exception) {
                    println("Failed connecting media ports" + e.message)
                }
            } else if (cmi.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                cmi.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                cmi.videoIncomingWindowId != pjsua2.INVALID_ID
            ) {
                vidWin = VideoWindow(cmi.videoIncomingWindowId)
                vidPrev = VideoPreview(cmi.videoCapDev)
            }
        }

        pjsipObserverImpl.onCallMediaStateObserver()
    }

    private fun onIncomingCall() {
        d(TAG, "PJSIP_INV_STATE_INCOMING")
    }

    private fun early() {
        d(TAG, "PJSIP_INV_STATE_EARLY")
        //role 0 - caller, 1 - callee
        if (info.role == PJSIP_ROLE_UAC) {
            d(TAG, "call role PJSIP_ROLE_UAC")
            pjsipObserverImpl.onOutgoingCallObserver(info)
        } else {
            d(TAG, "call role PJSIP_ROLE_UAS")
        }
    }

    private fun onConnectingCall(callParam: OnCallStateParam?) {
        d(TAG, "PJSIP_INV_STATE_CONNECTING")
        d(TAG, "call remoteUri ${info.remoteUri}")
        d(TAG, "call remoteContact ${info.remoteContact}")

//        d(TAG, "event type ${callParam!!.e.type}")
//        d(TAG, "tsx prevState ${callParam.e.body.tsxState.prevState}")
//        d(TAG, "tsx type ${callParam.e.body.tsxState.type}")
//        d(TAG, "tsx status ${callParam.e.body.tsxState.src.status}")
//        d(TAG, "src address ${callParam.e.body.tsxState.src.rdata.srcAddress}")
//        d(TAG, "tsx info ${callParam.e.body.tsxState.src.rdata.info}")
//        d(TAG, "tsx rdata wholeMsg ${callParam.e.body.tsxState.src.rdata.wholeMsg}")
        d(TAG, "============================================================")
    }

    private fun onConnectedCall() {
        d(TAG, "PJSIP_INV_STATE_CONFIRMED")
        pjsipObserverImpl.onConnectedCallObserver(info)
    }

    private fun onDisconnectedCall() {
        d(TAG, "PJSIP_INV_STATE_DISCONNECTED")
        pjsipObserverImpl.onTerminatedCallObserver(info)
        endpoint.utilLogWrite(3, TAG, dump(true, ""))
    }

    companion object {
        private const val TAG = "CallEventListener"
    }
}