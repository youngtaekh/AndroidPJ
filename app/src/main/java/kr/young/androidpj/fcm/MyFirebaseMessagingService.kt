package kr.young.androidpj.fcm

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.young.androidpj.CallService
import kr.young.androidpj.ui.main.MainViewModel
import kr.young.androidpj.util.NotificationUtil
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager
import kr.young.pjsip.model.CallModel

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        i(TAG, "message title is ${message.notification?.title}")
        i(TAG, "message body is ${message.notification?.body}")
        if (message.notification != null) {
            NotificationUtil.noticeNotification(this, message.notification!!.title!!, message.notification!!.body!!)
        }
        val data = message.data
        i(TAG, "message data is $data")
        when (data["type"]) {
            "call" -> { start() }
            "stop" -> {}
            else -> NotificationUtil.noticeNotification(this, data["title"]!!, data["message"]!!)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        i(TAG, "onNewToken($token)")
    }

    private fun start() {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            CallManager.instance.startRegistration(
                MainViewModel.getOutbound(),
                MainViewModel.USER_ID,
                MainViewModel.PASSWORD,
                MainViewModel.getStun(),
                MainViewModel.getTurn(),
                MainViewModel.TURN_ID,
                MainViewModel.TURN_PASSWORD,
                MainViewModel.transport
            )
            CallManager.instance.callModel = CallModel("test c", pushReceived = true)
            startForegroundService(Intent(this, CallService::class.java))
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}