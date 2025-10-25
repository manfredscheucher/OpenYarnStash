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
import openyarnstash.composeapp.generated.resources.statistics_projects_finished_this_year
import openyarnstash.composeapp.generated.resources.statistics_projects_in_progress
import openyarnstash.composeapp.generated.resources.statistics_projects_planned
import openyarnstash.composeapp.generated.resources.statistics_title
import openyarnstash.composeapp.generated.resources.statistics_total_yarn_weight
import openyarnstash.composeapp.generated.resources.statistics_yarn_bought_this_year
import openyarnstash.composeapp.generated.resources.statistics_yarn_used_this_year
import org.jetbrains.compose.resources.stringResource
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(yarns: List<Yarn>, projects: List<Project>, usages: List<Usage>, onBack: () -> Unit) {
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

            val currentYear = LocalDate.now().year.toString()
            val yarnBoughtThisYear = yarns
                .filter { it.added?.startsWith(currentYear) == true }
                .sumOf { it.amount }

            Text(stringResource(Res.string.statistics_yarn_bought_this_year, yarnBoughtThisYear))

            val yarnUsedThisYear = projects
                .filter { it.status == ProjectStatus.FINISHED && it.endDate?.startsWith(currentYear) == true }
                .sumOf { project ->
                    usages.filter { it.projectId == project.id }.sumOf { it.amount }
                }

            Text(stringResource(Res.string.statistics_yarn_used_this_year, yarnUsedThisYear))

            val projectsInProgress = projects.count { it.status == ProjectStatus.IN_PROGRESS }
            Text(stringResource(Res.string.statistics_projects_in_progress, projectsInProgress))

            val projectsPlanned = projects.count { it.status == ProjectStatus.PLANNING }
            Text(stringResource(Res.string.statistics_projects_planned, projectsPlanned))

            val projectsFinishedThisYear = projects.count { it.status == ProjectStatus.FINISHED && it.endDate?.startsWith(currentYear) == true }
            Text(stringResource(Res.string.statistics_projects_finished_this_year, projectsFinishedThisYear))
        }
    }
}
