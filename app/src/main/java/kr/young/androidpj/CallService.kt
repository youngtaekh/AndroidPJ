package kr.young.androidpj

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import kr.young.androidpj.util.NetworkUtil
import kr.young.androidpj.util.NotificationUtil
import kr.young.androidpj.util.NotificationUtil.Companion.ANSWER_ACTION
import kr.young.androidpj.util.NotificationUtil.Companion.CALL_NOTIFICATION_ID
import kr.young.androidpj.util.NotificationUtil.Companion.DECLINE_ACTION
import kr.young.androidpj.util.NotificationUtil.Companion.END_ACTION
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager
import kr.young.pjsip.model.CallModel
import kr.young.pjsip.model.RegistrationInfo
import kr.young.pjsip.observer.PJSIPObserver
import kr.young.pjsip.observer.PJSIPObserverImpl
import kr.young.pjsip.util.CustomHeader
import org.pjsip.pjsua2.*

class CallService : Service(), PJSIPObserver.Register, PJSIPObserver.Call {

    private lateinit var networkUtil: NetworkUtil

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        i(TAG, "onCreate")

        networkUtil = NetworkUtil(this)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.add(this as PJSIPObserver.Call)

        val filter = IntentFilter()
        filter.addAction(DECLINE_ACTION)
        filter.addAction(ANSWER_ACTION)
        filter.addAction(END_ACTION)
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        i(TAG, "onStartCommand")
        startForeground(CALL_NOTIFICATION_ID, NotificationUtil.getCallNotification(this))

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        i(TAG, "onDestroy")

        networkUtil.release()
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Register)
        PJSIPObserverImpl.instance.remove(this as PJSIPObserver.Call)
        unregisterReceiver(receiver)
    }

    companion object {
        private const val TAG = "CallService"
    }

    override fun onIncomingCall(callInfo: CallInfo) {
        //Send 180 Ringing
        CallManager.instance.ringingCall()
        if (CallManager.instance.callModel == null || CallManager.instance.callModel!!.terminated) {
            CallManager.instance.callModel = CallModel(callInfo.remoteContact)
        } else {
            CallManager.instance.callModel!!.counterpart = callInfo.remoteContact
        }
        CallManager.instance.callModel!!.incoming = true
        startForeground(CALL_NOTIFICATION_ID, NotificationUtil.getCallNotification(this))
    }

    override fun onRegStarted(onRegStartedParam: OnRegStartedParam?) {}

    override fun onRegistrationSuccess(registrationInfo: RegistrationInfo) {}

    override fun onRegistrationFailed(registrationInfo: RegistrationInfo) {}

    override fun onUnRegistrationSuccess(registrationInfo: RegistrationInfo) {}

    override fun onUnRegistrationFailed(registrationInfo: RegistrationInfo) {}

    override fun onOutgoingCall(callInfo: CallInfo) {
        i(TAG, "onOutgoingCall")
    }

    override fun onConnectedCall(callInfo: CallInfo) {
        i(TAG, "onConnectedCall")
        startForeground(CALL_NOTIFICATION_ID, NotificationUtil.getCallNotification(this))
        val intent = Intent(this, CallActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    override fun onTerminatedCall(callInfo: CallInfo) {
        i(TAG, "onTerminatedCall")
        stopSelf()
    }

    override fun onCallState(callInfo: CallInfo, wholeMsg: String?) {
        i(TAG, "onCallState")
    }

    override fun onCallMediaState() {
        i(TAG, "onCallMediaState")
    }

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            i(TAG, "onReceive action ${intent?.action}")
            when (intent?.action) {
                DECLINE_ACTION -> { CallManager.instance.declineCall() }
                ANSWER_ACTION -> { CallManager.instance.answerCall() }
                END_ACTION -> { CallManager.instance.endCall() }
            }
        }
    }
}