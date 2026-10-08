package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HospitalBlueDark,
    onPrimary = HospitalBlueOnDark,
    primaryContainer = HospitalBlueContainerDark,
    onPrimaryContainer = HospitalBlueContainer,
    secondary = HospitalSlateContainer,
    onSecondary = HospitalSlateOnContainer,
    tertiary = HospitalTealContainer,
    background = MedicalBackgroundDark,
    surface = MedicalSurfaceDark,
    surfaceVariant = MedicalSurfaceVariantDark,
    outline = MedicalOutlineDark,
    error = MedicalEmergencyRed,
    errorContainer = MedicalAlertContainer,
    onErrorContainer = MedicalAlertOnContainer
)

private val LightColorScheme = lightColorScheme(
    primary = HospitalBluePrimary,
    onPrimary = HospitalBlueOnPrimary,
    primaryContainer = HospitalBlueContainer,
    onPrimaryContainer = HospitalBlueOnContainer,
    secondary = HospitalSlateSecondary,
    onSecondary = HospitalSlateOnSecondary,
    secondaryContainer = HospitalSlateContainer,
    onSecondaryContainer = HospitalSlateOnContainer,
    tertiary = HospitalTealTertiary,
    onTertiary = HospitalTealOnTertiary,
    tertiaryContainer = HospitalTealContainer,
    onTertiaryContainer = HospitalTealOnContainer,
    background = MedicalBackgroundLight,
    surface = MedicalSurfaceLight,
    surfaceVariant = MedicalSurfaceVariantLight,
    outline = MedicalOutlineLight,
    error = MedicalEmergencyRed,
    errorContainer = MedicalAlertContainer,
    onErrorContainer = MedicalAlertOnContainer
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep distinct medical theme styling
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
