package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CalculatorUiState(
    val displayText: String = "0",
    val equationText: String = "",
    val historyList: List<String> = emptyList(),
    val activeOperator: String? = null
)

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private var operand1: Double? = null
    private var pendingOperator: String? = null
    private var isNewInput: Boolean = true

    fun onNumberClick(digit: String) {
        _uiState.update { state ->
            val updatedDisplay = if (isNewInput || state.displayText == "0") {
                digit
            } else {
                state.displayText + digit
            }
            isNewInput = false
            state.copy(displayText = updatedDisplay)
        }
    }

    fun onDecimalClick() {
        _uiState.update { state ->
            val current = state.displayText
            val updatedDisplay = if (isNewInput) {
                "0."
            } else if (current.contains(".")) {
                current
            } else {
                current + "."
            }
            isNewInput = false
            state.copy(displayText = updatedDisplay)
        }
    }

    fun onOperatorClick(operator: String) {
        val currentVal = _uiState.value.displayText.toDoubleOrNull() ?: return

        // If an operator is pressed and we already have operand1 and a pending operator,
        // calculate intermediate result as standard calculators do.
        if (operand1 != null && pendingOperator != null && !isNewInput) {
            calculateIntermediate(currentVal)
        } else {
            operand1 = currentVal
        }

        pendingOperator = operator
        isNewInput = true

        _uiState.update { state ->
            val formattedOp1 = formatResult(operand1 ?: 0.0)
            state.copy(
                equationText = "$formattedOp1 $operator",
                activeOperator = operator
            )
        }
    }

    private fun calculateIntermediate(currentVal: Double) {
        val op1 = operand1 ?: return
        val op = pendingOperator ?: return
        val result = performOperation(op1, currentVal, op)
        operand1 = result
    }

    fun onEqualClick() {
        val op1 = operand1 ?: return
        val op = pendingOperator ?: return
        val currentVal = _uiState.value.displayText.toDoubleOrNull() ?: return

        val result = performOperation(op1, currentVal, op)
        val formattedOp1 = formatResult(op1)
        val formattedOp2 = formatResult(currentVal)
        val formattedResult = formatResult(result)

        val historyEntry = "$formattedOp1 $op $formattedOp2 = $formattedResult"

        _uiState.update { state ->
            val newHistory = if (formattedResult != "Error") {
                listOf(historyEntry) + state.historyList
            } else {
                state.historyList
            }
            state.copy(
                displayText = formattedResult,
                equationText = "",
                activeOperator = null,
                historyList = newHistory.take(50) // Maintain last 50 calculations
            )
        }

        operand1 = if (formattedResult != "Error") result else null
        pendingOperator = null
        isNewInput = true
    }

    fun onClearClick() {
        operand1 = null
        pendingOperator = null
        isNewInput = true
        _uiState.update {
            CalculatorUiState(historyList = it.historyList)
        }
    }

    fun onBackspaceClick() {
        _uiState.update { state ->
            val current = state.displayText
            if (isNewInput || current == "0" || current == "Error") {
                state
            } else {
                val updatedDisplay = if (current.length <= 1) {
                    "0"
                } else {
                    current.substring(0, current.length - 1)
                }
                state.copy(displayText = updatedDisplay)
            }
        }
    }

    fun onToggleSignClick() {
        _uiState.update { state ->
            val current = state.displayText
            if (current == "0" || current == "Error") {
                state
            } else {
                val updatedDisplay = if (current.startsWith("-")) {
                    current.substring(1)
                } else {
                    "-$current"
                }
                state.copy(displayText = updatedDisplay)
            }
        }
    }

    fun onPercentClick() {
        _uiState.update { state ->
            val current = state.displayText.toDoubleOrNull()
            if (current != null) {
                val result = current / 100.0
                isNewInput = true
                state.copy(displayText = formatResult(result))
            } else {
                state
            }
        }
    }

    fun clearHistory() {
        _uiState.update { state ->
            state.copy(historyList = emptyList())
        }
    }

    private fun performOperation(val1: Double, val2: Double, op: String): Double {
        return when (op) {
            "+" -> val1 + val2
            "-" -> val1 - val2
            "×" -> val1 * val2
            "÷" -> if (val2 == 0.0) Double.NaN else val1 / val2
            else -> val2
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val longVal = value.toLong()
        if (value == longVal.toDouble()) {
            return longVal.toString()
        }
        // Avoid precision anomalies (e.g. 0.1 + 0.2 = 0.3)
        val rounded = Math.round(value * 100000000.0) / 100000000.0
        val str = rounded.toString()
        return if (str.endsWith(".0")) {
            str.substring(0, str.length - 2)
        } else {
            str
        }
    }
}
