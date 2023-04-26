package kr.young.pjsip

import kr.young.common.UtilLog.Companion.d
import kr.young.pjsip.observer.PJSIPObserverImpl
import org.pjsip.pjsua2.*

class CallEventListener(
    accountImpl: AccountImpl,
    callId: Int,
    private val endpoint: Endpoint
): Call(accountImpl, callId) {
    var vidWin: VideoWindow? = null
    var vidPrev: VideoPreview? = null

    private var pjsipObserverImpl: PJSIPObserverImpl? = null

    init {
        pjsipObserverImpl = PJSIPObserverImpl.instance
    }

    override fun onCallState(prm: OnCallStateParam?) {
        val callInfo = info
        if (callInfo.state == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
            d(TAG, "call state ${callInfo.state}")
            d(TAG, "call account ${callInfo.accId}")
            d(TAG, "call account ${callInfo.stateText}")

            d(TAG, "event type ${prm!!.e.type}")
            d(TAG, "tsx prevState ${prm.e.body.tsxState.prevState}")
            d(TAG, "tsx type ${prm.e.body.tsxState.type}")
            d(TAG, "tsx status ${prm.e.body.tsxState.src.status}")
            d(TAG, "src address ${prm.e.body.tsxState.src.rdata.srcAddress}")
            d(TAG, "tsx info ${prm.e.body.tsxState.src.rdata.info}")
            d(TAG, "tsx rdata wholeMsg ${prm.e.body.tsxState.src.rdata.wholeMsg}")
            d(TAG, "============================================================")
        }
        if (callInfo.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
            endpoint.utilLogWrite(3, TAG, dump(true, ""))
        }

        pjsipObserverImpl!!.onCallStateObserver(callInfo, prm!!.e.body.tsxState.src.rdata.wholeMsg)
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {
        val ci: CallInfo = try {
            info
        } catch (e: Exception) {
            return
        }

        val callMediaInfoVector = ci.media

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

        pjsipObserverImpl!!.onCallMediaStateObserver()
    }

    companion object {
        private const val TAG = "CallEventListener"
    }
}