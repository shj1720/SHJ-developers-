package com.example.model

import androidx.compose.ui.graphics.Color

enum class SHJTheme(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val onSurfaceColor: Color,
    val accentGlow: Color
) {
    CYBERPUNK_NEON(
        title = "Cyberpunk Neon",
        subtitle = "High-tech synthwave with neon teal & hot magenta",
        primaryColor = Color(0xFF00F5D4),
        secondaryColor = Color(0xFFF72585),
        backgroundColor = Color(0xFF0A0E17),
        surfaceColor = Color(0xFF121826),
        surfaceVariantColor = Color(0xFF1B2438),
        onSurfaceColor = Color(0xFFF0F4FC),
        accentGlow = Color(0xFF7209B7)
    ),
    MIDNIGHT_OLED(
        title = "Midnight OLED",
        subtitle = "Deep true black with electric cyan accents",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF818CF8),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF0C0F14),
        surfaceVariantColor = Color(0xFF181E29),
        onSurfaceColor = Color(0xFFFFFFFF),
        accentGlow = Color(0xFF0284C7)
    ),
    LOFI_CASSETTE(
        title = "Vintage Lo-Fi",
        subtitle = "Warm analog tape tones with amber & cream",
        primaryColor = Color(0xFFF4A261),
        secondaryColor = Color(0xFFE76F51),
        backgroundColor = Color(0xFF1A1412),
        surfaceColor = Color(0xFF271F1B),
        surfaceVariantColor = Color(0xFF382D27),
        onSurfaceColor = Color(0xFFF7EDE2),
        accentGlow = Color(0xFFE9C46A)
    ),
    SUNSET_HORIZON(
        title = "Sunset Horizon",
        subtitle = "Velvet dusk with coral pink & golden sun rays",
        primaryColor = Color(0xFFFF6B6B),
        secondaryColor = Color(0xFFFFD166),
        backgroundColor = Color(0xFF181024),
        surfaceColor = Color(0xFF231735),
        surfaceVariantColor = Color(0xFF33224C),
        onSurfaceColor = Color(0xFFFDE8E9),
        accentGlow = Color(0xFF9D4EDD)
    ),
    EMERALD_SYNTH(
        title = "Emerald Synth",
        subtitle = "Deep jade obsidian with luminous mint audio glow",
        primaryColor = Color(0xFF2EC4B6),
        secondaryColor = Color(0xFF70E000),
        backgroundColor = Color(0xFF061410),
        surfaceColor = Color(0xFF0D251E),
        surfaceVariantColor = Color(0xFF15392F),
        onSurfaceColor = Color(0xFFE8F8F5),
        accentGlow = Color(0xFF00F5D4)
    ),
    ROSE_QUARTZ(
        title = "Rose Quartz & Slate",
        subtitle = "Midnight amethyst with soft rose gold & lavender",
        primaryColor = Color(0xFFFFB5A7),
        secondaryColor = Color(0xFFDDA15E),
        backgroundColor = Color(0xFF16121D),
        surfaceColor = Color(0xFF221D2C),
        surfaceVariantColor = Color(0xFF322B40),
        onSurfaceColor = Color(0xFFFFF0F3),
        accentGlow = Color(0xFFB5179E)
    )
}
