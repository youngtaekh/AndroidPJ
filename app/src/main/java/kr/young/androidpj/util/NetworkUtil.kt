package kr.young.androidpj.util

import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Handler
import android.os.Looper
import kr.young.common.UtilLog.Companion.i
import kr.young.pjsip.CallManager

class NetworkUtil(mContext: Context) {
    private val manager = mContext.getSystemService(ConnectivityManager::class.java)
    private var activeNetworkType = NetworkType.INIT

    fun release() {
        this.manager.unregisterNetworkCallback(callback)
    }

    private val callback = object: NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            i(TAG, "onLost")
            activeNetworkType = NetworkType.NONE
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, capabilities)

            val networkType = if (capabilities.hasTransport(TRANSPORT_WIFI)) {
                NetworkType.WIFI
            } else if (capabilities.hasTransport(TRANSPORT_CELLULAR)) {
                NetworkType.CELLULAR
            } else {
                NetworkType.NONE
            }

            if (activeNetworkType != NetworkType.INIT && networkType != activeNetworkType) {
                i(TAG, "onCapabilitiesChanged $activeNetworkType -> $networkType")
                Handler(Looper.getMainLooper()).post { CallManager.instance.onNetworkChanged() }
            }
            activeNetworkType = networkType
        }
    }

    init {
        this.manager.registerDefaultNetworkCallback(callback)
    }

    companion object {
        private const val TAG = "NetworkUtil"
    }

    enum class NetworkType {
        INIT,
        WIFI,
        CELLULAR,
        NONE
    }
}