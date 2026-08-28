package yt.dsh.statuswear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import yt.dsh.statuswear.data.DshStatusUiState
import yt.dsh.statuswear.data.IncidentUiState
import yt.dsh.statuswear.data.MonitorStatus
import yt.dsh.statuswear.data.MonitorUiState
import yt.dsh.statuswear.ui.theme.BackgroundDark
import yt.dsh.statuswear.ui.theme.StatusDown
import yt.dsh.statuswear.ui.theme.StatusMaintenance
import yt.dsh.statuswear.ui.theme.StatusPending
import yt.dsh.statuswear.ui.theme.StatusUnknown
import yt.dsh.statuswear.ui.theme.StatusUp
import yt.dsh.statuswear.ui.theme.SurfaceDark
import yt.dsh.statuswear.ui.theme.TextPrimary
import yt.dsh.statuswear.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusScreen(viewModel: StatusViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    } else {
        Scaffold(
            timeText = { TimeText() },
            positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
        ) {
            StatusContent(
                uiState = uiState,
                listState = listState,
                onRefresh = { viewModel.refresh() },
                onOpenAbout = { showAboutDialog = true }
            )
        }
    }
}

@Composable
private fun StatusContent(
    uiState: DshStatusUiState,
    listState: ScalingLazyListState,
    onRefresh: () -> Unit,
    onOpenAbout: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 28.dp, bottom = 48.dp, start = 8.dp, end = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item(key = "overall_header") {
            OverallStatusHeader(uiState = uiState, onRefresh = onRefresh)
        }

        if (uiState.error != null && !uiState.hasData) {
            item(key = "error_card") {
                ErrorCard(message = uiState.error, onRetry = onRefresh)
            }
        }

        if (uiState.isLoading && !uiState.hasData) {
            item(key = "loading_indicator") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }

        // Sekcja incydentów
        if (uiState.incidents.isNotEmpty()) {
            item(key = "incidents_section_header") {
                ListHeader(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                    Text(
                        text = "Incydenty (${uiState.incidents.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusDown
                    )
                }
            }

            items(uiState.incidents, key = { "incident_${it.id}" }) { incident ->
                IncidentCard(incident = incident)
            }
        }

        // Sekcje kategorii i monitorów
        if (uiState.groups.isNotEmpty()) {
            uiState.groups.forEach { group ->
                item(key = "group_header_${group.id ?: group.name}") {
                    CategoryHeader(title = group.name)
                }

                items(group.monitors, key = { "monitor_${it.id}" }) { monitor ->
                    MonitorRow(monitor = monitor)
                }
            }
        }

        // Przyciski akcji na dole listy (odświeżanie + info)
        if (uiState.hasData) {
            item(key = "footer_actions") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.lastUpdated != null) {
                        Text(
                            text = "Zaktualizowano: ${formatTime(uiState.lastUpdated)}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompactChip(
                            onClick = onRefresh,
                            colors = ChipDefaults.secondaryChipColors(),
                            label = {
                                Text(
                                    text = if (uiState.isLoading) "Odświeżanie…" else "Odśwież",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            icon = {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = "Odśwież",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )

                        CompactChip(
                            onClick = onOpenAbout,
                            colors = ChipDefaults.secondaryChipColors(),
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = "Informacje o aplikacji",
                                    modifier = Modifier.size(16.dp),
                                    tint = StatusMaintenance
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallStatusHeader(uiState: DshStatusUiState, onRefresh: () -> Unit) {
    val (color, label) = statusColorAndLabel(uiState.overallStatus)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRefresh),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            StatusIcon(status = uiState.overallStatus, tint = color, size = 28.dp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uiState.pageTitle,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CategoryHeader(title: String) {
    ListHeader(
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IncidentCard(incident: IncidentUiState) {
    var expanded by remember { mutableStateOf(false) }
    val (styleColor, icon) = incidentStyleAndIcon(incident.style)
    val formattedDate = remember(incident.createdDate) {
        formatDate(incident.createdDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(1.dp, styleColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = styleColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = incident.title.ifBlank { "Incydent #${incident.id}" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = if (expanded) 10 else 2,
                overflow = TextOverflow.Ellipsis
            )
            if (incident.pin) {
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = "Przypięty",
                    tint = styleColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        if (!formattedDate.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formattedDate,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }

        if (incident.content.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = incident.content,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = if (expanded) 50 else 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "Zwiń" else "Więcej",
                fontSize = 10.sp,
                color = styleColor,
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = styleColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun MonitorRow(monitor: MonitorUiState) {
    val (color, label) = statusColorAndLabel(monitor.status)
    val uptimeText = monitor.uptime24h?.let { "${"%.1f".format(it * 100)}% / 24h" }
    val secondaryText = if (monitor.status == MonitorStatus.DOWN && !monitor.lastMsg.isNullOrBlank()) {
        monitor.lastMsg
    } else {
        uptimeText ?: label
    }

    Chip(
        onClick = { /* Informacyjny kafelek */ },
        colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface),
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(
                text = monitor.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryLabel = {
            Text(
                text = secondaryText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    )
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Chip(
        onClick = onRetry,
        colors = ChipDefaults.chipColors(backgroundColor = StatusDown.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text(text = "Błąd pobierania", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        },
        secondaryLabel = {
            Text(text = "$message — dotknij, aby ponowić", fontSize = 10.sp, maxLines = 2)
        },
        icon = {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = null, tint = StatusDown)
        }
    )
}

@Composable
private fun StatusIcon(status: MonitorStatus, tint: Color, size: androidx.compose.ui.unit.Dp) {
    val icon = when (status) {
        MonitorStatus.UP -> Icons.Filled.CheckCircle
        MonitorStatus.DOWN -> Icons.Filled.Warning
        MonitorStatus.MAINTENANCE -> Icons.Filled.Build
        MonitorStatus.PENDING -> Icons.Filled.Warning
        MonitorStatus.UNKNOWN -> Icons.Filled.QuestionMark
    }
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
}

private fun incidentStyleAndIcon(style: String?): Pair<Color, ImageVector> = when (style?.lowercase()) {
    "danger", "error" -> StatusDown to Icons.Filled.Warning
    "warning" -> StatusPending to Icons.Filled.Warning
    "success" -> StatusUp to Icons.Filled.CheckCircle
    "info", "primary" -> StatusMaintenance to Icons.Filled.Info
    else -> StatusMaintenance to Icons.Filled.Info
}

private fun formatDate(rawDate: String?): String? {
    if (rawDate.isNullOrBlank()) return null
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = inputFormat.parse(rawDate)
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } else {
            rawDate
        }
    } catch (_: Exception) {
        rawDate
    }
}

private fun formatTime(timestampMs: Long?): String {
    if (timestampMs == null || timestampMs <= 0L) return "--:--"
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestampMs))
}

private fun statusColorAndLabel(status: MonitorStatus): Pair<Color, String> = when (status) {
    MonitorStatus.UP -> StatusUp to "Wszystko działa"
    MonitorStatus.DOWN -> StatusDown to "Awaria"
    MonitorStatus.PENDING -> StatusPending to "Sprawdzanie…"
    MonitorStatus.MAINTENANCE -> StatusMaintenance to "Konserwacja"
    MonitorStatus.UNKNOWN -> StatusUnknown to "Brak danych"
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            state = listState,
            contentPadding = PaddingValues(top = 24.dp, bottom = 36.dp, start = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(StatusMaintenance.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = StatusMaintenance,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            item {
                Text(
                    text = "O aplikacji",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )
            }

            item {
                Text(
                    text = "Aplikacja została wykonana przez Shieldziak przy pomocy narzędzi AI, Claude oraz Gemini.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                CompactChip(
                    onClick = onDismiss,
                    colors = ChipDefaults.primaryChipColors(),
                    label = {
                        Text(
                            text = "Zamknij",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Zamknij",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

