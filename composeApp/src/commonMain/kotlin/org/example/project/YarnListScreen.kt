package org.example.project

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.common_back
import knittingappmultiplatt.composeapp.generated.resources.yarn_list_empty
import knittingappmultiplatt.composeapp.generated.resources.yarn_list_title
import knittingappmultiplatt.composeapp.generated.resources.common_plus_symbol
import knittingappmultiplatt.composeapp.generated.resources.yarn_item_display_name_color
import knittingappmultiplatt.composeapp.generated.resources.yarn_item_display_amount_g
import knittingappmultiplatt.composeapp.generated.resources.item_label_url
import knittingappmultiplatt.composeapp.generated.resources.item_label_date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YarnListScreen(
    yarns: List<Yarn>,
    onAddClick: () -> Unit,
    onOpen: (Int) -> Unit,
    onBack: () -> Unit      // <-- new
) {
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
            FloatingActionButton(onClick = onAddClick) { Text(stringResource(Res.string.common_plus_symbol)) }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        if (yarns.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(stringResource(Res.string.yarn_list_empty))
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(yarns) { yarn ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(yarn.id) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(stringResource(Res.string.yarn_item_display_name_color, yarn.name, yarn.color ?: "?"))
                            Text(stringResource(Res.string.yarn_item_display_amount_g, yarn.amount))
                            yarn.url?.let { Text(stringResource(Res.string.item_label_url, it)) }
                            yarn.date?.let { Text(stringResource(Res.string.item_label_date, it)) }
                        }
                    }
                    Divider()
                }
            }
        }
    }
}
