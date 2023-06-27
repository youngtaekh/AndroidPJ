package kr.young.androidpj.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.young.common.UtilLog.Companion.i

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        i(TAG, "message title is ${message.notification?.title}")
        i(TAG, "message body is ${message.notification?.body}")
        val data = message.data
        i(TAG, "message data is $data")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        i(TAG, "onNewToken($token)")
    }

    companion object {
        private const val TAG = "MyFirebaseMessagingService"
    }
}