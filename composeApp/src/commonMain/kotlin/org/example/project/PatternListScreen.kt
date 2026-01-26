package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.common_plus_symbol
import openyarnstash.composeapp.generated.resources.pattern_list_empty
import openyarnstash.composeapp.generated.resources.pattern_list_filter
import openyarnstash.composeapp.generated.resources.pattern_list_title
import openyarnstash.composeapp.generated.resources.patterns
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternListScreen(
    patterns: List<Pattern>,
    pdfManager: PdfManager,
    onAddClick: () -> Unit,
    onOpen: (UInt) -> Unit,
    onBack: () -> Unit
) {
    BackButtonHandler {
        onBack()
    }

    var filter by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.patterns),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.pattern_list_title))
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
            if (patterns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(stringResource(Res.string.pattern_list_empty))
                }
            } else {
                OutlinedTextField(
                    value = filter,
                    onValueChange = { filter = it },
                    label = { Text(stringResource(Res.string.pattern_list_filter)) },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = stringResource(Res.string.pattern_list_filter)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                val filteredPatterns = patterns.filter {
                    if (filter.isNotBlank()) {
                        it.name.contains(filter, ignoreCase = true) ||
                        it.creator?.contains(filter, ignoreCase = true) == true ||
                        it.category?.contains(filter, ignoreCase = true) == true
                    } else {
                        true
                    }
                }.sortedBy { it.name }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    items(filteredPatterns) { pattern ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            onClick = { onOpen(pattern.id) },
                            colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(pattern.id))
                        ) {

                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
                                LaunchedEffect(pattern.id) {
                                    imageBytes = pdfManager.getPatternPdfThumbnail(pattern.id)
                                }
                                val bitmap = remember(imageBytes) { imageBytes?.toImageBitmap() }

                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap,
                                        contentDescription = "Pattern thumbnail for ${pattern.name}",
                                        modifier = Modifier.size(64.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(Res.drawable.patterns),
                                        contentDescription = "Pattern icon",
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pattern.name, style = MaterialTheme.typography.titleMedium)
                                    pattern.creator?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall)
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
