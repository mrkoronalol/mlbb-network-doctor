package ru.mlbb.blocker.data.analytics

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.mlbb.blocker.data.PingTargetResult
import ru.mlbb.blocker.data.meta.MetaRepository
import java.net.HttpURLConnection
import java.net.URL

/**
 * Анонимная отправка результатов диагностики пинга на сайт.
 * Уважает переключатель analytics_enabled.
 */
object PingTelemetry {

    suspend fun send(context: Context, results: List<PingTargetResult>): Boolean =
        withContext(Dispatchers.IO) {
            Analytics.initialize(context)
            if (!Analytics.isEnabled(context)) return@withContext true
            val measured = results.filter { it.pingMs != null }
            if (measured.isEmpty()) return@withContext false
            val base = MetaRepository.apiBase(context).trimEnd('/')
            if (base.isBlank() || base.contains("your-domain")) return@withContext false

            val items = JSONArray()
            measured.forEach { result ->
                items.put(
                    JSONObject()
                        .put("region", result.target.region)
                        .put("serverIp", result.target.ip)
                        .put("pingMs", result.pingMs),
                )
            }
            val body = JSONObject()
                .put("platform", "android")
                .put("deviceId", Analytics.deviceId(context))
                .put("items", items)

            try {
                val connection = (URL("$base/api/v1/telemetry/ping").openConnection()
                    as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 8_000
                    readTimeout = 8_000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
                val code = connection.responseCode
                connection.disconnect()
                code in 200..299
            } catch (_: Throwable) {
                false
            }
        }
}
