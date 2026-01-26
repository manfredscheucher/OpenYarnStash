package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.Json
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    imageManager: ImageManager,
    assignments: List<Assignment>,
    settings: Settings,
    onAddClick: () -> Unit,
    onOpen: (UInt) -> Unit,
    onBack: () -> Unit,
    onSettingsChange: (Settings) -> Unit
) {
    BackButtonHandler {
        onBack()
    }

    var showConsumed by remember { mutableStateOf(settings.hideUsedYarns) }
    var filter by remember { mutableStateOf("") }

    val assignmentsByYarnId = remember(assignments) {
        assignments.groupBy { it.yarnId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val yarnData = remember(yarns, assignmentsByYarnId) {
        yarns.map { it.copy(usedAmount = assignmentsByYarnId[it.id] ?: 0) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.yarns),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.yarn_list_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(Res.string.common_plus_symbol),
                    style = MaterialTheme.typography.displayMedium
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (yarns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(stringResource(Res.string.yarn_list_empty))
                }
            } else {
                val filteredYarnData = yarnData.filter {
                    val consumedOk = if (showConsumed) true else it.availableAmount > 0
                    val filterOk = if (filter.isNotBlank()) {
                        Json.encodeToString(it).contains(filter, ignoreCase = true)
                    } else {
                        true
                    }
                    consumedOk && filterOk
                }.sortedByDescending { it.modified }

                val totalAvailable = yarnData.sumOf { it.availableAmount }
                val totalMeterage = yarnData.sumOf { it.availableMeterage ?: 0 }

                Text(
                    text = if (settings.lengthUnit == LengthUnit.METER)
                        stringResource(Res.string.yarn_list_summary, totalAvailable, totalMeterage)
                    else
                        stringResource(Res.string.yarn_list_summary_yards, totalAvailable, totalMeterage),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showConsumed,
                        onClick = {
                            val newShowConsumed = !showConsumed
                            showConsumed = newShowConsumed
                            onSettingsChange(settings.copy(hideUsedYarns = newShowConsumed))
                        },
                        label = { Text(stringResource(Res.string.yarn_list_hide_used)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    )
                    OutlinedTextField(
                        value = filter,
                        onValueChange = { filter = it },
                        label = { Text(stringResource(Res.string.yarn_list_filter)) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = stringResource(Res.string.yarn_list_filter)
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    items(filteredYarnData) { yarn ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onOpen(yarn.id) },
                            colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(yarn.id))
                        ) {

                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
                                        contentDescription = "Yarn image for ${yarn.name}",
                                        modifier = Modifier.size(64.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(Res.drawable.yarns),
                                        contentDescription = "Yarn icon",
                                        modifier = Modifier.size(64.dp).alpha(0.5f),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {

                                    Text(buildAnnotatedString {
                                        yarn.brand?.takeIf { it.isNotBlank() }?.let {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            ) {
                                                append("$it ")
                                            }
                                        }
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(yarn.name)
                                        }
                                        yarn.color?.takeIf { it.isNotBlank() }?.let {
                                            append(" (${it})")
                                        }
                                    })
                                    Spacer(Modifier.height(8.dp))
                                    if ((yarn.availableMeterage ?: 0) + (yarn.usedMeterage ?: 0) > 0) {
                                        Text(
                                            if (settings.lengthUnit == LengthUnit.METER)
                                                stringResource(Res.string.usage_used_with_meterage, yarn.usedAmount, yarn.usedMeterage ?: 0)
                                            else
                                                stringResource(Res.string.usage_used_with_yardage, yarn.usedAmount, yarn.usedMeterage ?: 0)
                                        )
                                        Text(
                                            if (settings.lengthUnit == LengthUnit.METER)
                                                stringResource(Res.string.usage_available_with_meterage, yarn.availableAmount, yarn.availableMeterage ?: 0)
                                            else
                                                stringResource(Res.string.usage_available_with_yardage, yarn.availableAmount, yarn.availableMeterage ?: 0)
                                        )
                                    } else {
                                        Text(
                                            stringResource(
                                                Res.string.usage_used,
                                                yarn.usedAmount
                                            )
                                        )
                                        Text(
                                            stringResource(
                                                Res.string.usage_available,
                                                yarn.availableAmount
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
