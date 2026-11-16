package com.example.data.security

import android.util.Log
import com.example.data.model.DnsMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * High-performance RFC 8484 DNS-over-HTTPS (DoH) resolver.
 * Handles binary DNS queries to eu.geohide.ru and dns.adguard-dns.com with
 * fallback, TTL caching, and latency tracking.
 */
object DohResolver {
    private const val TAG = "DohResolver"

    data class DohStatus(
        val activeEndpoint: String = "https://eu.geohide.ru/dns-query",
        val mode: DnsMode = DnsMode.SYNTEXT_BYPASS,
        val totalQueries: Int = 0,
        val lastLatencyMs: Long = 0,
        val isHealthy: Boolean = true
    )

    private val _status = MutableStateFlow(DohStatus())
    val status: StateFlow<DohStatus> = _status.asStateFlow()

    private val queryCounter = AtomicInteger(0)
    private var currentMode = DnsMode.SYNTEXT_BYPASS

    private data class CachedRecord(
        val ips: List<InetAddress>,
        val expiresAt: Long
    )

    private val cache = ConcurrentHashMap<String, CachedRecord>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(3, TimeUnit.SECONDS)
        .build()

    private fun logI(msg: String) {
        try { Log.i(TAG, msg) } catch (_: Throwable) {}
    }

    private fun logW(msg: String) {
        try { Log.w(TAG, msg) } catch (_: Throwable) {}
    }

    private fun logE(msg: String) {
        try { Log.e(TAG, msg) } catch (_: Throwable) {}
    }

    fun updateMode(mode: DnsMode) {
        currentMode = mode
        cache.clear()
        _status.value = _status.value.copy(
            activeEndpoint = mode.dohUrl,
            mode = mode
        )
        logI("DohResolver updated mode to: ${mode.displayName} (${mode.dohUrl})")
    }

    /**
     * Resolves a hostname to a list of InetAddresses using the configured DoH server.
     */
    fun resolve(hostname: String): List<InetAddress> {
        val cleanHost = hostname.trim().lowercase()

        // Check if already an IP
        if (cleanHost.matches(Regex("^(\\d{1,3}\\.){3}\\d{1,3}$"))) {
            return try {
                listOf(InetAddress.getByName(cleanHost))
            } catch (_: Exception) {
                emptyList()
            }
        }

        // Check internal cache
        val now = System.currentTimeMillis()
        cache[cleanHost]?.let { record ->
            if (record.expiresAt > now && record.ips.isNotEmpty()) {
                return record.ips
            }
        }

        val primaryEndpoint = currentMode.dohUrl
        val fallbackEndpoint = if (currentMode == DnsMode.SYNTEXT_BYPASS) {
            "https://1.1.1.1/dns-query"
        } else {
            "https://dns.google/dns-query"
        }

        val startTime = System.currentTimeMillis()
        var ips = queryDohEndpoint(cleanHost, primaryEndpoint)
        var latency = System.currentTimeMillis() - startTime

        if (ips.isEmpty()) {
            logW("Primary DoH ($primaryEndpoint) failed for $cleanHost, trying fallback ($fallbackEndpoint)")
            val fallbackStart = System.currentTimeMillis()
            ips = queryDohEndpoint(cleanHost, fallbackEndpoint)
            latency = System.currentTimeMillis() - fallbackStart
        }

        // Last resort: System DNS if DoH failed completely (prevent blocking user navigation)
        if (ips.isEmpty()) {
            try {
                ips = InetAddress.getAllByName(cleanHost).toList()
            } catch (e: Exception) {
                logE("All DNS resolution failed for $cleanHost: ${e.message}")
            }
        }

        if (ips.isNotEmpty()) {
            cache[cleanHost] = CachedRecord(
                ips = ips,
                expiresAt = now + TimeUnit.MINUTES.toMillis(5) // 5 min TTL
            )
            val total = queryCounter.incrementAndGet()
            _status.value = _status.value.copy(
                totalQueries = total,
                lastLatencyMs = latency,
                isHealthy = true
            )
        } else {
            _status.value = _status.value.copy(isHealthy = false)
        }

        return ips
    }

    /**
     * Sends an RFC 8484 binary DNS query (Type A) via HTTPS POST to the endpoint.
     */
    private fun queryDohEndpoint(hostname: String, dohUrl: String): List<InetAddress> {
        return try {
            val queryBytes = buildDnsQuery(hostname)
            val requestBody = queryBytes.toRequestBody("application/dns-message".toMediaType())
            val request = Request.Builder()
                .url(dohUrl)
                .post(requestBody)
                .header("Content-Type", "application/dns-message")
                .header("Accept", "application/dns-message")
                .header("User-Agent", "XFERWEN-CyberBrowser/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                logW("DoH $dohUrl returned HTTP ${response.code}")
                return emptyList()
            }
            val responseBytes = response.body?.bytes() ?: return emptyList()
            parseDnsResponse(responseBytes)
        } catch (e: Exception) {
            logW("Error querying DoH $dohUrl for $hostname: ${e.message}")
            emptyList()
        }
    }

    /**
     * Builds RFC 1035 / RFC 8484 binary wireformat DNS query for A record (IPv4).
     */
    private fun buildDnsQuery(domain: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        // 12-byte DNS Header
        val queryId = (1..65535).random()
        dos.writeShort(queryId)      // ID
        dos.writeShort(0x0100)       // Flags: standard query, recursion desired
        dos.writeShort(1)            // QDCOUNT: 1 question
        dos.writeShort(0)            // ANCOUNT: 0
        dos.writeShort(0)            // NSCOUNT: 0
        dos.writeShort(0)            // ARCOUNT: 0

        // Question QNAME (labels format)
        for (part in domain.split('.')) {
            val bytes = part.toByteArray(Charsets.US_ASCII)
            dos.writeByte(bytes.size)
            dos.write(bytes)
        }
        dos.writeByte(0)             // Terminating null byte

        // QTYPE = 1 (A Record), QCLASS = 1 (IN)
        dos.writeShort(1)
        dos.writeShort(1)

        dos.flush()
        return baos.toByteArray()
    }

    /**
     * Parses RFC 1035 binary DNS response to extract IPv4 addresses.
     */
    private fun parseDnsResponse(data: ByteArray): List<InetAddress> {
        if (data.size < 12) return emptyList()
        val buffer = ByteBuffer.wrap(data)

        val id = buffer.short
        val flags = buffer.short
        val qdcount = buffer.short.toInt() and 0xFFFF
        val ancount = buffer.short.toInt() and 0xFFFF
        val nscount = buffer.short
        val arcount = buffer.short

        // Skip Question Section
        for (i in 0 until qdcount) {
            while (buffer.hasRemaining()) {
                val len = buffer.get().toInt() and 0xFF
                if (len == 0) break
                if ((len and 0xC0) == 0xC0) {
                    if (buffer.hasRemaining()) buffer.get() // pointer second byte
                    break
                }
                buffer.position(buffer.position() + len)
            }
            if (buffer.remaining() >= 4) {
                buffer.short // QTYPE
                buffer.short // QCLASS
            }
        }

        val ips = mutableListOf<InetAddress>()

        // Parse Answer Section
        for (i in 0 until ancount) {
            if (!buffer.hasRemaining()) break

            // Skip Name (label or compression pointer)
            val firstByte = buffer.get().toInt() and 0xFF
            if ((firstByte and 0xC0) == 0xC0) {
                if (buffer.hasRemaining()) buffer.get()
            } else {
                var len = firstByte
                while (len > 0 && buffer.hasRemaining()) {
                    buffer.position(buffer.position() + len)
                    len = buffer.get().toInt() and 0xFF
                }
            }

            if (buffer.remaining() < 10) break
            val type = buffer.short.toInt() and 0xFFFF
            val clazz = buffer.short.toInt() and 0xFFFF
            val ttl = buffer.int
            val rdlength = buffer.short.toInt() and 0xFFFF

            if (buffer.remaining() < rdlength) break

            if (type == 1 && rdlength == 4) { // Type A (IPv4)
                val ipBytes = ByteArray(4)
                buffer.get(ipBytes)
                try {
                    ips.add(InetAddress.getByAddress(ipBytes))
                } catch (_: Exception) {}
            } else {
                buffer.position(buffer.position() + rdlength)
            }
        }

        return ips
    }
}
