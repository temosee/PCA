package com.example.calculator.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.calculator.model.Calculation
import com.example.calculator.ui.state.InputState

class SplitViewModel : ViewModel() {
    var state by mutableStateOf(InputState())
        private set

    private val _history = mutableStateListOf<Calculation>()
    val history: List<Calculation> get() = _history.takeLast(5).reversed()

    fun onEvent(event: SplitEvent) {
        when (event) {
            is SplitEvent.BillChanged -> {
                val newAmount = event.amount.replace(',', '.')
                state = state.copy(
                    billAmount = newAmount,
                    isInputValid = validate(newAmount, state.numPeople)
                )
            }
            is SplitEvent.PeopleChanged -> {
                state = state.copy(
                    numPeople = event.count,
                    isInputValid = validate(state.billAmount, event.count)
                )
            }
            SplitEvent.Reset -> {
                state = InputState()
            }
        }
    }

    private fun validate(bill: String, people: String): Boolean {
        val b = bill.toDoubleOrNull() ?: 0.0
        val p = people.toIntOrNull() ?: 0
        return b > 0 && p > 0
    }

    fun calculate(): String {
        val calc = Calculation(
            totalBill = state.billAmount.toDoubleOrNull() ?: 0.0,
            people = state.numPeople.toIntOrNull() ?: 1
        )
        if (_history.size >= 50) _history.removeAt(0)
        _history.add(calc)
        return calc.id
    }

    fun getCalculation(id: String?): Calculation? {
        return _history.find { it.id == id }
    }
}

sealed class SplitEvent {
    data class BillChanged(val amount: String) : SplitEvent()
    data class PeopleChanged(val count: String) : SplitEvent()
    object Reset : SplitEvent()
}
