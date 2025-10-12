package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.home_title
import knittingappmultiplatt.composeapp.generated.resources.home_button_yarns
import knittingappmultiplatt.composeapp.generated.resources.home_button_projects
import knittingappmultiplatt.composeapp.generated.resources.home_button_statistics
import knittingappmultiplatt.composeapp.generated.resources.logo
import knittingappmultiplatt.composeapp.generated.resources.info_screen_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenYarns: () -> Unit,
    onOpenProjects: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    Scaffold(
        topBar = { 
            TopAppBar(title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.logo),
                        contentDescription = "OpenStash Logo",
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(Res.string.home_title))
                }
            })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Button(onClick = onOpenYarns) { Text(stringResource(Res.string.home_button_yarns)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenProjects) { Text(stringResource(Res.string.home_button_projects)) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = onOpenStatistics) { Text(stringResource(Res.string.home_button_statistics)) }
            Spacer(Modifier.height(24.dp))
            Button(onClick = onOpenInfo) { Text(stringResource(Res.string.info_screen_title)) }
        }
    }
}
