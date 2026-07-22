package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.MathContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var currentInput = "0"
    private var previousValue: BigDecimal? = null
    private var pendingOperator: Char? = null
    private var expressionText = ""
    private var justEvaluated = false
    private var hasError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapOf(
            binding.btn0 to "0", binding.btn1 to "1", binding.btn2 to "2",
            binding.btn3 to "3", binding.btn4 to "4", binding.btn5 to "5",
            binding.btn6 to "6", binding.btn7 to "7", binding.btn8 to "8",
            binding.btn9 to "9"
        ).forEach { (button, digit) -> button.setOnClickListener { onDigit(digit) } }

        binding.btnPlus.setOnClickListener { onOperator('+') }
        binding.btnMinus.setOnClickListener { onOperator('-') }
        binding.btnMultiply.setOnClickListener { onOperator('×') }
        binding.btnDivide.setOnClickListener { onOperator('÷') }
        binding.btnDot.setOnClickListener { onDot() }
        binding.btnEquals.setOnClickListener { onEquals() }
        binding.btnClear.setOnClickListener { onClear() }
        binding.btnBackspace.setOnClickListener { onBackspace() }
        binding.btnPercent.setOnClickListener { onPercent() }

        updateDisplay()
    }

    private fun resetIfJustEvaluated() {
        if (justEvaluated) {
            currentInput = "0"
            expressionText = ""
            previousValue = null
            pendingOperator = null
            justEvaluated = false
        }
    }

    private fun onDigit(digit: String) {
        if (hasError) {
            onClear()
        }
        resetIfJustEvaluated()
        currentInput = if (currentInput == "0") digit else currentInput + digit
        updateDisplay()
    }

    private fun onDot() {
        if (hasError) {
            onClear()
        }
        resetIfJustEvaluated()
        if (!currentInput.contains(".")) {
            currentInput += "."
        }
        updateDisplay()
    }

    private fun onOperator(op: Char) {
        if (hasError) {
            onClear()
            return
        }
        justEvaluated = false
        val inputValue = currentInput.toBigDecimal()
        val result = if (previousValue != null && pendingOperator != null) {
            compute(previousValue!!, inputValue, pendingOperator!!)
        } else {
            inputValue
        }
        if (result == null) {
            showError()
            return
        }
        previousValue = result
        pendingOperator = op
        expressionText = "${formatNumber(result)} $op"
        currentInput = "0"
        updateDisplay()
    }

    private fun onEquals() {
        if (hasError) {
            onClear()
            return
        }
        val op = pendingOperator ?: return
        val prev = previousValue ?: return
        val inputValue = currentInput.toBigDecimal()
        expressionText = "${formatNumber(prev)} $op ${formatNumber(inputValue)} ="
        val result = compute(prev, inputValue, op)
        if (result == null) {
            showError()
            return
        }
        currentInput = formatNumber(result)
        previousValue = null
        pendingOperator = null
        justEvaluated = true
        updateDisplay()
    }

    private fun onPercent() {
        if (hasError) {
            onClear()
            return
        }
        val value = currentInput.toBigDecimal()
        val result = if (previousValue != null) {
            previousValue!!.multiply(value).divide(BigDecimal(100), MathContext.DECIMAL64)
        } else {
            value.divide(BigDecimal(100), MathContext.DECIMAL64)
        }
        currentInput = formatNumber(result)
        updateDisplay()
    }

    private fun onClear() {
        currentInput = "0"
        previousValue = null
        pendingOperator = null
        expressionText = ""
        justEvaluated = false
        hasError = false
        updateDisplay()
    }

    private fun onBackspace() {
        if (hasError) {
            onClear()
            return
        }
        if (justEvaluated) {
            onClear()
            return
        }
        currentInput = if (currentInput.length <= 1) "0" else currentInput.dropLast(1)
        updateDisplay()
    }

    private fun compute(a: BigDecimal, b: BigDecimal, op: Char): BigDecimal? {
        return try {
            when (op) {
                '+' -> a.add(b)
                '-' -> a.subtract(b)
                '×' -> a.multiply(b)
                '÷' -> if (b.compareTo(BigDecimal.ZERO) == 0) null else a.divide(b, MathContext.DECIMAL64)
                else -> b
            }
        } catch (e: ArithmeticException) {
            null
        }
    }

    private fun showError() {
        hasError = true
        currentInput = "Error"
        expressionText = ""
        previousValue = null
        pendingOperator = null
        updateDisplay()
    }

    private fun formatNumber(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return if (stripped.scale() <= 0) stripped.toBigInteger().toString() else stripped.toPlainString()
    }

    private fun updateDisplay() {
        binding.tvExpression.text = expressionText
        binding.tvDisplay.text = currentInput
    }
}
