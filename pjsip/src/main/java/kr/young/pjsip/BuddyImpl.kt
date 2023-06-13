package kr.young.pjsip

import kr.young.common.UtilLog.Companion.d
import org.pjsip.pjsua2.Buddy
import org.pjsip.pjsua2.BuddyConfig
import org.pjsip.pjsua2.pjsip_evsub_state.PJSIP_EVSUB_STATE_ACTIVE
import org.pjsip.pjsua2.pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE
import org.pjsip.pjsua2.pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE

class BuddyImpl(val buddyConfig: BuddyConfig): Buddy() {

    fun getStatusText(): String {
        val buddyInfo = info

        return if (buddyInfo.subState == PJSIP_EVSUB_STATE_ACTIVE) {
            when (buddyInfo.presStatus.status) {
                PJSUA_BUDDY_STATUS_ONLINE -> "Online"
                PJSUA_BUDDY_STATUS_OFFLINE -> "Offline"
                else -> "Unknown"
            }
        } else {
            "Inactive"
        }
    }

    override fun onBuddyState() {
        d(TAG, "buddy status ${getStatusText()}")
    }

    companion object {
        private const val TAG = "BuddyImpl"
    }
}