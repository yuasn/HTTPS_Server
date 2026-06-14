package com.example.httpsserveryuasn.server

import java.net.Inet4Address
import java.net.NetworkInterface

object NetworkUtils {
    /**
     * Returns a list of local IPv4 addresses for the device.
     */
    fun getLocalIpAddresses(): List<String> {
        val ipList = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        ipList.add(address.hostAddress ?: "")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ipList.filter { it.isNotEmpty() }
    }
}
