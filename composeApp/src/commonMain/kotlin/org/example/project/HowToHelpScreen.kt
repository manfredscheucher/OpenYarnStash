package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.email_button
import openyarnstash.composeapp.generated.resources.github_button
import openyarnstash.composeapp.generated.resources.github_issues_button
import openyarnstash.composeapp.generated.resources.info_github_button
import openyarnstash.composeapp.generated.resources.ko_fi_button
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToHelpScreen(
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How you can help") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(1))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Support us on Ko-fi:")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { uriHandler.openUri("https://ko-fi.com/openyarnstash") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.ko_fi_button))
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(2))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Use the app & report issues:")
                                }
                            }
                        )
                        Text(
                            text = "Report bugs or translation mistakes via GitHub Issues or email."
                        )
                        Text(
                            text = "To help us reproduce the bugs, please include:"
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = "• Steps to reproduce")
                            Text(text = "• Screenshots (if possible)")
                            Text(text = "• App version/platform")
                            Text(text = "• Data export (ZIP) from the app (Settings → Export data)")
                        }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Privacy note: ")
                                }
                                append("Only share what you’re comfortable with. Remove sensitive info from attachments/exports.")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { uriHandler.openUri("https://github.com/manfredscheucher/OpenYarnStash/issues") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.github_issues_button))
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(3))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Suggest new features:")
                                }
                            }
                        )
                        Text(
                            text = "Open a feature request on GitHub. Describe the goal, why it helps, and give examples."
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ColorPalette.idToColor(4))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Join the discussion:")
                                }
                            }
                        )
                        Text(
                            text = "Participate in GitHub Discussions, upvote issues, and share feedback."
                        )
                    }
                }
            }
        }
    }
}
