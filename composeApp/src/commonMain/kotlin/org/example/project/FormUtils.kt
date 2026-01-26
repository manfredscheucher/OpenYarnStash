package org.example.project

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_no
import openyarnstash.composeapp.generated.resources.common_save
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_message
import openyarnstash.composeapp.generated.resources.form_unsaved_changes_title
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable dialog for handling unsaved changes in form screens.
 * When the user confirms saving, it calls saveAction and then onConfirm.
 * When the user chooses not to save, it just calls onConfirm.
 */
@Composable
fun UnsavedChangesDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSaveAndProceed: () -> Unit,
    onDiscardAndProceed: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(Res.string.form_unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.form_unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = onSaveAndProceed) {
                    Text(stringResource(Res.string.common_save))
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscardAndProceed) {
                    Text(stringResource(Res.string.common_no))
                }
            }
        )
    }
}
