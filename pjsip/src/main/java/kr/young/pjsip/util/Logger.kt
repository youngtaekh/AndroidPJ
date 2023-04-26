package kr.young.pjsip.util

import kr.young.common.UtilLog.Companion.i
import org.pjsip.pjsua2.LogEntry
import org.pjsip.pjsua2.LogWriter

class Logger: LogWriter() {
    override fun write(entry: LogEntry?) {
        i("PJSIP", entry!!.msg)
    }
}