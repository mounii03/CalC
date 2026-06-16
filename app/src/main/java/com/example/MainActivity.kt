package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    CalculatorApp(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorApp(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistory by remember { mutableStateOf(false) }

    // Adaptive box mapping the UI layout width (max 480.dp) to center nicely on tables/foldables
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 480.dp)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Calculator Header Toolbar
            HeaderToolbar(
                showingHistory = showHistory,
                onToggleHistory = { showHistory = !showHistory },
                onClearHistory = { viewModel.clearHistory() }
            )

            // Dynamic Display Panel Area
            DisplayPanel(
                equationText = uiState.equationText,
                displayText = uiState.displayText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            )

            // Animated Keyboard or History View panel transitions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
            ) {
                AnimatedContent(
                    targetState = showHistory,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = spring(dampingRatio = 0.85f),
                            initialOffsetY = { it }
                        ) togetherWith fadeOut(animationSpec = tween(150))
                    },
                    label = "keyboard_history_transition"
                ) { isHistoryOpen ->
                    if (isHistoryOpen) {
                        HistoryPanel(
                            historyList = uiState.historyList,
                            onHistoryItemClick = { value ->
                                viewModel.onClearClick() // Start fresh with the historic value
                                viewModel.onNumberClick(value)
                                showHistory = false
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        KeyboardPanel(
                            activeOperator = uiState.activeOperator,
                            onDigit = { viewModel.onNumberClick(it) },
                            onDecimal = { viewModel.onDecimalClick() },
                            onOperator = { viewModel.onOperatorClick(it) },
                            onEquals = { viewModel.onEqualClick() },
                            onClear = { viewModel.onClearClick() },
                            onBackspace = { viewModel.onBackspaceClick() },
                            onToggleSign = { viewModel.onToggleSignClick() },
                            onPercent = { viewModel.onPercentClick() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderToolbar(
    showingHistory: Boolean,
    onToggleHistory: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Calculator",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showingHistory) {
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History Logs",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            FilledTonalButton(
                onClick = onToggleHistory,
                modifier = Modifier.testTag("btn_history_toggle"),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (showingHistory) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (showingHistory) "Calculator" else "History",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (showingHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    }
}

@Composable
fun DisplayPanel(
    equationText: String,
    displayText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Formula equation buffer line (gray)
            Text(
                text = equationText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.End
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("display_equation")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Display (large, with adaptive font scaling)
            val textStyle = when {
                displayText.length > 12 -> MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp)
                displayText.length > 8 -> MaterialTheme.typography.displaySmall.copy(fontSize = 42.sp)
                else -> MaterialTheme.typography.displayLarge.copy(fontSize = 58.sp)
            }

            Text(
                text = displayText,
                style = textStyle.copy(
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.End,
                    fontFamily = FontFamily.SansSerif,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("display_result")
            )
        }
    }
}

@Composable
fun HistoryPanel(
    historyList: List<String>,
    onHistoryItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "Recent Calculations",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent calculations yet",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(historyList) { item ->
                        val resultStr = item.substringAfter("=").trim()
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHistoryItemClick(resultStr) }
                                .testTag("history_item_${resultStr}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                val equation = item.substringBefore("=").trim()
                                val result = item.substringAfter("=").trim()

                                Text(
                                    text = "$equation =",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.End
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (result == "Error") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.End
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardPanel(
    activeOperator: String?,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val numKeyBg = MaterialTheme.colorScheme.secondary
    val numKeyText = MaterialTheme.colorScheme.onSecondary
    val opKeyBg = MaterialTheme.colorScheme.primary
    val opKeyText = MaterialTheme.colorScheme.onPrimary
    val fnKeyBg = if (isDark) FnKeyBg else LightFnKeyBg
    val fnKeyText = if (isDark) Color.White else Color(0xFF4C1D95)
    val equalsKeyBg = MaterialTheme.colorScheme.tertiary
    val equalsKeyText = MaterialTheme.colorScheme.onTertiary

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: AC, +/-, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalcButton(
                text = "AC",
                backgroundColor = fnKeyBg,
                textColor = fnKeyText,
                onClick = onClear,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_clear")
            )
            CalcButton(
                text = "±",
                backgroundColor = fnKeyBg,
                textColor = fnKeyText,
                onClick = onToggleSign,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_toggle_sign")
            )
            CalcButton(
                text = "%",
                backgroundColor = fnKeyBg,
                textColor = fnKeyText,
                onClick = onPercent,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_percent")
            )
            val isDivideActive = activeOperator == "÷"
            CalcButton(
                text = "÷",
                backgroundColor = if (isDivideActive) (if (isDark) Color.White else opKeyBg.copy(alpha = 0.25f)) else opKeyBg,
                textColor = if (isDivideActive) opKeyBg else opKeyText,
                onClick = { onOperator("÷") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_divide")
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalcButton(
                text = "7",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("7") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_7")
            )
            CalcButton(
                text = "8",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("8") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_8")
            )
            CalcButton(
                text = "9",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("9") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_9")
            )
            val isMultiplyActive = activeOperator == "×"
            CalcButton(
                text = "×",
                backgroundColor = if (isMultiplyActive) (if (isDark) Color.White else opKeyBg.copy(alpha = 0.25f)) else opKeyBg,
                textColor = if (isMultiplyActive) opKeyBg else opKeyText,
                onClick = { onOperator("×") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_multiply")
            )
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalcButton(
                text = "4",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("4") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_4")
            )
            CalcButton(
                text = "5",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("5") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_5")
            )
            CalcButton(
                text = "6",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("6") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_6")
            )
            val isSubtractActive = activeOperator == "-"
            CalcButton(
                text = "-",
                backgroundColor = if (isSubtractActive) (if (isDark) Color.White else opKeyBg.copy(alpha = 0.25f)) else opKeyBg,
                textColor = if (isSubtractActive) opKeyBg else opKeyText,
                onClick = { onOperator("-") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_subtract")
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalcButton(
                text = "1",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("1") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_1")
            )
            CalcButton(
                text = "2",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("2") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_2")
            )
            CalcButton(
                text = "3",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("3") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_3")
            )
            val isAddActive = activeOperator == "+"
            CalcButton(
                text = "+",
                backgroundColor = if (isAddActive) (if (isDark) Color.White else opKeyBg.copy(alpha = 0.25f)) else opKeyBg,
                textColor = if (isAddActive) opKeyBg else opKeyText,
                onClick = { onOperator("+") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_add")
            )
        }

        // Row 5: 0, ., backspace, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CalcButton(
                text = "0",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = { onDigit("0") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_0")
            )
            CalcButton(
                text = ".",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = onDecimal,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_decimal")
            )
            CalcButton(
                text = "⌫",
                backgroundColor = numKeyBg,
                textColor = numKeyText,
                onClick = onBackspace,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_backspace")
            )
            CalcButton(
                text = "=",
                backgroundColor = equalsKeyBg,
                textColor = equalsKeyText,
                onClick = onEquals,
                modifier = Modifier
                    .weight(1f)
                    .testTag("btn_equals")
            )
        }
    }
}

@Composable
fun CalcButton(
    text: String? = null,
    icon: ImageVector? = null,
    iconDescription: String? = null,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth physical visual response to press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "button_press_scale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.1f) // Squircle vibe styling
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 4.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.15f)),
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = if (text.length > 2) 18.sp else 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = textColor
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun CalculatorPreview() {
    MyApplicationTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(SlateBg)) {
            CalculatorApp(modifier = Modifier.fillMaxSize())
        }
    }
}
