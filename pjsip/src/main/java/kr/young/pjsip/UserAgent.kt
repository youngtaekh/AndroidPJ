package kr.young.pjsip

import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.util.Logger
import org.pjsip.pjsua2.*

class UserAgent private constructor() {
    enum class TransportType {
        UDP,
        TCP,
        TLS
    }

    private object Holder {
        val INSTANCE = UserAgent()
    }

    private var uaConfig: UaConfig? = null

    internal var accountImpl: AccountImpl? = null
    private var accountConfig: AccountConfig? = null

    internal var endPointImpl: EndPointImpl? = null
    private var epConfig: EpConfig? = null

    private var transportConfig: TransportConfig? = null

    private var logger: Logger? = null

    private var ownWorkerThread = false
    private var type = TransportType.UDP

    private var forAsterisk: Boolean? = null
    private var isSRTP: Boolean? = null
    private var isIPv6: Boolean? = null

    init {
        forAsterisk = true
        isSRTP = false
        isIPv6 = false
    }

    fun init(
        outboundProxyAddress: String,
        userId: String,
        userPassword: String,
        stunServer: String,
        turnServer: String,
        turnUserName: String,
        turnPassword: String,
        type: TransportType
    ) {
        i(TAG, "init(Proxy, Id, Password, Stun, Turn)")
        this.type = type
        endPointImpl = EndPointImpl()
        epConfig = EpConfig()
        transportConfig = TransportConfig()

        /* Create endpoint */
        try {
            endPointImpl!!.libCreate()
        } catch (ignored: Exception) {}

        initLog()
        setUaConfig()
        if (!forAsterisk!!) {
            setStunServer(stunServer)
        }

        try {
            endPointImpl!!.libInit(epConfig)
        } catch (ignored: Exception) {}

        setTransportConfig()
        setSipConfig(outboundProxyAddress, userId, userPassword)
        if (isSRTP!!) {
            setSRTPUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL)
        } else {
            setSRTPUse(pjmedia_srtp_use.PJMEDIA_SRTP_DISABLED)
        }
        if (isIPv6!!) {
            setIpv6(pjsua_ipv6_use.PJSUA_IPV6_ENABLED)
        } else {
            setIpv6(pjsua_ipv6_use.PJSUA_IPV6_DISABLED)
        }
        setIceEnable(true)
        if (!forAsterisk!!) {
            setTurnServer(turnServer, turnUserName, turnPassword)
        }
    }

    fun release() {
        transportConfig!!.delete()
        transportConfig = null
        accountConfig!!.delete()
        accountConfig = null
        accountImpl!!.delete()
        accountImpl = null
        epConfig!!.delete()
        epConfig = null
    }

    fun start() {
        try {
            endPointImpl!!.libStart()
            createAccount()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        /* Shutdown pjsua. Note that Endpoint destructor will also invoke
		 * libDestroy(), so this will be a test of double libDestroy().
		 */
        try {
            endPointImpl!!.libDestroy()
        } catch (ignored: java.lang.Exception) {
        }

        /* Force delete Endpoint here, to avoid deletion from a non-
		 * registered thread (by GC?).
		 */
        endPointImpl!!.delete()
        endPointImpl = null
    }

    private fun initLog() {
        /* Override log level setting */
        epConfig!!.logConfig.level = LOG_LEVEL
        epConfig!!.logConfig.consoleLevel = LOG_LEVEL

        /* Set log config. */
        val logConfig = epConfig!!.logConfig
        logger = Logger()
        logConfig.writer = logger
        logConfig.decor = logConfig.decor and
                (pj_log_decoration.PJ_LOG_HAS_CR or
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE).inv().toLong()
    }

    private fun setUaConfig() {
        uaConfig = epConfig!!.uaConfig
        uaConfig!!.userAgent = USER_AGENT

        /* No worker thread */
        if (ownWorkerThread) {
            uaConfig!!.threadCnt = 0
            uaConfig!!.mainThreadOnly = true
        }
    }

    private fun setTransportConfig() {
        transportConfig!!.port = SIP_PORT

        /* Create transports. */
        when (type) {
            TransportType.UDP -> {
                try {
                    endPointImpl!!.transportCreate(
                        pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
                        transportConfig
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            TransportType.TCP -> {
                try {
                    endPointImpl!!.transportCreate(
                        pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
                        transportConfig
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
            TransportType.TLS -> {
                try {
                    transportConfig!!.port = (SIP_PORT + 1.toLong())
                    endPointImpl!!.transportCreate(
                        pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,
                        transportConfig
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

        /* Set SIP port back to default for JSON saved config */
        transportConfig!!.port = SIP_PORT
    }

    private fun getUserName(userId: String): String {
        return userId.substring(userId.indexOf(":")+1, userId.indexOf("@"))
    }

    private fun setSipConfig(outboundProxyAddress: String,
                             userId: String,
                             userPassword: String): UserAgent {
        accountConfig = AccountConfig()
        accountConfig!!.idUri = userId
        accountConfig!!.regConfig.registrarUri = outboundProxyAddress
        val vector = StringVector()
        when (type) {
            TransportType.TLS -> {
                vector.add(outboundProxyAddress + TLS_TAIL)
            }
            TransportType.TCP -> {
                vector.add(outboundProxyAddress + TCP_TAIL)
            }
            else -> {
                vector.add(outboundProxyAddress)
            }
        }
        accountConfig!!.sipConfig.proxies = vector
        val credVector = AuthCredInfoVector()
        credVector.add(AuthCredInfo("Digest", "*", getUserName(userId), 0, userPassword))
        accountConfig!!.sipConfig.authCreds = credVector
        return this
    }

    private fun setStunServer(stunServer: String): UserAgent {
        val stunServers = StringVector()
        stunServers.add(stunServer)
        uaConfig!!.stunServer = stunServers
        return this
    }

    private fun setTurnServer(turnServer: String,
                              turnUserName: String,
                              turnPassword: String): UserAgent {
        accountConfig!!.natConfig.turnEnabled = true
        accountConfig!!.natConfig.turnServer = turnServer
        accountConfig!!.natConfig.turnUserName = turnUserName
        accountConfig!!.natConfig.turnPassword = turnPassword
        return this
    }

    private fun setIceEnable(enable: Boolean): UserAgent {
        accountConfig!!.natConfig.iceEnabled = enable
        return this
    }

    private fun setSRTPUse(value: Int): UserAgent {
        accountConfig!!.mediaConfig.srtpUse = value
        accountConfig!!.mediaConfig.srtpSecureSignaling = 0
        return this
    }

    private fun setIpv6(value: Int): UserAgent {
        accountConfig!!.mediaConfig.ipv6Use = value
        return this
    }

    private fun createAccount() {
        accountImpl = AccountImpl(accountConfig!!, endPointImpl!!)
        try {
            println(accountConfig!!.idUri)
            accountImpl!!.create(accountConfig)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun onNetworkChanged() {
        val param = IpChangeParam()
        endPointImpl?.handleIpChange(param)
    }

    companion object {
        private const val TAG = "UserAgent"
        private const val LOG_LEVEL = 4L
        const val USER_AGENT = "Android"
        const val SIP_PORT = 6000L
        const val TLS_TAIL = ";hide;transport=tls"
        const val TCP_TAIL = ";hide;transport=tcp"

        val instance: UserAgent by lazy { Holder.INSTANCE }
    }
}
