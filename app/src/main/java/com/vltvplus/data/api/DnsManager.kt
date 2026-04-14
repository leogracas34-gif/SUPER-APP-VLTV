package com.vltvplus.data.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.vltvplus.utils.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DnsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    companion object {
        val DNS_LIST = listOf(
            "http://fibercdn.sbs",
            "http://tvblack.shop",
            "http://blackzz.shop",
            "http://playchannels.shop",
            "http://xppv.shop",
            "http://redeinternadestiny.top",
            "http://blackstartv.shop",
            "http://blackdns.shop",
            "http://ranos.sbs",
            "http://cmdtv.casa",
            "http://cmdtv.pro",
            "http://cmdtv.sbs",
            "http://cmdtv.top",
            "http://cmdbr.life",
            "http://blackdeluxe.shop"
        )
        const val DEFAULT_PORT = 8080
        const val PING_TIMEOUT_MS = 5000L
    }

    private val testClient = OkHttpClient.Builder()
        .connectTimeout(PING_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
        .readTimeout(PING_TIMEOUT_MS, java.util.concurrent.TimeUnit.MILLISECONDS)
        .build()

    suspend fun findFastestDns(): String? = withContext(Dispatchers.IO) {
        val cachedDns = preferenceManager.getActiveDns()
        if (cachedDns != null && pingDns(cachedDns)) {
            return@withContext cachedDns
        }

        val results = DNS_LIST.map { dns ->
            async {
                val latency = measureLatency(dns)
                Pair(dns, latency)
            }
        }.awaitAll()

        val fastest = results
            .filter { it.second >= 0 }
            .minByOrNull { it.second }
            ?.first

        fastest?.let {
            preferenceManager.setActiveDns(it)
        }
        fastest
    }

    suspend fun findFastestDnsForCredentials(username: String, password: String): DnsResult =
        withContext(Dispatchers.IO) {
            val jobs = DNS_LIST.map { dns ->
                async {
                    val latency = measureLatency(dns)
                    Triple(dns, latency, latency >= 0)
                }
            }.awaitAll()

            val reachable = jobs.filter { it.second >= 0 }.sortedBy { it.second }

            for ((dns, _, _) in reachable) {
                val url = buildAuthUrl(dns, username, password)
                try {
                    val request = Request.Builder().url(url).head().build()
                    val response = testClient.newCall(request).execute()
                    if (response.isSuccessful || response.code == 200) {
                        preferenceManager.setActiveDns(dns)
                        return@withContext DnsResult.Success(dns, buildBaseUrl(dns))
                    }
                } catch (e: Exception) {
                    continue
                }
            }
            DnsResult.Error("Nenhum servidor disponível. Verifique sua conexão.")
        }

    private suspend fun measureLatency(dns: String): Long = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val request = Request.Builder()
                .url("$dns:$DEFAULT_PORT")
                .head()
                .build()
            testClient.newCall(request).execute().close()
            System.currentTimeMillis() - start
        } catch (e: Exception) {
            -1L
        }
    }

    private suspend fun pingDns(dns: String): Boolean = withContext(Dispatchers.IO) {
        measureLatency(dns) >= 0
    }

    fun buildBaseUrl(dns: String): String = "$dns:$DEFAULT_PORT"

    fun buildAuthUrl(dns: String, username: String, password: String): String =
        "$dns:$DEFAULT_PORT/player_api.php?username=$username&password=$password"

    fun buildStreamUrl(dns: String, username: String, password: String, streamId: String, ext: String = "ts"): String =
        "$dns:$DEFAULT_PORT/$username/$password/$streamId.$ext"

    fun buildVodUrl(dns: String, username: String, password: String, vodId: String, ext: String = "mp4"): String =
        "$dns:$DEFAULT_PORT/movie/$username/$password/$vodId.$ext"

    fun buildSeriesUrl(dns: String, username: String, password: String, seriesId: String, ext: String = "mp4"): String =
        "$dns:$DEFAULT_PORT/series/$username/$password/$seriesId.$ext"

    fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

sealed class DnsResult {
    data class Success(val dns: String, val baseUrl: String) : DnsResult()
    data class Error(val message: String) : DnsResult()
}
