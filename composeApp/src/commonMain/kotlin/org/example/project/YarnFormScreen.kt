package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.*
import org.example.project.components.DateInput
import org.example.project.components.SelectAllOutlinedTextField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.max
import kotlin.math.round

// Helper function to normalize integer input fields
fun normalizeIntInput(input: String): String {
    val filtered = input.filter { it.isDigit() }
    return if (filtered.startsWith("0") && filtered.length > 1) {
        filtered.substring(1)
    } else {
        filtered
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnFormScreen(
    initial: Yarn?,
    initialImages: Map<Int, ByteArray>,
    usagesForYarn: List<Usage>,
    projectById: (Int) -> Project?,
    imageManager: ImageManager,
    settings: Settings,
    onBack: () -> Unit,
    onDelete: (Int) -> Unit,
    onSave: (Yarn, Map<Int, ByteArray>) -> Unit,
    onAddColor: (Yarn) -> Unit,
    onNavigateToProject: (Int) -> Unit
) {
    val totalUsedAmount = usagesForYarn.sumOf { it.amount }

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var color by remember { mutableStateOf(initial?.color ?: "") }
    var colorCode by remember { mutableStateOf(initial?.colorCode ?: "") }
    var brand by remember { mutableStateOf(initial?.brand ?: "") }
    var blend by remember { mutableStateOf(initial?.blend ?: "") }
    var dyeLot by remember { mutableStateOf(initial?.dyeLot ?: "") }
    var storagePlace by remember { mutableStateOf(initial?.storagePlace ?: "") }

    var weightPerSkeinText by remember { mutableStateOf(initial?.weightPerSkein?.toString() ?: "") }
    var meteragePerSkeinText by remember { mutableStateOf(initial?.meteragePerSkein?.toString() ?: "") }
    var amountText by remember(initial) { mutableStateOf(initial?.amount?.toString()?.takeIf { it != "0" } ?: "") }
    var numberOfBallsText by remember { mutableStateOf("1") }

    val modifiedState by remember { mutableStateOf(initial?.modified ?: getCurrentTimestamp()) }
    var added by remember { mutableStateOf(initial?.added ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val images = remember(initialImages) { mutableStateMapOf(*initialImages.toList().toTypedArray()) }
    var nextTempId by remember(initial?.id) { mutableStateOf((initial?.imageIds?.maxOrNull() ?: 0) + 1) }
    var selectedImageId by remember(initial?.id) { mutableStateOf(initial?.imageIds?.firstOrNull()) }


    val imagePicker = rememberImagePickerLauncher { newImageBytes ->
        newImageBytes.forEach { bytes ->
            val newId = nextTempId++
            images[newId] = bytes
            println("Image added with id: $newId")
        }
    }

    val cameraLauncher = rememberCameraLauncher { result ->
        result?.let {
            val newId = nextTempId++
            images[newId] = it
            println("Image added with id: $newId")
        }
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }
    var onConfirmUnsaved by remember { mutableStateOf<() -> Unit>({}) }

    val currentYarnState by remember(amountText, weightPerSkeinText, meteragePerSkeinText, totalUsedAmount) {
        derivedStateOf {
            Yarn(
                id = initial?.id ?: -1,
                name = "", // Not needed for calculations
                amount = amountText.toIntOrNull() ?: 0,
                weightPerSkein = weightPerSkeinText.toIntOrNull(),
                meteragePerSkein = meteragePerSkeinText.toIntOrNull(),
                usedAmount = totalUsedAmount
            )
        }
    }

    val hasChanges by remember(
        name,
        color,
        colorCode,
        brand,
        blend,
        dyeLot,
        storagePlace,
        weightPerSkeinText,
        meteragePerSkeinText,
        amountText,
        added,
        notes,
    ) {
        derivedStateOf {
            val imagesChanged = images.keys != initialImages.keys
            if (initial == null) {
                name.isNotEmpty() || color.isNotEmpty() || colorCode.isNotEmpty() || brand.isNotEmpty() ||
                        blend.isNotEmpty() ||
                        dyeLot.isNotEmpty() || storagePlace.isNotEmpty() || weightPerSkeinText.isNotEmpty() || meteragePerSkeinText.isNotEmpty() ||
                        amountText.isNotEmpty() ||
                        added.isNotEmpty() || notes.isNotEmpty() || images.isNotEmpty()
            } else {
                name != initial.name ||
                        color != (initial.color ?: "") ||
                        colorCode != (initial.colorCode ?: "") ||
                        brand != (initial.brand ?: "") ||
                        blend != (initial.blend ?: "") ||
                        dyeLot != (initial.dyeLot ?: "") ||
                        storagePlace != (initial.storagePlace ?: "") ||
                        weightPerSkeinText != (initial.weightPerSkein?.toString() ?: "") ||
                        meteragePerSkeinText != (initial.meteragePerSkein?.toString() ?: "") ||
                        amountText != (initial.amount.toString().takeIf { it != "0" } ?: "") ||
                        added != (initial.added ?: "") ||
                        notes != (initial.notes ?: "") ||
                        imagesChanged
            }
        }
    }

    val saveAction = {
        val enteredAmount = amountText.toIntOrNull() ?: 0
        val finalAmountToSave = max(enteredAmount, totalUsedAmount)

        val finalImageIds = images.keys.toList()
        val yarn = (initial ?: Yarn(id = -1, name = "", amount = 0, modified = getCurrentTimestamp()))
            .copy(
                name = name,
                brand = brand.ifBlank { null },
                color = color.ifBlank { null },
                colorCode = colorCode.ifBlank { null },
                blend = blend.ifBlank { null },
                dyeLot = dyeLot.ifBlank { null },
                storagePlace = storagePlace.ifBlank { null },
                amount = finalAmountToSave,
                weightPerSkein = weightPerSkeinText.toIntOrNull(),
                meteragePerSkein = meteragePerSkeinText.toIntOrNull(),
                modified = getCurrentTimestamp(),
                added = normalizeDateString(added),
                notes = notes.ifBlank { null },
                imageIds = finalImageIds,
                imagesChanged = initialImages.keys != images.keys
            )
        onSave(yarn, images.toMap())
    }

    val confirmDiscardChanges = { onConfirm: () -> Unit ->
        if (hasChanges) {
            showUnsavedDialog = true
            onConfirmUnsaved = onConfirm
        } else {
            onConfirm()
        }
    }

    val backAction = {
        confirmDiscardChanges(onBack)
    }

    BackButtonHandler {
        backAction()
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text(stringResource(Res.string.form_unsaved_changes_title)) },
            text = { Text(stringResource(Res.string.form_unsaved_changes_message)) },
            confirmButton = {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showUnsavedDialog = false }) {
                        Text(stringResource(Res.string.common_stay))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        showUnsavedDialog = false
                        onConfirmUnsaved()
                    }) {
                        Text(stringResource(Res.string.common_no))
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        saveAction()
                        showUnsavedDialog = false
                        onConfirmUnsaved()
                    }) {
                        Text(stringResource(Res.string.common_yes))
                    }
                }
            },
            dismissButton = null
        )
    }

    LaunchedEffect(initial) {
        if (initial != null) {
            val gpb = initial.weightPerSkein
            val amount = initial.amount
            if (gpb != null && gpb > 0 && amount > 0) {
                val balls = amount.toDouble() / gpb.toDouble()
                numberOfBallsText = if (balls == balls.toInt().toDouble()) {
                    balls.toInt().toString()
                } else {
                    (round(balls * 100) / 100.0).toString().trimEnd('0').trimEnd('.')
                }
            } else if (gpb != null && gpb > 0 && amount == 0) {
                numberOfBallsText = "0"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) stringResource(Res.string.yarn_form_new) else stringResource(Res.string.yarn_form_edit)) },
                navigationIcon = {
                    IconButton(onClick = backAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                val displayedImages = remember(images.size) {
                    images.entries.sortedBy { it.key }
                }

                LaunchedEffect(displayedImages) {
                    val keys = displayedImages.map { it.key }
                    if (selectedImageId == null && keys.isNotEmpty()) {
                        selectedImageId = keys.first()
                    } else if (selectedImageId != null && selectedImageId !in keys) {
                        selectedImageId = keys.firstOrNull()
                    }
                }

                val previewImage = selectedImageId?.let { images[it] }

                if (previewImage != null) {
                    val bitmap: ImageBitmap? = remember(previewImage) { previewImage.toImageBitmap() }
                    if (bitmap != null) {
                        Image(bitmap, contentDescription = "Yarn Preview Image", modifier = Modifier.fillMaxWidth().height(200.dp))
                    }
                } else {
                    Image(
                        painter = painterResource(Res.drawable.yarns),
                        contentDescription = "Yarn icon",
                        modifier = Modifier.fillMaxWidth().height(200.dp).alpha(0.5f)
                    )
                }
                Spacer(Modifier.height(8.dp))

                if (displayedImages.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedImages, key = { it.key }) { (id, bytes) ->
                            Box {
                                val bitmap = remember(bytes) { bytes.toImageBitmap() }
                                if (bitmap != null) {
                                    Image(
                                        bitmap,
                                        contentDescription = "Image $id",
                                        modifier = Modifier.size(80.dp)
                                            .clickable { selectedImageId = id }
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        images.remove(id)
                                        println("Image removed with id: $id")
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), CircleShape).size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove Image", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { imagePicker.launch() }) {
                        Text(stringResource(Res.string.project_form_select_image))
                    }
                    cameraLauncher?.let {
                        Button(onClick = { it.launch() }) {
                            Text(stringResource(Res.string.project_form_take_image))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                SelectAllOutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text(stringResource(Res.string.yarn_label_brand)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.yarn_label_name)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = color, onValueChange = { color = it }, label = { Text(stringResource(Res.string.yarn_label_color)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectAllOutlinedTextField(
                        value = colorCode,
                        onValueChange = { colorCode = it },
                        label = { Text(stringResource(Res.string.yarn_label_color_code)) },
                        modifier = Modifier.weight(1f)
                    )
                    SelectAllOutlinedTextField(
                        value = dyeLot,
                        onValueChange = { dyeLot = it },
                        label = { Text(stringResource(Res.string.yarn_label_dye_lot)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = blend, onValueChange = { blend = it }, label = { Text(stringResource(Res.string.yarn_label_blend)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectAllOutlinedTextField(
                        value = weightPerSkeinText,
                        onValueChange = { newValue ->
                            val normalized = normalizeIntInput(newValue)
                            weightPerSkeinText = normalized
                            val gpb = normalized.toIntOrNull()
                            val balls = numberOfBallsText.replace(",", ".").toDoubleOrNull()
                            if (gpb != null && balls != null && gpb > 0 && balls > 0) {
                                amountText = round(gpb * balls).toInt().toString()
                            }
                        },
                        label = { Text(stringResource(Res.string.yarn_label_weight_per_skein)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    SelectAllOutlinedTextField(
                        value = meteragePerSkeinText,
                        onValueChange = { meteragePerSkeinText = normalizeIntInput(it) },
                        label = { Text(if (settings.lengthUnit == LengthUnit.METER) stringResource(Res.string.yarn_label_meterage_per_skein) else stringResource(Res.string.yarn_label_yardage_per_skein)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    SelectAllOutlinedTextField(
                        value = numberOfBallsText,
                        onValueChange = { newValue ->
                            val normalized = newValue.replace(",", ".")
                            if (normalized.isEmpty() || normalized.toDoubleOrNull() != null || (normalized.endsWith(".") && normalized.count { it == '.' } == 1)) {
                                numberOfBallsText = normalized
                                val balls = normalized.toDoubleOrNull()
                                val gpb = weightPerSkeinText.toIntOrNull()
                                if (gpb != null && balls != null && gpb > 0) { // Allow balls > 0
                                    amountText = round(gpb * balls).toInt().toString()
                                }
                            }
                        },
                        label = { Text(stringResource(Res.string.yarn_label_number_of_skeins)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    SelectAllOutlinedTextField(
                        value = amountText,
                        onValueChange = { newValue ->
                            val normalized = normalizeIntInput(newValue)
                            amountText = normalized
                            val amount = normalized.toIntOrNull()
                            val gpb = weightPerSkeinText.toIntOrNull()
                            if (gpb != null && amount != null && gpb > 0) { // Allow amount >= 0
                                val balls = amount.toDouble() / gpb.toDouble()
                                numberOfBallsText = if (balls == balls.toInt().toDouble()) {
                                    balls.toInt().toString()
                                } else {
                                    (round(balls * 100) / 100.0).toString().trimEnd('0').trimEnd('.')
                                }
                            } else if (amount == 0) {
                                numberOfBallsText = "0"
                            }
                        },
                        label = { Text(stringResource(Res.string.yarn_label_amount)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if ((currentYarnState.availableMeterage ?: 0) + (currentYarnState.usedMeterage ?: 0) > 0) {
                            Text(
                                if (settings.lengthUnit == LengthUnit.METER)
                                    stringResource(Res.string.usage_used_with_meterage, currentYarnState.usedAmount, currentYarnState.usedMeterage ?: 0)
                                else
                                    stringResource(Res.string.usage_used_with_yardage, currentYarnState.usedAmount, currentYarnState.usedMeterage ?: 0)
                            )
                            Text(
                                if (settings.lengthUnit == LengthUnit.METER)
                                    stringResource(Res.string.usage_available_with_meterage, currentYarnState.availableAmount, currentYarnState.availableMeterage ?: 0)
                                else
                                    stringResource(Res.string.usage_available_with_yardage, currentYarnState.availableAmount, currentYarnState.availableMeterage ?: 0)
                            )
                        } else {
                            Text(
                                stringResource(
                                    Res.string.usage_used,
                                    currentYarnState.usedAmount
                                )
                            )
                            Text(
                                stringResource(
                                    Res.string.usage_available,
                                    currentYarnState.availableAmount
                                )
                            )
                        }
                        if (initial != null && usagesForYarn.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = {
                                amountText = totalUsedAmount.toString()
                            }) {
                                Text(stringResource(Res.string.yarn_form_button_set_remaining_to_zero))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                DateInput(
                    label = stringResource(Res.string.yarn_label_added),
                    date = added,
                    onDateChange = { added = it ?: "" }
                )
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = storagePlace, onValueChange = { storagePlace = it }, label = { Text(stringResource(Res.string.yarn_label_storage_place)) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                SelectAllOutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(Res.string.yarn_label_notes)) }, singleLine = false, minLines = 3, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text(stringResource(Res.string.yarn_item_label_modified, modifiedState))

                if (initial != null) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(stringResource(Res.string.usage_projects_title), style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        if (usagesForYarn.isEmpty()) {
                            Text(stringResource(Res.string.yarn_form_no_projects_assigned), modifier = Modifier.padding(start = 8.dp))
                        } else {
                            usagesForYarn.forEach { usage ->
                                val project = projectById(usage.projectId)
                                if (project != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                                        LaunchedEffect(project.id, project.imageIds) {
                                            val imageId = project.imageIds.firstOrNull()
                                            imageBytes = if (imageId != null) {
                                                imageManager.getProjectImageThumbnail(project.id, imageId)
                                            } else {
                                                null
                                            }
                                        }
                                        val bitmap = remember(imageBytes) { imageBytes?.toImageBitmap() }

                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Project image",
                                                modifier = Modifier.size(40.dp)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(Res.drawable.projects),
                                                contentDescription = "Default project image",
                                                modifier = Modifier.size(40.dp).alpha(0.5f)
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        var usageText = "${project.name}: ${usage.amount} g"
                                        initial.weightPerSkein?.let { weightPerSkein ->
                                            initial.meteragePerSkein?.let { meteragePerSkein ->
                                                if (weightPerSkein > 0) {
                                                    val usedMeterage = (usage.amount.toDouble() / weightPerSkein * meteragePerSkein).toInt()
                                                    usageText += " ($usedMeterage ${if (settings.lengthUnit == LengthUnit.METER) "m" else "yd"})"
                                                }
                                            }
                                        }
                                        Text(usageText, modifier = Modifier.weight(1f))
                                        Button(onClick = { confirmDiscardChanges { onNavigateToProject(project.id) } }) {
                                            Text(stringResource(Res.string.yarn_form_view_project))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = backAction) { Text(stringResource(Res.string.common_cancel)) }
                    Row {
                        if (initial != null) {
                            TextButton(onClick = {
                                confirmDiscardChanges { onAddColor(initial) }
                            }) { Text(stringResource(Res.string.yarn_form_add_color)) }
                            if (usagesForYarn.isEmpty()) {
                                Spacer(Modifier.width(8.dp))
                                TextButton(onClick = { onDelete(initial.id) }) { Text(stringResource(Res.string.common_delete)) }
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Button(onClick = saveAction) { Text(stringResource(Res.string.common_save)) }
                    }
                }
            }
        }
    }
}
