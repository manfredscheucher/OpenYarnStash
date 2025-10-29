package org.example.project

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import io.github.koalaplot.core.bar.BarScope
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotGroupedPointEntry
import io.github.koalaplot.core.bar.DefaultVerticalBarPosition
import io.github.koalaplot.core.bar.GroupedVerticalBarPlot
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel
import openyarnstash.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
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

    AppBackHandler { onBack() }

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {

            // --- Summary & Filter ---
            item {
                val totalAvailableWeight = yarns.sumOf { yarn ->
                    val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                    (yarn.amount - used).coerceAtLeast(0)
                }
                val totalAvailableMeterage = yarns.sumOf { yarn ->
                    val used = usages.filter { it.yarnId == yarn.id }.sumOf { it.amount }
                    val available = (yarn.amount - used).coerceAtLeast(0)
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) {
                        (available * meterage) / weight
                    } else 0
                }
                Text(
                    stringResource(
                        Res.string.statistics_total_yarn_available,
                        totalAvailableWeight,
                        totalAvailableMeterage
                    )
                )

                val projectsPlanned = projects.count { it.status == ProjectStatus.PLANNING }
                Text(stringResource(Res.string.statistics_projects_planned, projectsPlanned))

                Divider(Modifier.padding(vertical = 16.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = if (selectedTimespan == "year")
                            stringResource(Res.string.statistics_per_year)
                        else
                            stringResource(Res.string.statistics_per_month),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        timespanOptions.forEach { timespan ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (timespan == "year")
                                            stringResource(Res.string.statistics_per_year)
                                        else
                                            stringResource(Res.string.statistics_per_month)
                                    )
                                },
                                onClick = {
                                    selectedTimespan = timespan
                                    onSettingsChange(settings.copy(statisticTimespan = timespan))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            val groupingKey: (String?) -> String? = if (selectedTimespan == "year") {
                { date -> date?.takeIf { it.length >= 4 }?.substring(0, 4) }
            } else {
                { date ->
                    when {
                        date == null -> null
                        date.length >= 7 -> date.substring(0, 7) // YYYY-MM
                        date.length >= 4 -> "${date.substring(0, 4)}-01"
                        else -> null
                    }
                }
            }

            val yarnBoughtByGroup = yarns.groupBy { groupingKey(it.added) }
            val finishedProjectsByGroup =
                projects.filter { it.status == ProjectStatus.FINISHED }.groupBy { groupingKey(it.endDate) }

            val groups = (yarnBoughtByGroup.keys + finishedProjectsByGroup.keys)
                .filterNotNull()
                .toSet()
                .sorted()

            item {
                val labelsBase = listOf("Yarn Bought", "Yarn Used")
                val basePalette = listOf(
                    Color(0xFF1E88E5), // Blau
                    Color(0xFFFFA000), // Amber
                    Color(0xFF43A047), // Grün
                    Color(0xFF8E24AA)  // Violett
                )

                val barChartData: List<DefaultVerticalBarPlotGroupedPointEntry<String, Float>> = groups.map { group ->
                    val yarnBought = yarnBoughtByGroup[group]?.sumOf { it.amount }?.toFloat() ?: 0f
                    val yarnUsed = finishedProjectsByGroup[group]?.sumOf { project ->
                        usages.filter { it.projectId == project.id }.sumOf { it.amount }
                    }?.toFloat() ?: 0f

                    DefaultVerticalBarPlotGroupedPointEntry(
                        x = group,
                        y = listOf(
                            DefaultVerticalBarPosition(0f, yarnBought),
                            DefaultVerticalBarPosition(0f, yarnUsed)
                        )
                    )
                }

                if (barChartData.isNotEmpty()) {
                    val seriesCount = barChartData.maxOf { it.y.size }
                    require(seriesCount > 0) { "No series to plot" }
                    check(barChartData.all { it.y.size == seriesCount }) {
                        val bad = barChartData.first { it.y.size != seriesCount }
                        "Inconsistent series size. Group=${bad.x} has ${bad.y.size}, expected $seriesCount"
                    }

                    val legendColors: List<Color> =
                        if (basePalette.size >= seriesCount) basePalette.take(seriesCount)
                        else List(seriesCount) { i -> basePalette[i % basePalette.size] }

                    val legendLabels: List<String> =
                        if (labelsBase.size >= seriesCount) labelsBase.take(seriesCount)
                        else List(seriesCount) { i -> labelsBase.getOrElse(i) { "Series ${i + 1}" } }

                    val yMax = barChartData
                        .flatMap { entry -> entry.y.map { it.yMax } }
                        .maxOrNull()
                        ?.coerceAtLeast(1f) ?: 1f

                    KoalaPlotTheme {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            FlowLegend(
                                itemCount = seriesCount,
                                symbol = { i: Int ->
                                    Box(Modifier.size(10.dp).background(legendColors[i]))
                                },
                                label = { i: Int ->
                                    Text(legendLabels[i])
                                }
                            )
                        }

                        key(selectedTimespan) {
                            val xCats: List<String> = barChartData.map { it.x }
                            val xModel = CategoryAxisModel<String>(xCats)
                            val yModel = rememberFloatLinearAxisModel(range = 0f..yMax)

                            val barRenderer: @Composable BarScope.(groupIndex: Int, series: Int, entry: DefaultVerticalBarPlotGroupedPointEntry<String, Float>) -> Unit =
                                { _, series, _ ->
                                    DefaultVerticalBar(color = legendColors[series])
                                }

                            XYGraph(
                                modifier = Modifier
                                    .height(340.dp)
                                    .fillMaxWidth(),
                                xAxisModel = xModel,
                                yAxisModel = yModel,
                                xAxisLabels = { category: String ->
                                    Text(
                                        text = formatCategoryLabel(category, selectedTimespan, settings.language),
                                        modifier = Modifier.rotate(90f)
                                    )
                                },
                                yAxisLabels = { yValue: Float ->
                                    Text(yValue.toInt().toString())
                                },
                                xAxisTitle = {},
                                yAxisTitle = {}
                            ) {
                                GroupedVerticalBarPlot(
                                    data = barChartData,
                                    bar = barRenderer,
                                    animationSpec = tween(durationMillis = 500)
                                )
                            }
                        }
                    }
                }
            }

            items(groups) { group ->
                val displayText = if (selectedTimespan == "year") {
                    group
                } else {
                    val year = group.take(4)
                    val month = group.drop(5).take(2).toIntOrNull()
                    val monthName = if (month != null) getMonthName(month, settings.language) else group
                    "$monthName $year"
                }
                Text(displayText, style = MaterialTheme.typography.headlineMedium)

                val yarnBoughtThisGroup = yarnBoughtByGroup[group] ?: emptyList()
                val yarnBoughtThisGroupAmount = yarnBoughtThisGroup.sumOf { it.amount }
                val yarnBoughtThisGroupMeterage = yarnBoughtThisGroup.sumOf { yarn ->
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) {
                        (yarn.amount * meterage) / weight
                    } else 0
                }
                Text(
                    stringResource(
                        Res.string.statistics_yarn_bought,
                        yarnBoughtThisGroupAmount,
                        yarnBoughtThisGroupMeterage
                    )
                )

                val finishedProjectsThisGroup = finishedProjectsByGroup[group] ?: emptyList()

                val yarnUsedThisGroupAmount = finishedProjectsThisGroup
                    .sumOf { project ->
                        usages.filter { it.projectId == project.id }.sumOf { it.amount }
                    }
                val yarnUsedThisGroupMeterage = finishedProjectsThisGroup.sumOf { project ->
                    usages.filter { it.projectId == project.id }.sumOf { usage ->
                        val yarn = yarns.find { it.id == usage.yarnId }
                        if (yarn != null) {
                            val meterage = yarn.meteragePerSkein
                            val weight = yarn.weightPerSkein
                            if (meterage != null && weight != null && weight > 0) {
                                (usage.amount * meterage) / weight
                            } else 0
                        } else 0
                    }
                }
                Text(
                    stringResource(
                        Res.string.statistics_yarn_used,
                        yarnUsedThisGroupAmount,
                        yarnUsedThisGroupMeterage
                    )
                )

                val projectsFinishedThisGroupCount = finishedProjectsThisGroup.size
                Text(stringResource(Res.string.statistics_projects_finished, projectsFinishedThisGroupCount))

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
                        } else false
                    }
                }
                Text(stringResource(Res.string.statistics_projects_in_progress, projectsInProgressThisGroup))

                Divider(Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

private fun formatCategoryLabel(category: String, selectedTimespan: String, language: String): String {
    if (selectedTimespan == "year") return category
    if (category.length >= 7 && category.getOrNull(4) == '-') {
        val year = category.substring(0, 4)
        val month = category.substring(5, 7).toIntOrNull()
        if (month != null && month in 1..12) {
            return "${getMonthName(month, language)} ${year.takeLast(2)}"
        }
    }
    return category
}

private fun getMonthName(month: Int, language: String): String {
    val monthNames = when (language) {
        "de" -> listOf(
            "Januar", "Februar", "März", "April", "Mai", "Juni",
            "Juli", "August", "September", "Oktober", "November", "Dezember"
        )
        else -> listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }
    return if (month in 1..12) monthNames[month - 1] else ""
}
