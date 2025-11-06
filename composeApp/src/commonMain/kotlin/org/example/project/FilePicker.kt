package org.example.project

import androidx.compose.runtime.Composable

@Composable
expect fun FilePicker(show: Boolean, onFileSelected: (String?) -> Unit)

@Composable
expect fun FilePickerForZip(show: Boolean, onFileSelected: (ByteArray?) -> Unit)
