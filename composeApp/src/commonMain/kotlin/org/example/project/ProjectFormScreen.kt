package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import openyarnstash.composeapp.generated.resources.*
import org.example.project.components.DateInput
import org.example.project.components.SelectAllOutlinedTextField
import org.example.project.pdf.getProjectPdfExporter
import org.example.project.pdf.rememberPdfSaver
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.example.project.pdf.Params as PdfParams
import org.example.project.pdf.Project as PdfProject
import org.example.project.pdf.Yarn as PdfYarn
import org.example.project.pdf.YarnUsage as PdfYarnUsage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectFormScreen(
    initial: Project,
    initialImages: Map<UInt, ByteArray>,
    assignmentsForProject: List<Assignment>,
    yarnById: (UInt) -> Yarn?,
    patterns: List<Pattern>,
    imageManager: ImageManager,
    onBack: () -> Unit,
    onDelete: (UInt) -> Unit,
    onSave: (Project, Map<UInt, ByteArray>, (() -> Unit)?) -> Unit,
    onNavigateToAssignments: () -> Unit,
    onNavigateToPattern: (UInt) -> Unit,
    onNavigateToYarn: (UInt) -> Unit
) {
    val isNewProject = initial.id == 0u

    var name by remember { mutableStateOf(initial.name) }
    var forWho by remember { mutableStateOf(initial.madeFor ?: "") }
    var startDate by remember { mutableStateOf(initial.startDate ?: "") }
    var endDate by remember { mutableStateOf(initial.endDate ?: "") }
    var notes by remember { mutableStateOf(initial.notes ?: "") }
    var needleSize by remember { mutableStateOf(initial.needleSize ?: "") }
    var size by remember { mutableStateOf(initial.size ?: "") }
    var gauge by remember { mutableStateOf(initial.gauge ?: "") }
    var rowCounters by remember { mutableStateOf(initial.rowCounters) }
    var patternId by remember { mutableStateOf(initial.patternId) }
    val modifiedState by remember { mutableStateOf(initial.modified ?: getCurrentTimestamp()) }
    var showDeleteRestrictionDialog by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }
    var onConfirmUnsaved by remember { mutableStateOf<() -> Unit>({}) }
    var showAddCounterDialog by remember { mutableStateOf(false) }
    var patternDropdownExpanded by remember { mutableStateOf(false) }

    val images = remember { mutableStateMapOf<UInt, ByteArray>() }
    LaunchedEffect(initialImages) {
        images.clear()
        images.putAll(initialImages)
    }
    var nextTempId by remember(initial.id) { mutableStateOf<UInt>((initial.imageIds.maxOrNull() ?: 0u) + 1u) }
    var selectedImageId by remember(initial.id) { mutableStateOf<UInt?>(initial.imageIds.firstOrNull()) }


    val scope = rememberCoroutineScope()

    val imagePicker = rememberImagePickerLauncher { newImageBytes ->
        newImageBytes.forEach { bytes ->
            val newId = nextTempId++
            images[newId] = bytes
            scope.launch {
                Logger.log(LogLevel.DEBUG, "Image added with id: $newId")
            }
        }
    }

    val cameraLauncher = rememberCameraLauncher { result ->
        result?.let {
            val newId = nextTempId++
            images[newId] = it
            scope.launch {
                Logger.log(LogLevel.DEBUG, "Image added with id: $newId")
            }
        }
    }
    val pdfExporter = remember { getProjectPdfExporter() }
    val pdfSaver = rememberPdfSaver()

    val changes by remember(
        initial,
        name,
        forWho,
        startDate,
        endDate,
        notes,
        needleSize,
        size,
        gauge,
        rowCounters,
        patternId,
        images,
        initialImages
    ) {
        derivedStateOf {
            val changedFields = mutableListOf<String>()
            if (name != initial.name) changedFields.add("name")
            if (forWho != (initial.madeFor ?: "")) changedFields.add("forWho")
            if (startDate != (initial.startDate ?: "")) changedFields.add("startDate")
            if (endDate != (initial.endDate ?: "")) changedFields.add("endDate")
            if (notes != (initial.notes ?: "")) changedFields.add("notes")
            if (needleSize != (initial.needleSize ?: "")) changedFields.add("needleSize")
            if (size != (initial.size ?: "")) changedFields.add("size")
            if (gauge != (initial.gauge ?: "")) changedFields.add("gauge")
            if (rowCounters != initial.rowCounters) changedFields.add("rowCounters")
            if (patternId != initial.patternId) changedFields.add("patternId")
            if (images.toMap().keys != initialImages.keys) changedFields.add("images")
            changedFields
        }
    }
    val hasChanges by derivedStateOf { changes.isNotEmpty() }

    val exportPdf: () -> Unit = { // Using current state values
        scope.launch {
            val projectData = PdfProject(
                id = initial.id,
                imageIds = initial.imageIds,
                title = name
            )
            val paramsData = PdfParams(
                gauge = gauge.ifBlank { null },
                needles = needleSize.ifBlank { null },
                size = size.ifBlank { null },
                yarnWeight = null, // Not available in your model
                notes = notes.ifBlank { null }
            )
            val yarnUsagesData = assignmentsForProject.mapNotNull { assignment ->
                yarnById(assignment.yarnId)?.let { yarn ->
                    val metersUsed = if (yarn.weightPerSkein != null && yarn.weightPerSkein > 0 && yarn.meteragePerSkein != null) {
                        (assignment.amount.toDouble() / yarn.weightPerSkein) * yarn.meteragePerSkein
                    } else {
                        null
                    }
                    PdfYarnUsage(
                        yarn = PdfYarn(
                            id = yarn.id,
                            brand = yarn.brand,
                            name = yarn.name,
                            colorway = yarn.color,
                            lot = yarn.dyeLot,
                            material = yarn.blend,
                            weightClass = null, // Not available
                            imageIds = yarn.imageIds
                        ),
                        gramsUsed = assignment.amount.toDouble(),
                        metersUsed = metersUsed
                    )
                }
            }

            val pdfBytes = pdfExporter.exportToPdf(projectData, paramsData, yarnUsagesData, imageManager)
            pdfSaver("${name.replace(" ", "_")}.pdf", pdfBytes)
        }
    }

    val saveAction: ((() -> Unit)?) -> Unit = { callback ->
        val finalImageIds = images.keys.toList()
        val project = initial.copy(
            name = name,
            madeFor = forWho.ifBlank { null },
            startDate = startDate.ifBlank { null },
            endDate = endDate.ifBlank { null },
            notes = notes.ifBlank { null },
            modified = getCurrentTimestamp(),
            needleSize = needleSize.ifBlank { null },
            size = size.ifBlank { null },
            gauge = gauge.ifBlank { null },
            rowCounters = rowCounters,
            patternId = patternId,
            imageIds = finalImageIds,
            imagesChanged = initialImages.keys != images.keys // TODO: not used anywhere
        )
        onSave(project, images.toMap(), callback)
    }

    val saveAndGoBack = {
        saveAction { onBack() }
    }

    fun confirmDiscardChanges(action: () -> Unit) {
        if (hasChanges) {
            scope.launch {
                Logger.log(LogLevel.DEBUG, "ProjectFormScreen has changes: ${changes.joinToString(", ")}")
            }
            onConfirmUnsaved = action
            showUnsavedDialog = true
        } else {
            action()
        }
    }

    val backAction = {
        confirmDiscardChanges { onBack() }
    }

    val assignAction = {
        confirmDiscardChanges { onNavigateToAssignments() }
    }

    BackButtonHandler { backAction() }

    var showDeleteDialog by remember { mutableStateOf(false) }

    UnsavedChangesDialog(
        showDialog = showUnsavedDialog,
        onDismiss = { showUnsavedDialog = false },
        onSaveAndProceed = {
            showUnsavedDialog = false
            saveAction { onConfirmUnsaved?.invoke() }
        },
        onDiscardAndProceed = {
            showUnsavedDialog = false
            onConfirmUnsaved?.invoke()
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(Res.string.project_form_delete_title)) },
            text = { Text(stringResource(Res.string.project_form_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete(initial.id)
                }) {
                    Text(stringResource(Res.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.common_cancel))
                }
            }
        )
    }


    if (showDeleteRestrictionDialog) {
        DeleteRestrictionDialog(
            onDismiss = { showDeleteRestrictionDialog = false }
        )
    }

    if (showAddCounterDialog) {
        AddCounterDialog(
            initialName = stringResource(Res.string.row_counter_new_default_name, rowCounters.size + 1),
            onDismiss = { showAddCounterDialog = false },
            onAdd = {
                rowCounters = rowCounters + RowCounter(it, 0)
                showAddCounterDialog = false
            }
        )
    }

    val status = when {
        endDate.isBlank() -> ProjectStatus.FINISHED
        startDate.isBlank() -> ProjectStatus.IN_PROGRESS
        else -> ProjectStatus.PLANNING
    }

    Scaffold(
        topBar = {
            val titleRes = if (isNewProject) Res.string.project_form_new else Res.string.project_form_edit
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = backAction) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.common_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { saveAndGoBack() },
                        enabled = name.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.common_save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                    Image(bitmap, contentDescription = "Project Preview Image", modifier = Modifier.fillMaxWidth().height(200.dp))
                }
            } else {
                Image(
                    painter = painterResource(Res.drawable.projects),
                    contentDescription = "Project icon",
                    modifier = Modifier.fillMaxWidth().height(200.dp).alpha(0.5f)
                )
            }
            Spacer(Modifier.height(8.dp))

            if (displayedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedImages, key = { it.key.toLong() }) { (id, bytes) ->
                        Box {
                            val bitmap = remember(bytes) { bytes.toImageBitmap() }
                            Image(
                                bitmap,
                                contentDescription = "Image $id",
                                modifier = Modifier.size(80.dp)
                                    .clickable { selectedImageId = id }
                            )
                            IconButton(
                                onClick = {
                                    images.remove(id)
                                    scope.launch {
                                        Logger.log(LogLevel.DEBUG, "Image removed with id: $id")
                                    }
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
            SelectAllOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(Res.string.project_label_name)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Box {
                val selectedPattern = patterns.firstOrNull { it.id == patternId }
                OutlinedTextField(
                    value = selectedPattern?.name ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(Res.string.project_label_pattern)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { patternDropdownExpanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, "Select Pattern")
                        }
                    }
                )
                DropdownMenu(
                    expanded = patternDropdownExpanded,
                    onDismissRequest = { patternDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("No Pattern") },
                        onClick = {
                            patternId = null
                            patternDropdownExpanded = false
                        }
                    )
                    patterns.forEach { pattern ->
                        DropdownMenuItem(
                            text = { Text(pattern.name) },
                            onClick = {
                                patternId = pattern.id
                                patternDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (patternId != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = { confirmDiscardChanges { onNavigateToPattern(patternId!!) } }) {
                    Text(stringResource(Res.string.project_form_view_pattern))
                }
            }
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(value = forWho, onValueChange = { forWho = it }, label = { Text(stringResource(Res.string.project_label_for)) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            DateInput(
                label = stringResource(Res.string.project_label_start_date),
                date = startDate,
                onDateChange = { startDate = it ?: "" }
            )
            Spacer(Modifier.height(8.dp))
            DateInput(
                label = stringResource(Res.string.project_label_end_date),
                date = endDate,
                onDateChange = { endDate = it ?: "" }
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectAllOutlinedTextField(value = needleSize, onValueChange = { needleSize = it }, label = { Text(stringResource(Res.string.project_label_needle_size)) }, modifier = Modifier.weight(1f))
                SelectAllOutlinedTextField(value = size, onValueChange = { size = it }, label = { Text(stringResource(Res.string.project_label_size)) }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = gauge,
                onValueChange = { gauge = it },
                label = { Text(stringResource(Res.string.project_label_gauge)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                val rowHeight = 50
                val totalHeight = if (rowCounters.isNotEmpty()) {
                    (rowCounters.size * rowHeight).dp + 60.dp
                } else {
                    64.dp
                }
                OutlinedTextField(
                    value = " ",
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(totalHeight),
                    readOnly = true,
                    label = { Text(stringResource(Res.string.project_label_row_count)) }
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 8.dp)
                ) {
                    rowCounters.forEachIndexed { index, counter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().padding(end = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(counter.name, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.weight(0.6f))
                            IconButton(onClick = {
                                rowCounters = rowCounters.toMutableList().also {
                                    it[index] = it[index].copy(value = it[index].value - 1)
                                }
                            }) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease row count")
                            }
                            Text(text = counter.value.toString(), style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = {
                                rowCounters = rowCounters.toMutableList().also {
                                    it[index] = it[index].copy(value = it[index].value + 1)
                                }
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Increase row count")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { showAddCounterDialog = true }) {
                            Text(stringResource(Res.string.project_form_add_counter))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val statusText = when (status) {
                ProjectStatus.PLANNING -> stringResource(Res.string.project_status_planning)
                ProjectStatus.IN_PROGRESS -> stringResource(Res.string.project_status_in_progress)
                ProjectStatus.FINISHED -> stringResource(Res.string.project_status_finished)
            }
            Text("Status: $statusText", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.yarn_item_label_modified, formatTimestamp(modifiedState)))
            Spacer(Modifier.height(8.dp))
            SelectAllOutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(Res.string.project_label_notes)) },
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isNewProject) {
                Spacer(Modifier.height(16.dp))
                val yarnHeight = 50
                Box(modifier = Modifier.fillMaxWidth()) {
                    val totalHeight = if (assignmentsForProject.isNotEmpty()) {
                        (assignmentsForProject.size * yarnHeight).dp + 75.dp
                    } else {
                        64.dp
                    }
                    OutlinedTextField(
                        value = " ",
                        onValueChange = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(totalHeight),
                        readOnly = true,
                        label = { Text(stringResource(Res.string.usage_section_title)) }
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 16.dp)
                    ) {
                        if (assignmentsForProject.isEmpty()) {
                            Text(stringResource(Res.string.project_form_no_yarn_assigned))
                        } else {
                            assignmentsForProject.forEach { assignment ->
                                val yarn = yarnById(assignment.yarnId)
                                if (yarn != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                    ) {
                                        var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                                        LaunchedEffect(yarn.id, yarn.imageIds) {
                                            val imageId = yarn.imageIds.firstOrNull()
                                            imageBytes = if (imageId != null) {
                                                imageManager.getYarnImageThumbnail(yarn.id, imageId)
                                            } else {
                                                null
                                            }
                                        }
                                        val bitmap = remember(imageBytes) { imageBytes?.toImageBitmap() }

                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap,
                                                contentDescription = "Yarn image",
                                                modifier = Modifier.size(40.dp)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(Res.drawable.yarns),
                                                contentDescription = "Default yarn image",
                                                modifier = Modifier.size(40.dp).alpha(0.5f)
                                            )
                                        }

                                        Spacer(Modifier.width(8.dp))

                                        Column(Modifier.weight(1f)) {
                                            Text(buildAnnotatedString {
                                                yarn.brand?.takeIf { it.isNotBlank() }?.let {
                                                    withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold)) {
                                                        append("$it ")
                                                    }
                                                }
                                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                    append(yarn.name)
                                                }
                                                yarn.color?.takeIf { it.isNotBlank() }?.let {
                                                    append(" ($it)")
                                                }
                                            })
                                            Text("${assignment.amount} g")
                                        }
                                        Spacer(Modifier.width(8.dp))

                                        Button(onClick = { confirmDiscardChanges { onNavigateToYarn(yarn.id) } }) {
                                            Text(stringResource(Res.string.project_form_view_yarn))
                                        }
                                    }
                                } else {
                                    Text("- ERROR: yarnid ${assignment.yarnId} does not exist!")
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = assignAction
                            ) {
                                Text(stringResource(Res.string.project_form_button_assignments))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            if (!isNewProject) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        if (assignmentsForProject.isNotEmpty()) {
                            showDeleteRestrictionDialog = true
                        } else {
                            showDeleteDialog = true
                        }
                    }) {
                        Text(stringResource(Res.string.common_delete))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCounterDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.project_form_add_counter_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.project_form_add_counter_dialog_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                val finalName = name.ifBlank { initialName }
                onAdd(finalName)
            }) {
                Text(stringResource(Res.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel))
            }
        }
    )
}

@Composable
private fun DeleteRestrictionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.delete_project_restricted_title)) },
        text = { Text(stringResource(Res.string.delete_project_restricted_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_ok)) }
        }
    )
}

expect fun ByteArray.toImageBitmap(): ImageBitmap
