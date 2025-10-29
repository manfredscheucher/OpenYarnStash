package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    yarns: List<Yarn>,
    projects: List<Project>,
    usages: List<Usage>,
    settings: Settings,
    onBack: () -> Unit,
    onSettingsChange: (Settings) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timespanOptions = listOf("year", "month")
    var selectedTimespan by remember { mutableStateOf(settings.statisticTimespan) }

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

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (selectedTimespan == "year") stringResource(Res.string.statistics_per_year) else stringResource(Res.string.statistics_per_month),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timespanOptions.forEach { timespan ->
                            DropdownMenuItem(
                                text = { Text(if (timespan == "year") stringResource(Res.string.statistics_per_year) else stringResource(Res.string.statistics_per_month)) },
                                onClick = {
                                    selectedTimespan = timespan
                                    onSettingsChange(settings.copy(statisticTimespan = timespan))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(vertical = 8.dp))
            }

            val groupingKey: (String?) -> String? = if (selectedTimespan == "year") {
                { date -> date?.takeIf { it.length >= 4 }?.substring(0, 4) }
            } else {
                { date ->
                    when {
                        date == null -> null
                        date.length >= 7 -> date.substring(0, 7)
                        date.length >= 4 -> "${date.substring(0, 4)}-01"
                        else -> null
                    }
                }
            }

            val yarnBoughtByGroup = yarns.groupBy { groupingKey(it.added) }
            val finishedProjectsByGroup = projects.filter { it.status == ProjectStatus.FINISHED }.groupBy { groupingKey(it.endDate) }

            val groups = (yarnBoughtByGroup.keys + finishedProjectsByGroup.keys).filterNotNull().toSet().sortedDescending()

            items(groups) { group ->
                val displayText = if (selectedTimespan == "year") {
                    group
                } else {
                    val year = group.substring(0, 4)
                    val month = group.substring(5, 7).toInt()
                    val monthName = getMonthName(month, settings.language)
                    "$monthName $year"
                }
                Text(displayText, style = MaterialTheme.typography.headlineMedium)

                val yarnBoughtThisGroup = yarnBoughtByGroup[group] ?: emptyList()
                Text(stringResource(Res.string.statistics_yarn_bought_this_year, yarnBoughtThisGroup.sumOf { it.amount }))

                val yarnBoughtThisGroupMeterage = yarnBoughtThisGroup.sumOf { yarn ->
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) {
                        (yarn.amount * meterage) / weight
                    } else {
                        0
                    }
                }
                Text(stringResource(Res.string.statistics_yarn_bought_this_year_meterage, yarnBoughtThisGroupMeterage))


                val finishedProjectsThisGroup = finishedProjectsByGroup[group] ?: emptyList()

                val yarnUsedThisGroup = finishedProjectsThisGroup
                    .sumOf { project ->
                        usages.filter { it.projectId == project.id }.sumOf { it.amount }
                    }

                Text(stringResource(Res.string.statistics_yarn_used_this_year, yarnUsedThisGroup))

                val yarnUsedThisGroupMeterage = finishedProjectsThisGroup.sumOf { project ->
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
                Text(stringResource(Res.string.statistics_yarn_used_this_year_meterage, yarnUsedThisGroupMeterage))

                val projectsFinishedThisGroupCount = finishedProjectsThisGroup.size
                Text(stringResource(Res.string.statistics_projects_finished_this_year, projectsFinishedThisGroupCount))


                val projectsInProgressThisGroup = projects.count { project ->
                    val startGroup = groupingKey(project.startDate)
                    if (startGroup == null || startGroup > group) {
                        false
                    } else {
                        if (project.status == ProjectStatus.IN_PROGRESS) {
                            true
                        } else if (project.status == ProjectStatus.FINISHED) {
                            val finishedGroup = groupingKey(project.endDate)
                            finishedGroup != null && finishedGroup > group
                        } else {
                            false
                        }
                    }
                }
                Text(stringResource(Res.string.statistics_projects_in_progress, projectsInProgressThisGroup))


                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

private fun getMonthName(month: Int, language: String): String {
    val monthNames = when (language) {
        "de" -> listOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")
        else -> listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }
    return if (month in 1..12) monthNames[month - 1] else ""
}
