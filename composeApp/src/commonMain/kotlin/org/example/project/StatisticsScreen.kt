package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(Res.drawable.statistics),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.statistics_title))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
                    }
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            item {
                val totalAvailable = yarns.sumOf { yarn ->
                    val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                    (yarn.amount - used).coerceAtLeast(0)
                }
                Text(stringResource(Res.string.statistics_total_yarn_weight, totalAvailable))

                val totalAvailableMeterage = yarns.sumOf { yarn ->
                    val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                    val available = (yarn.amount - used).coerceAtLeast(0)
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) {
                        (available * meterage) / weight
                    } else {
                        0
                    }
                }
                Text(stringResource(Res.string.statistics_total_yarn_meterage, totalAvailableMeterage))

                val projectsPlanned = projects.count { it.status == ProjectStatus.PLANNING }
                Text(stringResource(Res.string.statistics_projects_planned, projectsPlanned))

                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }


            val yarnBoughtByYear = yarns.groupBy { it.added?.substring(0, 4) }
            val finishedProjectsByYear = projects.filter { it.status == ProjectStatus.FINISHED }.groupBy { it.endDate?.substring(0, 4) }

            val years = (yarnBoughtByYear.keys + finishedProjectsByYear.keys).filterNotNull().toSet().sortedDescending()

            items(years) { year ->
                Text(year, style = MaterialTheme.typography.headlineMedium)
                val yarnBoughtThisYear = yarnBoughtByYear[year] ?: emptyList()
                Text(stringResource(Res.string.statistics_yarn_bought_this_year, yarnBoughtThisYear.sumOf { it.amount }))

                val yarnBoughtThisYearMeterage = yarnBoughtThisYear.sumOf { yarn ->
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) {
                        (yarn.amount * meterage) / weight
                    } else {
                        0
                    }
                }
                Text(stringResource(Res.string.statistics_yarn_bought_this_year_meterage, yarnBoughtThisYearMeterage))


                val finishedProjectsThisYear = finishedProjectsByYear[year] ?: emptyList()

                val yarnUsedThisYear = finishedProjectsThisYear
                    .sumOf { project ->
                        usages.filter { it.projectId == project.id }.sumOf { it.amount }
                    }

                Text(stringResource(Res.string.statistics_yarn_used_this_year, yarnUsedThisYear))

                val yarnUsedThisYearMeterage = finishedProjectsThisYear.sumOf { project ->
                    usages.filter { it.projectId == project.id }.sumOf { usage ->
                        val yarn = yarns.find { it.id == usage.yarnId }
                        if (yarn != null) {
                            val meterage = yarn.meteragePerSkein
                            val weight = yarn.weightPerSkein
                            if (meterage != null && weight != null && weight > 0) {
                                (usage.amount * meterage) / weight
                            } else {
                                0
                            }
                        } else {
                            0
                        }
                    }
                }
                Text(stringResource(Res.string.statistics_yarn_used_this_year_meterage, yarnUsedThisYearMeterage))

                val projectsFinishedThisYearCount = finishedProjectsThisYear.size
                Text(stringResource(Res.string.statistics_projects_finished_this_year, projectsFinishedThisYearCount))

                val yearInt = year.toIntOrNull() ?: 0
                val projectsInProgressThisYear = projects.count { project ->
                    val started = project.startDate?.substring(0, 4)?.toIntOrNull() ?: 0
                    if (started == 0 || started > yearInt) {
                        false
                    } else {
                        if (project.status == ProjectStatus.IN_PROGRESS) {
                            true
                        } else if (project.status == ProjectStatus.FINISHED) {
                            val finished = project.endDate?.substring(0, 4)?.toIntOrNull()
                            finished != null && finished > yearInt
                        } else {
                            false
                        }
                    }
                }
                Text(stringResource(Res.string.statistics_projects_in_progress, projectsInProgressThisYear))


                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}
