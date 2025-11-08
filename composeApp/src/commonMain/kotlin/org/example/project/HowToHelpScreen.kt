package org.example.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToHelpScreen(
    onBack: () -> Unit
) {
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
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("If you like the app: Support us on Ko-fi. ")
                        }
                        append("(insert link)")
                    }
                )
            }
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Use it & report issues:")
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Report bugs or translation mistakes via GitHub Issues or email."
                )
            }
            item {
                Text(
                    text = "To help us reproduce quickly, please include:"
                )
            }
            item {
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(text = "• Steps to reproduce")
                    Text(text = "• Screenshots (if possible)")
                    Text(text = "• App version/platform")
                    Text(text = "• Data export (ZIP) from the app (Settings → Export data)")
                }
            }
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Suggest new features:")
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Open a feature request on GitHub. Describe the goal, why it helps, and examples."
                )
            }
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Join the discussion:")
                        }
                    }
                )
            }
            item {
                Text(
                    text = "Participate in GitHub Discussions, upvote issues, and share feedback."
                )
            }
            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Privacy note: ")
                        }
                        append("Only share what you’re comfortable with. Remove sensitive info from attachments/exports.")
                    }
                )
            }
        }
    }
}
