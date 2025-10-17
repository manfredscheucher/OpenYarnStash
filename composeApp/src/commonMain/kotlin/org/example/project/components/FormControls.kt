package org.example.project.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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

    // Update internal state when value from parent changes
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = textFieldValue.copy(text = value)
        }
    }

    var focused by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = {
            textFieldValue = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        modifier = modifier.onFocusChanged {
            if (focused != it.isFocused) {
                focused = it.isFocused
                if (focused) {
                    // Select all text on focus gain
                    textFieldValue = textFieldValue.copy(selection = TextRange(0, textFieldValue.text.length))
                }
            }
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
