package com.ingamarin.campusgps.util

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Texto de una distancia: menos de 1000 m en metros enteros ("350 m"),
 * desde 1000 m en kilometros con un decimal ("1.5 km").
 */
fun formatearDistancia(metros: Float): String =
    if (metros < 1000f) {
        "${metros.roundToInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", metros / 1000f)
    }
