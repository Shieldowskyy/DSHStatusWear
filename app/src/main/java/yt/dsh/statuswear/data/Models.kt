package yt.dsh.statuswear.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Odpowiedź z GET /api/status-page/{slug}
 * Zawiera konfigurację strony, incydenty i grupy z listą monitorów.
 */
@Serializable
data class StatusPageResponse(
    val config: StatusPageConfig? = null,
    val incidents: List<IncidentResponse> = emptyList(),
    val publicGroupList: List<MonitorGroup> = emptyList()
)

@Serializable
data class StatusPageConfig(
    val slug: String? = null,
    val title: String? = null
)

@Serializable
data class IncidentResponse(
    val id: Int,
    val title: String = "",
    val content: String = "",
    val style: String = "info",
    val pin: Boolean = false,
    val active: Boolean = true,
    val createdDate: String? = null,
    val lastUpdatedDate: String? = null,
    @SerialName("status_page_id") val statusPageId: Int? = null
)

@Serializable
data class MonitorGroup(
    val id: Int? = null,
    val name: String = "",
    val weight: Int? = null,
    val monitorList: List<Monitor> = emptyList()
)

@Serializable
data class Monitor(
    val id: Int,
    val name: String = "",
    val sendUrl: Int? = null
)

/**
 * Odpowiedź z GET /api/status-page/heartbeat/{slug}
 * heartbeatList: monitorId (jako String) -> lista ostatnich heartbeatów
 * uptimeList: "{monitorId}_24" -> uptime 24h jako ułamek (0.0 - 1.0)
 */
@Serializable
data class HeartbeatResponse(
    val heartbeatList: Map<String, List<Heartbeat>> = emptyMap(),
    val uptimeList: Map<String, Double> = emptyMap()
)

@Serializable
data class Heartbeat(
    val status: Int, // 0 = down, 1 = up, 2 = pending, 3 = maintenance
    val time: String = "",
    val msg: String? = null,
    val ping: Double? = null
)

/** Model widoku dla incydentu. */
data class IncidentUiState(
    val id: Int,
    val title: String,
    val content: String,
    val style: String = "info",
    val createdDate: String? = null,
    val pin: Boolean = false,
    val active: Boolean = true
)

/** Model widoku - kategoria (grupa) monitorów. */
data class MonitorGroupUiState(
    val id: Int?,
    val name: String,
    val monitors: List<MonitorUiState>
)

/** Model widoku - połączone dane monitora gotowe do wyświetlenia. */
data class MonitorUiState(
    val id: Int,
    val name: String,
    val status: MonitorStatus,
    val uptime24h: Double? = null,
    val lastMsg: String? = null
)

enum class MonitorStatus {
    UP, DOWN, PENDING, MAINTENANCE, UNKNOWN;

    companion object {
        fun fromCode(code: Int?): MonitorStatus = when (code) {
            0 -> DOWN
            1 -> UP
            2 -> PENDING
            3 -> MAINTENANCE
            else -> UNKNOWN
        }
    }
}

data class DshStatusUiState(
    val pageTitle: String = "Stan Usług DSH",
    val groups: List<MonitorGroupUiState> = emptyList(),
    val incidents: List<IncidentUiState> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val lastUpdated: Long? = null
) {
    /** Wszystkie monitory ze wszystkich grup */
    val allMonitors: List<MonitorUiState>
        get() = groups.flatMap { it.monitors }

    /** Kompatybilność z odwołaniami do .monitors */
    val monitors: List<MonitorUiState>
        get() = allMonitors

    val hasData: Boolean
        get() = groups.isNotEmpty() || incidents.isNotEmpty()

    val overallStatus: MonitorStatus
        get() = when {
            allMonitors.isEmpty() -> MonitorStatus.UNKNOWN
            allMonitors.any { it.status == MonitorStatus.DOWN } -> MonitorStatus.DOWN
            allMonitors.any { it.status == MonitorStatus.PENDING } -> MonitorStatus.PENDING
            allMonitors.all { it.status == MonitorStatus.UP } -> MonitorStatus.UP
            else -> MonitorStatus.UNKNOWN
        }
}
