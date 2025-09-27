package org.example.project

// commonMain
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenYarns: () -> Unit,
    onOpenProjects: () -> Unit = {}
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Yarns", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onOpenYarns) { Text("Zur Yarn-Liste") }

            // Platzhalter für später
            Spacer(Modifier.height(24.dp))
            Text("Projects", style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = onOpenProjects, enabled = false) { Text("Bald…") }
        }
    }
}
