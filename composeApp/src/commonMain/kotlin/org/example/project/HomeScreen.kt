package org.example.project

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.home_title
import knittingappmultiplatt.composeapp.generated.resources.home_button_yarns
import knittingappmultiplatt.composeapp.generated.resources.home_button_projects

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenYarns: () -> Unit,
    onOpenProjects: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.home_title)) }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(onClick = onOpenYarns) { Text(stringResource(Res.string.home_button_yarns)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenProjects) { Text(stringResource(Res.string.home_button_projects)) }
        }
    }
}
