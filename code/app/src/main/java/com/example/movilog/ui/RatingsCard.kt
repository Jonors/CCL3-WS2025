package com.example.movilog.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@Composable
fun RatingsCard(
    modifier: Modifier,
    cardBg: Color,
    userRating: Float
) {
    Card(
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Ratings", color = Color.White, style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxSize()) {
                // Graph
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    RatingBarsGraph(
                        modifier = Modifier.fillMaxSize(),
                        // For now: create a "nice" looking distribution.
                        // Later: replace with your own user's rating history distribution.
                        bars = pseudoDistribution(userRating)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("User rating", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        text = String.format("%.1f", userRating),
                        color = Color(0xFFF2B400),
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBarsGraph(modifier: Modifier, bars: List<Float>) {
    val barColor = Color(0xFFF2B400)
    val bgBar = Color.White.copy(alpha = 0.18f)

    Canvas(modifier = modifier) {
        val count = bars.size
        val gap = 10.dp.toPx()
        val totalGap = gap * (count - 1)
        val barWidth = (size.width - totalGap) / count

        val maxVal = max(1f, bars.maxOrNull() ?: 1f)

        for (i in 0 until count) {
            val x = i * (barWidth + gap)
            val normalized = bars[i] / maxVal
            val h = size.height * (0.1f + 0.9f * normalized) // avoid super tiny bars

            // background bar
            drawRoundRect(
                color = bgBar,
                topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )

            // filled bar
            drawRoundRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - h),
                size = androidx.compose.ui.geometry.Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )
        }
    }
}

private fun pseudoDistribution(userRating: Float): List<Float> {
    // userRating 0..10 -> peak somewhere around it
    val peak = (min(10f, max(0f, userRating)) / 10f) * 9f
    return List(10) { i ->
        val dist = 1f / (1f + (i - peak) * (i - peak))
        dist + Random(i).nextFloat() * 0.08f
    }
}
