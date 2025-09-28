package org.example.project

import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.common_button_cancel
import knittingappmultiplatt.composeapp.generated.resources.common_button_delete
import knittingappmultiplatt.composeapp.generated.resources.common_button_save
import knittingappmultiplatt.composeapp.generated.resources.form_label_amount_required
import knittingappmultiplatt.composeapp.generated.resources.form_label_color
import knittingappmultiplatt.composeapp.generated.resources.form_label_date_optional
import knittingappmultiplatt.composeapp.generated.resources.form_label_name_required
import knittingappmultiplatt.composeapp.generated.resources.form_label_url_optional
import knittingappmultiplatt.composeapp.generated.resources.yarn_form_error_amount_invalid
import knittingappmultiplatt.composeapp.generated.resources.yarn_form_error_name_empty
import knittingappmultiplatt.composeapp.generated.resources.yarn_form_title_edit
import knittingappmultiplatt.composeapp.generated.resources.yarn_form_title_new

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    initial: Yarn?,                  // null = new
    onCancel: () -> Unit,
    onDelete: (Int) -> Unit,         // returns id
    onSave: (Yarn) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var amountText by remember { mutableStateOf((initial?.amount ?: 0).toString()) }
    var url by remember { mutableStateOf(initial?.url ?: "") }
    var date by remember { mutableStateOf(initial?.date ?: "") }

    var error by remember { mutableStateOf<String?>(null) }

    val title = if (initial == null) stringResource(Res.string.yarn_form_title_new) else stringResource(Res.string.yarn_form_title_edit)
    val errorNameEmpty = stringResource(Res.string.yarn_form_error_name_empty)
    val errorAmountInvalid = stringResource(Res.string.yarn_form_error_amount_invalid)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.form_label_name_required)) }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text(stringResource(Res.string.form_label_color)) }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text(stringResource(Res.string.form_label_amount_required)) }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text(stringResource(Res.string.form_label_url_optional)) }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text(stringResource(Res.string.form_label_date_optional)) }, singleLine = true)

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    // Validation
                    val amt = amountText.toIntOrNull()
                    if (name.isBlank()) { error = errorNameEmpty; return@Button }
                    if (amt == null || amt < 0) { error = errorAmountInvalid; return@Button }

                    val y = Yarn(
                        id = initial?.id ?: -1, // -1 = will be assigned in App
                        name = name.trim(),
                        color = color.ifBlank { null },
                        amount = amt,
                        url = url.ifBlank { null },
                        date = date.ifBlank { null }
                    )
                    onSave(y)
                }) {
                    Text(stringResource(Res.string.common_button_save))
                }
                OutlinedButton(onClick = onCancel) { Text(stringResource(Res.string.common_button_cancel)) }
                if (initial != null) {
                    OutlinedButton(
                        onClick = { onDelete(initial.id) },
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text(stringResource(Res.string.common_button_delete)) }
                }
            }
        }
    }
}
