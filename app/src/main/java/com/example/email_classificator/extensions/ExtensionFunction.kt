package com.example.email_classificator.extensions

import kotlin.math.roundToInt

fun Float.convertToRoundedPercentageAsString(): String {
    return (this * 100).roundToInt().toString() + " %"
}

fun Float.convertToRoundedInt(): Int {
    return (this * 100).roundToInt()
}