package kr.young.pjsip.model

class RegistrationInfo(
    val code: Int,
    val status: Int,
    val expiration: Long,
    val reason: String
)