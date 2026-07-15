package com.example.kidtubelock.feature_parent.timelimit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kidtubelock.app.ui.theme.BrandBlue
import com.example.kidtubelock.app.ui.theme.BrandGreen
import com.example.kidtubelock.app.ui.theme.BrandYellow
import com.example.kidtubelock.domain.model.TimeLimitProgress
import com.example.kidtubelock.feature_parent.common.InfoNote
import com.example.kidtubelock.feature_parent.common.ParentScreenBackground
import com.example.kidtubelock.feature_parent.common.parentPrimaryButtonColors

private val timeLimitPresets = listOf<Int?>(null, 15, 30, 45, 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLimitsScreen(
    viewModel: TimeLimitsViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
                ),
                title = { Text("Time Limits") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        ParentScreenBackground(
            modifier = Modifier.padding(paddingValues),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(
                    modifier = Modifier.padding(top = 6.dp, start = 2.dp, end = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Set Time Limits",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Healthy screen time made easy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TimerPreviewPanel(
                    selectedMinutes = uiState.selectedMinutes,
                    timerFaceLabel = uiState.timerFaceLabel,
                    endAtLabel = uiState.endAtLabel,
                    summaryLabel = uiState.summaryLabel,
                )

                Card(
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Choose an optional time limit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "This is a saved parent preference, not a required timer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        TimeLimitChoiceRows(
                            selectedMinutes = uiState.selectedMinutes,
                            onSelect = viewModel::onTimeLimitSelected,
                        )
                    }
                }

                InfoNote(
                    title = "Optional for this pass",
                    body = "This saves the parent preference and preview. Child mode does not automatically stop playback yet.",
                )

                Button(
                    onClick = viewModel::saveTimeLimit,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.hasChanges && !uiState.isSaving,
                    colors = parentPrimaryButtonColors(),
                ) {
                    Text(
                        if (uiState.isSaving) {
                            "Saving..."
                        } else if (uiState.selectedMinutes == null) {
                            "Save With No Limit"
                        } else {
                            "Save ${uiState.summaryLabel} Limit"
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerPreviewPanel(
    selectedMinutes: Int?,
    timerFaceLabel: String,
    endAtLabel: String,
    summaryLabel: String,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Screen Timer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.size(260.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TimerRing(
                        progress = TimeLimitProgress.normalized(selectedMinutes),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = timerFaceLabel,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = if (timerFaceLabel == "--:--") "No time limit" else "Time Remaining",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Text(
                text = endAtLabel,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f),
                ),
            ) {
                Text(
                    text = if (summaryLabel == "Off") "Limit is turned off" else "$summaryLabel selected",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 14.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun TimeLimitChoiceRows(
    selectedMinutes: Int?,
    onSelect: (Int?) -> Unit,
) {
    val rows = listOf(
        listOf<Int?>(null, 15),
        listOf<Int?>(30, 45),
        listOf<Int?>(60),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { minutes ->
                    TimeLimitChoiceCard(
                        modifier = Modifier.weight(1f),
                        label = minutes?.let { "$it min" } ?: "Off",
                        selected = selectedMinutes == minutes,
                        onClick = { onSelect(minutes) },
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimerRing(
    progress: Float,
    trackColor: androidx.compose.ui.graphics.Color,
) {
    val segments = listOf(
        RingSegment(color = BrandBlue, ratio = 0.2f),
        RingSegment(color = BrandGreen, ratio = 0.45f),
        RingSegment(color = BrandYellow, ratio = 0.35f),
    )

    Canvas(
        modifier = Modifier.fillMaxSize(),
    ) {
        val startAngle = 140f
        val totalSweep = 260f
        val strokeWidth = 24.dp.toPx()
        val inset = strokeWidth / 2
        val size = size.minDimension - strokeWidth
        val topLeft = Offset(inset, inset)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

        drawArc(
            color = trackColor,
            startAngle = startAngle,
            sweepAngle = totalSweep,
            useCenter = false,
            topLeft = topLeft,
            size = androidx.compose.ui.geometry.Size(size, size),
            style = stroke,
        )

        var currentStartAngle = startAngle
        var remainingSweep = totalSweep * progress.coerceIn(0f, 1f)

        segments.forEach { segment ->
            if (remainingSweep <= 0f) return@forEach

            val segmentSweep = totalSweep * segment.ratio
            val drawnSweep = remainingSweep.coerceAtMost(segmentSweep)

            drawArc(
                color = segment.color,
                startAngle = currentStartAngle,
                sweepAngle = drawnSweep,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(size, size),
                style = stroke,
            )

            currentStartAngle += segmentSweep
            remainingSweep -= drawnSweep
        }
    }
}

private data class RingSegment(
    val color: Color,
    val ratio: Float,
)

@Composable
private fun TimeLimitChoiceCard(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.34f)
            },
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.84f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
