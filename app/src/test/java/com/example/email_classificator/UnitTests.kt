package com.example.email_classificator

import com.example.email_classificator.extensions.convertToRoundedInt
import com.example.email_classificator.extensions.convertToRoundedPercentageAsString
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTests {

    @Test
    fun convertToRoundedInt() {
        val floatNumber = 0.12345F
        assertEquals(floatNumber.convertToRoundedInt(), 12)
    }

    @Test
    fun convertToRoundedPercentageAsString() {
        val floatNumber = 0.12345F
        assertEquals(floatNumber.convertToRoundedPercentageAsString(), "12 %")
    }
}