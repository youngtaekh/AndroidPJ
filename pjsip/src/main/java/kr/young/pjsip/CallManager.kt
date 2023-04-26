package kr.young.pjsip

class CallManager private constructor() {
    private object Holder {
        val INSTANCE = CallManager()
    }

    private var call: CallEventListener? = null

    fun setCall(call: CallEventListener?) {
        this.call = call
    }

    fun getCall(): CallEventListener? {
        return this.call
    }

    companion object {
        private const val TAG = "CallManager"
        val instance: CallManager by lazy { Holder.INSTANCE }
    }
}