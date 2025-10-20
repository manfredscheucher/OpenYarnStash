package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.info_copyright_notice
import openyarnstash.composeapp.generated.resources.info_description
import openyarnstash.composeapp.generated.resources.info_feedback_button
import openyarnstash.composeapp.generated.resources.info_github_button
import openyarnstash.composeapp.generated.resources.info_screen_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    HandleBack {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.info_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            Text(stringResource(Res.string.info_description), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { uriHandler.openUri("https://github.com/manfredscheucher/OpenYarnStash") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.info_github_button))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { uriHandler.openUri("mailto:OpenYarnStash@proton.me?subject=feedback") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.info_feedback_button))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(Res.string.info_copyright_notice), style = MaterialTheme.typography.bodySmall)
        }
    }
}
