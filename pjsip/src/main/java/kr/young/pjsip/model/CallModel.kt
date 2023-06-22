package kr.young.pjsip.model

data class CallModel(
    var counterpart: String,
    var outgoing: Boolean = false,
    var incoming: Boolean = false,
    var connected: Boolean = false,
    var terminated: Boolean = false,
    var mute: Boolean = false,
    var speaker: Boolean = false,
)