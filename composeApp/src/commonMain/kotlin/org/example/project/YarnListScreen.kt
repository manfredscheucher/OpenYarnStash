package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.text.append

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    yarnImages: Map<Int, ByteArray?>,
    usages: List<Usage>,
    settings: Settings,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit,
    onSettingsChange: (Settings) -> Unit
) {
    AppBackHandler {
        onBack()
    }

    var hideUsed by remember { mutableStateOf(settings.hideUsedYarns) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.yarn_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(onClick = onAddClick) {
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
                val usagesByYarnId = remember(usages) {
                    usages.groupBy { it.yarnId }
                        .mapValues { entry -> entry.value.sumOf { it.amount } }
                }

                val yarnData = remember(yarns, usagesByYarnId) {
                    yarns.map { yarn ->
                        val used = usagesByYarnId[yarn.id] ?: 0
                        val available = (yarn.amount - used).coerceAtLeast(0)
                        val meterage = yarn.meteragePerSkein
                        val weight = yarn.weightPerSkein
                        val availableMeterage = if (meterage != null && weight != null && weight > 0) {
                            (available * meterage) / weight
                        } else {
                            null
                        }
                        object {
                            val yarnItem = yarn
                            val usedAmount = used
                            val availableAmount = available
                            val availableMeterageAmount = availableMeterage
                        }
                    }
                }

                val filteredYarnData = if (hideUsed) {
                    yarnData.filter { it.availableAmount > 0 }
                } else {
                    yarnData
                }

                val totalAvailable = filteredYarnData.sumOf { it.availableAmount }
                val totalMeterage = filteredYarnData.sumOf { it.availableMeterageAmount ?: 0 }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = hideUsed,
                        onClick = {
                            hideUsed = !hideUsed
                            onSettingsChange(settings.copy(hideUsedYarns = hideUsed))
                        },
                        label = { Text(stringResource(Res.string.yarn_list_hide_used)) }
                    )
                }

                Text(
                    text = stringResource(Res.string.yarn_list_summary, totalAvailable),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
                Text(
                    text = stringResource(Res.string.yarn_list_summary_meterage, totalMeterage),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    items(filteredYarnData) { data ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onOpen(data.yarnItem.id) },
                            colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(data.yarnItem.id))
                        ) {

                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                val imageBytes = yarnImages[data.yarnItem.id]
                                if (imageBytes != null) {
                                    val bitmap = remember(imageBytes) { imageBytes.toImageBitmap() }
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Yarn image for ${data.yarnItem.name}",
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
                                        // Append brand with normal weight if it exists and is not blank
                                        data.yarnItem.brand?.takeIf { it.isNotBlank() }?.let {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontStyle = FontStyle.Italic,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            ) {
                                                append("$it ")
                                            }
                                        }
                                        // Append yarn name with bold weight
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append(data.yarnItem.name)
                                        }
                                        // Append color with bold weight if it exists and is not blank
                                        data.yarnItem.color?.takeIf { it.isNotBlank() }?.let {
                                            append(" ($it)")
                                        }
                                    })
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            stringResource(
                                                Res.string.usage_used,
                                                data.usedAmount
                                            )
                                        )
                                        Text(
                                            stringResource(
                                                Res.string.usage_available,
                                                data.availableAmount
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
