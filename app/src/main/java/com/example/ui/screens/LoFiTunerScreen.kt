package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ReverbRoom
import com.example.model.VintageLoFiTone
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun LoFiTunerScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val lofiSettings by viewModel.loFiSettings.collectAsState()
    val reverbRoom by viewModel.reverbRoom.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("lofi_tuner_screen"),
        color = theme.backgroundColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LO-FI & REVERB STUDIO",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.onSurfaceColor
                    )
                    Text(
                        text = "Tape Flutter • Vinyl Crackle • Acoustic Spaces",
                        fontSize = 12.sp,
                        color = theme.onSurfaceColor.copy(alpha = 0.65f)
                    )
                }

                Switch(
                    checked = lofiSettings.isEnabled,
                    onCheckedChange = { viewModel.setLoFiEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.primaryColor,
                        checkedTrackColor = theme.primaryColor.copy(alpha = 0.4f),
                        uncheckedThumbColor = theme.onSurfaceColor.copy(alpha = 0.5f),
                        uncheckedTrackColor = theme.surfaceVariantColor
                    ),
                    modifier = Modifier.testTag("master_lofi_toggle")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Acoustic Reverb Room Picker
            Text(
                text = "ENVIRONMENTAL REVERB SPACES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.secondaryColor,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = "Reverb",
                                tint = theme.secondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Room: ${reverbRoom.displayName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                        }

                        Text(
                            text = if (reverbRoom == ReverbRoom.OFF) "Bypassed" else "Decay: ${reverbRoom.decaySeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.secondaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reverb Room Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReverbRoom.values().forEach { room ->
                            val isSelected = room == reverbRoom
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) theme.secondaryColor.copy(alpha = 0.25f) else theme.surfaceVariantColor)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) theme.secondaryColor else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setReverbRoom(room) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = room.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) theme.secondaryColor else theme.onSurfaceColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Wet/Dry mix visual gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Acoustic Wet Mix",
                            fontSize = 11.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.6f),
                            modifier = Modifier.width(110.dp)
                        )
                        LinearProgressIndicator(
                            progress = { reverbRoom.wetMix },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = theme.secondaryColor,
                            trackColor = theme.surfaceVariantColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(reverbRoom.wetMix * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.secondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 2: Lo-Fi Tone Presets
            Text(
                text = "VINTAGE ANALOG TONES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = theme.primaryColor,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VintageLoFiTone.values().forEach { tone ->
                    val isSelected = tone == lofiSettings.activeTone
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.25f) else theme.surfaceColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.primaryColor else theme.surfaceVariantColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setLoFiTone(tone) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = tone.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) theme.primaryColor else theme.onSurfaceColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: Fine Analog Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ANALOG GRIT & MODULATION TUNERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Vinyl Crackle & Surface Noise
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vinyl Surface Crackle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = "${(lofiSettings.crackleVolume * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                        }
                        Slider(
                            value = lofiSettings.crackleVolume,
                            onValueChange = { viewModel.updateLoFiCrackle(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 2. Tape Flutter & Wow
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tape Flutter & Pitch Wow",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = "${(lofiSettings.tapeFlutter * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.secondaryColor
                            )
                        }
                        Slider(
                            value = lofiSettings.tapeFlutter,
                            onValueChange = { viewModel.updateLoFiFlutter(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.secondaryColor,
                                activeTrackColor = theme.secondaryColor,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Analog Warmth & Tube Drive
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tube Warmth & Saturation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = "${(lofiSettings.analogWarmth * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accentGlow
                            )
                        }
                        Slider(
                            value = lofiSettings.analogWarmth,
                            onValueChange = { viewModel.updateLoFiWarmth(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.accentGlow,
                                activeTrackColor = theme.accentGlow,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 4. Vintage Low-Pass Cutoff Filter
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Low-Pass Vintage Radio Filter",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.onSurfaceColor
                            )
                            Text(
                                text = "${lofiSettings.lowPassCutoffHz} Hz",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                        }
                        Slider(
                            value = lofiSettings.lowPassCutoffHz.toFloat(),
                            onValueChange = { viewModel.updateLoFiCutoff(it.toInt()) },
                            valueRange = 3000f..20000f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
