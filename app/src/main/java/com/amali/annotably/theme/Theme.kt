package com.amali.annotably.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import com.amali.annotably.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple400,
    onPrimary = AccentWhite,
    primaryContainer = Purple700,
    onPrimaryContainer = Purple50,

    secondary = Amber500,
    onSecondary = Purple900,
    secondaryContainer = AccentOrange,
    onSecondaryContainer = AccentWhite,

    tertiary = AccentBlue,
    onTertiary = AccentWhite,
    tertiaryContainer = Purple700,
    onTertiaryContainer = Purple50,

    background = Grey900,
    onBackground = Grey50,
    surface = Grey800,
    onSurface = Grey50,
    surfaceVariant = Grey700,
    onSurfaceVariant = Grey500,
    outline = Grey600,

    error = AccentRed,
    onError = AccentWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple500,
    onPrimary = AccentWhite,
    primaryContainer = Purple100,
    onPrimaryContainer = Purple800,

    secondary = Amber500,
    onSecondary = Purple900,
    secondaryContainer = AccentYellow,
    onSecondaryContainer = Purple900,

    tertiary = AccentBlue,
    onTertiary = AccentWhite,
    tertiaryContainer = Purple50,
    onTertiaryContainer = Purple700,

    background = Grey50,
    onBackground = Grey900,
    surface = Grey50,
    onSurface = Grey900,
    surfaceVariant = Grey200,
    onSurfaceVariant = Grey700,
    outline = Grey400,

    error = AccentRed,
    onError = AccentWhite,
)

@Composable
fun AnnotablyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {


    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}