package com.example.calculator.ui.state

data class InputState(
    val billAmount: String = "",
    val numPeople: String = "",
    val isInputValid: Boolean = false
)
