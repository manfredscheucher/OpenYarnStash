package org.example.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.*
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(Res.string.help_screen_title),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(Res.string.help_screen_title))
                    }
                },
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
                                    append(stringResource(Res.string.support_us_on_kofi))
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
                                    append(stringResource(Res.string.use_the_app_and_report_issues))
                                }
                            }
                        )
                        Text(
                            text = stringResource(Res.string.report_bugs_or_translation_mistakes)
                        )
                        Text(
                            text = stringResource(Res.string.to_help_us_reproduce_bugs)
                        )
                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(text = stringResource(Res.string.steps_to_reproduce))
                            Text(text = stringResource(Res.string.screenshots_if_possible))
                            Text(text = stringResource(Res.string.app_version_platform))
                            Text(text = stringResource(Res.string.data_export_from_app))
                        }
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(stringResource(Res.string.privacy_note))
                                }
                                append(stringResource(Res.string.only_share_what_you_are_comfortable_with))
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { uriHandler.openUri("https://github.com/manfredscheucher/OpenYarnStash") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.github_button))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { uriHandler.openUri("mailto:openyarnstash@proton.me") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.email_button))
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
                                    append(stringResource(Res.string.suggest_new_features))
                                }
                            }
                        )
                        Text(
                            text = stringResource(Res.string.open_a_feature_request_on_github)
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
                                    append(stringResource(Res.string.join_the_discussion))
                                }
                            }
                        )
                        Text(
                            text = stringResource(Res.string.participate_in_github_discussions)
                        )
                    }
                }
            }
        }
    }
}
