package kr.young.pjsip.util

import org.pjsip.pjsua2.SipHeader
import org.pjsip.pjsua2.SipHeaderVector

class CustomHeader {
    companion object {
        fun make(name: String, value: String): SipHeader {
            val customHeader = SipHeader()
            customHeader.hName = name
            customHeader.hValue = value
            return customHeader
        }

        fun makeVector(name: String, value: String): SipHeaderVector {
            return SipHeaderVector(arrayOf(make(name, value)))
        }
    }
}