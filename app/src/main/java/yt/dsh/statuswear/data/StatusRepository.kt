package yt.dsh.statuswear.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://status.dsh.yt"
private const val SLUG = "dsh"

class StatusRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
) {

    /** Pobiera i łączy dane strony statusu, incydentów oraz heartbeatów w jeden model widoku. */
    suspend fun fetchStatus(): Result<DshStatusUiState> = withContext(Dispatchers.IO) {
        try {
            val pageResponse = get("$BASE_URL/api/status-page/$SLUG")
            val heartbeatResponse = get("$BASE_URL/api/status-page/heartbeat/$SLUG")

            val page = json.decodeFromString(StatusPageResponse.serializer(), pageResponse)
            val heartbeats = json.decodeFromString(HeartbeatResponse.serializer(), heartbeatResponse)

            val groups = page.publicGroupList.map { group ->
                val monitorUiList = group.monitorList.map { monitor ->
                    val id = monitor.id
                    val hbList = heartbeats.heartbeatList[id.toString()]
                    val latest = hbList?.lastOrNull()
                    val uptime = heartbeats.uptimeList["${id}_24"]

                    MonitorUiState(
                        id = id,
                        name = monitor.name,
                        status = MonitorStatus.fromCode(latest?.status),
                        uptime24h = uptime,
                        lastMsg = latest?.msg
                    )
                }

                MonitorGroupUiState(
                    id = group.id,
                    name = group.name.trim(),
                    monitors = monitorUiList
                )
            }.filter { it.monitors.isNotEmpty() }

            val incidents = page.incidents
                .filter { it.active }
                .map { incident ->
                    IncidentUiState(
                        id = incident.id,
                        title = incident.title,
                        content = incident.content,
                        style = incident.style,
                        createdDate = incident.createdDate,
                        pin = incident.pin,
                        active = incident.active
                    )
                }

            Result.success(
                DshStatusUiState(
                    pageTitle = page.config?.title ?: "Stan Usług DSH",
                    groups = groups,
                    incidents = incidents,
                    isLoading = false,
                    error = null,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun get(url: String): String {
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code} for $url")
            }
            return response.body?.string() ?: throw IOException("Puste ciało odpowiedzi: $url")
        }
    }
}
