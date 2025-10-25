package org.example.project

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.Res
import openyarnstash.composeapp.generated.resources.common_back
import openyarnstash.composeapp.generated.resources.statistics_title
import openyarnstash.composeapp.generated.resources.statistics_total_yarn_weight
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(yarns: List<Yarn>, usages: List<Usage>, onBack: () -> Unit) {
    AppBackHandler {
        onBack()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            val totalAvailable = yarns.sumOf { yarn ->
                val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                (yarn.amount - used).coerceAtLeast(0)
                //TODO handle negative ones differently
            }
            Text(stringResource(Res.string.statistics_total_yarn_weight, totalAvailable))
        }
    }
}
