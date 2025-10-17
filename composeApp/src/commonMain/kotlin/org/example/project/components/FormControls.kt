package org.example.project.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.withFrameNanos // <-- wichtig

@Composable
fun SelectAllOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text = value)) }

    // Parent-Value nachziehen (Selektion beibehalten)
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    var hadFocus by remember { mutableStateOf(false) }
    var selectAllRequested by remember { mutableStateOf(false) }

    // Auswahl NACH dem Tap/Focus setzen (eine Frame später)
    LaunchedEffect(selectAllRequested) {
        if (selectAllRequested) {
            withFrameNanos { /* wartet genau 1 Frame */ }
            textFieldValue = textFieldValue.copy(
                selection = TextRange(0, textFieldValue.text.length)
            )
            selectAllRequested = false
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            if (value != it.text) onValueChange(it.text)
        },
        modifier = modifier.onFocusChanged { state ->
            // Nur beim tatsächlichen Fokuserhalt einmalig select-all auslösen
            if (state.isFocused && !hadFocus) {
                selectAllRequested = true
            }
            hadFocus = state.isFocused
        },
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        minLines = minLines,
    )
}