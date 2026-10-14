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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DefaultSoundPresets
import com.example.viewmodel.MusicPlayerViewModel

@Composable
fun EqualizerScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val bands by viewModel.eqBands.collectAsState()
    val bassBoost by viewModel.bassBoost.collectAsState()
    val virtualizer by viewModel.virtualizer.collectAsState()
    val presets by viewModel.soundPresets.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("equalizer_screen"),
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
                        text = "STUDIO EQUALIZER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.onSurfaceColor
                    )
                    Text(
                        text = "5-Band Graphic Parametric EQ & Sound Profiles",
                        fontSize = 12.sp,
                        color = theme.onSurfaceColor.copy(alpha = 0.65f)
                    )
                }

                Row {
                    // Reset button
                    IconButton(
                        onClick = {
                            viewModel.selectPreset(DefaultSoundPresets.presets[0]) // Flat
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset EQ",
                            tint = theme.primaryColor
                        )
                    }

                    // Save custom preset
                    IconButton(
                        onClick = {
                            newPresetName = "My Sound #${presets.count { it.isCustom } + 1}"
                            showSaveDialog = true
                        },
                        modifier = Modifier.testTag("save_custom_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Save Custom Preset",
                            tint = theme.secondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Profile Presets Carousel
            Text(
                text = "SOUND PROFILE PRESETS",
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
                presets.forEach { preset ->
                    val isSelected = preset.id == activePreset.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) theme.primaryColor.copy(alpha = 0.25f) else theme.surfaceColor)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) theme.primaryColor else theme.surfaceVariantColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectPreset(preset) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = theme.primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) theme.primaryColor else theme.onSurfaceColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Graphic Equalizer Sliders Container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FREQUENCY RESPONSE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.onSurfaceColor.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Range: -12 dB to +12 dB",
                            fontSize = 11.sp,
                            color = theme.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5 Band Sliders
                    bands.forEachIndexed { index, band ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = band.frequencyLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurfaceColor
                                )

                                val gainLabel = if (band.gainDb > 0) "+%.1f dB".format(band.gainDb) else "%.1f dB".format(band.gainDb)
                                Text(
                                    text = gainLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (band.gainDb > 0) theme.primaryColor else if (band.gainDb < 0) theme.secondaryColor else theme.onSurfaceColor.copy(alpha = 0.5f)
                                )
                            }

                            Slider(
                                value = band.gainDb,
                                onValueChange = { newDb ->
                                    viewModel.updateEqBand(index, newDb)
                                },
                                valueRange = -12f..12f,
                                colors = SliderDefaults.colors(
                                    thumbColor = theme.primaryColor,
                                    activeTrackColor = theme.primaryColor,
                                    inactiveTrackColor = theme.surfaceVariantColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("eq_slider_band_$index")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost & 3D Virtualizer Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bass Boost Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Bass",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BASS BOOST",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(bassBoost * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.onSurfaceColor
                        )

                        Slider(
                            value = bassBoost,
                            onValueChange = { viewModel.setBassBoost(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.primaryColor,
                                activeTrackColor = theme.primaryColor,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bass_boost_slider")
                        )
                    }
                }

                // 3D Virtualizer Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = "Virtualizer",
                                tint = theme.secondaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "3D SPATIAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.secondaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(virtualizer * 100).toInt()}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = theme.onSurfaceColor
                        )

                        Slider(
                            value = virtualizer,
                            onValueChange = { viewModel.setVirtualizer(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = theme.secondaryColor,
                                activeTrackColor = theme.secondaryColor,
                                inactiveTrackColor = theme.surfaceVariantColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("virtualizer_slider")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Save Custom Preset Dialog
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = {
                    Text(
                        text = "Save Custom Sound Profile",
                        fontWeight = FontWeight.Bold,
                        color = theme.onSurfaceColor
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Save the current 5-band Equalizer, Bass Boost, and 3D Virtualizer settings as your personal sound preset.",
                            fontSize = 13.sp,
                            color = theme.onSurfaceColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = newPresetName,
                            onValueChange = { newPresetName = it },
                            label = { Text("Profile Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primaryColor,
                                focusedLabelColor = theme.primaryColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_preset_name_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPresetName.isNotBlank()) {
                                viewModel.saveCustomPreset(newPresetName.trim())
                                showSaveDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        modifier = Modifier.testTag("confirm_save_preset_button")
                    ) {
                        Text("Save Preset", color = Color(0xFF0A0E17), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel", color = theme.onSurfaceColor.copy(alpha = 0.7f))
                    }
                },
                containerColor = theme.surfaceColor
            )
        }
    }
}
