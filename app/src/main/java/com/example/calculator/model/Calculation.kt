package com.example.calculator.model

import java.util.UUID

data class Calculation(
    val id: String = UUID.randomUUID().toString(),
    val totalBill: Double,
    val people: Int,
    val tipPercent: Double = 15.0,
    val timestamp: Long = System.currentTimeMillis()
) {
    val tipAmount: Double get() = totalBill * (tipPercent / 100)
    val totalWithTip: Double get() = totalBill + tipAmount
    val perPerson: Double get() = totalWithTip / people
}
