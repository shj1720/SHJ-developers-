package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.SHJTheme
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.CloudSyncScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LoFiTunerScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SpotifyHubScreen
import com.example.ui.screens.ThemesScreen
import com.example.ui.theme.Typography
import com.example.viewmodel.MusicPlayerViewModel
import com.example.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MusicPlayerViewModel = viewModel()
            val currentTheme by viewModel.currentTheme.collectAsState()

            val customColorScheme = darkColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                background = currentTheme.backgroundColor,
                surface = currentTheme.surfaceColor,
                surfaceVariant = currentTheme.surfaceVariantColor,
                onPrimary = Color(0xFF0A0E17),
                onSecondary = Color.White,
                onBackground = currentTheme.onSurfaceColor,
                onSurface = currentTheme.onSurfaceColor
            )

            MaterialTheme(
                colorScheme = customColorScheme,
                typography = Typography
            ) {
                SHJApp(viewModel = viewModel, currentTheme = currentTheme)
            }
        }
    }
}

@Composable
fun SHJApp(
    viewModel: MusicPlayerViewModel,
    currentTheme: SHJTheme
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val visualizerFrame by viewModel.visualizerFrame.collectAsState()

    // Handle back button to return to Player screen
    BackHandler(enabled = activeTab != NavigationTab.PLAYER) {
        viewModel.setActiveTab(NavigationTab.PLAYER)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = currentTheme.backgroundColor,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .background(currentTheme.backgroundColor)
            ) {
                // Docked MiniPlayer when on non-Player screens
                if (activeTab != NavigationTab.PLAYER) {
                    MiniPlayerBar(
                        track = currentTrack,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        visualizerFrame = visualizerFrame,
                        theme = currentTheme,
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onSkipNext = { viewModel.playNextTrack() },
                        onClick = { viewModel.setActiveTab(NavigationTab.PLAYER) }
                    )
                }

                // Global Navigation Bar
                SHJNavigationBar(
                    activeTab = activeTab,
                    theme = currentTheme,
                    onTabSelected = { viewModel.setActiveTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                NavigationTab.PLAYER -> PlayerScreen(viewModel = viewModel)
                NavigationTab.LIBRARY -> LibraryScreen(viewModel = viewModel)
                NavigationTab.EQUALIZER -> EqualizerScreen(viewModel = viewModel)
                NavigationTab.LOFI -> LoFiTunerScreen(viewModel = viewModel)
                NavigationTab.SPOTIFY -> SpotifyHubScreen(viewModel = viewModel)
                NavigationTab.CLOUD -> CloudSyncScreen(viewModel = viewModel)
                NavigationTab.THEMES -> ThemesScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val tab: NavigationTab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun SHJNavigationBar(
    activeTab: NavigationTab,
    theme: SHJTheme,
    onTabSelected: (NavigationTab) -> Unit
) {
    val items = listOf(
        NavItem(NavigationTab.PLAYER, "Player", Icons.Default.PlayCircle),
        NavItem(NavigationTab.LIBRARY, "Library", Icons.Default.QueueMusic),
        NavItem(NavigationTab.EQUALIZER, "EQ", Icons.Default.Equalizer),
        NavItem(NavigationTab.LOFI, "Lo-Fi", Icons.Default.Radio),
        NavItem(NavigationTab.SPOTIFY, "Spotify", Icons.Default.Share),
        NavItem(NavigationTab.CLOUD, "Cloud", Icons.Default.CloudSync),
        NavItem(NavigationTab.THEMES, "Themes", Icons.Default.Palette)
    )

    ScrollableTabRow(
        selectedTabIndex = items.indexOfFirst { it.tab == activeTab }.coerceAtLeast(0),
        containerColor = theme.surfaceColor,
        contentColor = theme.primaryColor,
        edgePadding = 8.dp,
        indicator = { tabPositions ->
            val index = items.indexOfFirst { it.tab == activeTab }.coerceAtLeast(0)
            if (index in tabPositions.indices) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = theme.primaryColor
                )
            }
        },
        divider = {}
    ) {
        items.forEach { item ->
            val isSelected = item.tab == activeTab
            Tab(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .testTag("nav_tab_${item.tab.name.lowercase()}"),
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) theme.primaryColor else theme.onSurfaceColor.copy(alpha = 0.5f)
                        )
                    }
                }
            )
        }
    }
}
