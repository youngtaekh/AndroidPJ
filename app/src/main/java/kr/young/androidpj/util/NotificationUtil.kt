package kr.young.androidpj.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_MIN
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.os.PowerManager
import android.os.PowerManager.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.CATEGORY_CALL
import androidx.core.app.NotificationManagerCompat
import kr.young.androidpj.CallActivity
import kr.young.androidpj.MainActivity
import kr.young.androidpj.R
import kr.young.androidpj.ReceiveActivity
import kr.young.pjsip.CallManager


class NotificationUtil {

    companion object {
        fun getCallNotification(context: Context): Notification {
            val builder = NotificationCompat.Builder(context, CALL_CHANNEL_ID)
                .setSmallIcon(R.drawable.round_call_24)
                .setContentTitle("Call")
                .setAutoCancel(false)
                .setOngoing(true)

            val intent = Intent()
            intent.flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            if (CallManager.instance.callModel?.connected == true || CallManager.instance.callModel?.outgoing ==true) {
                //Call
                intent.setClass(context, CallActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

                builder.setContentText("Call to ${CallManager.instance.callModel!!.counterpart}")
                builder.setCategory(CATEGORY_CALL)
                builder.setContentIntent(pendingIntent)
                builder.setChannelId(CALL_CHANNEL_ID)
                val endPending = PendingIntent.getBroadcast(context, 0, Intent(END_ACTION), FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
                val endAction = Action(R.drawable.round_call_end_24, context.getString(R.string.end), endPending)
                builder.addAction(endAction)

                createChannel(
                    context, CALL_CHANNEL_ID,
                    context.getString(R.string.call_notification),
                    context.getString(R.string.call_notification_desc))
            } else if (CallManager.instance.callModel?.incoming == true) {
                //Incoming
                intent.setClass(context, ReceiveActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

                builder.setContentText("Incoming from ${CallManager.instance.callModel!!.counterpart}")
                builder.setCategory(CATEGORY_CALL)
                builder.setContentIntent(pendingIntent)
                builder.setFullScreenIntent(pendingIntent, true)
                builder.priority = NotificationCompat.PRIORITY_HIGH
                builder.setChannelId(RECEIVE_CHANNEL_ID)
                val declinePending = PendingIntent.getBroadcast(context, 0, Intent(DECLINE_ACTION), FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
                val declineAction = Action(R.drawable.round_call_end_24, context.getString(R.string.decline), declinePending)
                val answerPending = PendingIntent.getBroadcast(context, 0, Intent(ANSWER_ACTION), FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
                val answerAction = Action(R.drawable.round_call_24, context.getString(R.string.accept), answerPending)
                builder.addAction(declineAction)
                builder.addAction(answerAction)

                createChannel(
                    context, RECEIVE_CHANNEL_ID,
                    context.getString(R.string.receive_notification),
                    context.getString(R.string.receive_notification_desc))
            } else {
                intent.setClass(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

                builder.setContentText("Push from ${CallManager.instance.callModel!!.counterpart}")
                builder.setContentIntent(pendingIntent)
                builder.priority = NotificationCompat.PRIORITY_MIN
                builder.setChannelId(MUTE_CHANNEL_ID)

                createChannel(
                    context, MUTE_CHANNEL_ID,
                    context.getString(R.string.mute_notice_notification),
                    context.getString(R.string.mute_notice_notification_desc),
                    IMPORTANCE_MIN)
            }

            return builder.build()
        }

        fun noticeNotification(context: Context, title: String, body: String) {
            createChannel(context, NOTICE_CHANNEL_ID, context.getString(R.string.notice_notification), context.getString(R.string.notice_notification_desc))
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_SINGLE_TOP
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
            val builder = NotificationCompat.Builder(context, NOTICE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setChannelId(NOTICE_CHANNEL_ID)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(123, builder.build())
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val isScreenOn = pm.isInteractive

            if (!isScreenOn) {
                val wl = pm.newWakeLock(
                    SCREEN_BRIGHT_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP or ON_AFTER_RELEASE,
                    "AndroidPJ:notificationLock"
                )
                wl.acquire(3000) //set your time in milliseconds
            }
        }

        private fun createChannel(
            context: Context,
            channelId: String,
            name: String,
            description: String,
            importance: Int = NotificationManager.IMPORTANCE_HIGH
        ) {
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = description
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        private const val TAG = "NotificationUtil"
        private const val NOTICE_CHANNEL_ID = "noticeChannelId"
        private const val CALL_CHANNEL_ID = "callChannelId"
        private const val MESSAGE_CHANNEL_ID = "messageChannelId"
        private const val RECEIVE_CHANNEL_ID = "receiveChannelId"
        private const val MUTE_CHANNEL_ID = "muteChannelId"
        const val CALL_NOTIFICATION_ID = 1234

        const val DECLINE_ACTION = "declineAction"
        const val ANSWER_ACTION = "answerAction"
        const val END_ACTION = "endAction"
    }
}