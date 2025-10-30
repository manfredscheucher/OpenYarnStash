package org.example.project

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onBack: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val overallOption = stringResource(Res.string.statistics_overall)

    val yearsFromYarns = remember(yarns) {
        yarns.mapNotNull { it.added?.take(4)?.toIntOrNull() }
    }
    val yearsFromProjects = remember(projects) {
        projects.flatMap { p ->
            listOfNotNull(
                p.startDate?.take(4)?.toIntOrNull(),
                p.endDate?.take(4)?.toIntOrNull()
            )
        }
    }
    val allYearsDetected = remember(yearsFromYarns, yearsFromProjects) {
        (yearsFromYarns + yearsFromProjects).distinct().sorted()
    }
    val yearOptionsDesc = remember(allYearsDetected) { allYearsDetected.asReversed().map { it.toString() } }

    val dropdownOptions = remember(yearOptionsDesc) {
        buildList {
            add(overallOption)
            addAll(yearOptionsDesc)
        }
    }

    var selectedFilter by remember { mutableStateOf(overallOption) }

    val isOverall = selectedFilter == overallOption

    val categories: List<String> = remember(isOverall, allYearsDetected, selectedFilter) {
        if (isOverall) {
            val minYear = allYearsDetected.firstOrNull()
            val maxYear = allYearsDetected.lastOrNull()
            if (minYear != null && maxYear != null) {
                (minYear..maxYear).map { it.toString() } // Years "YYYY"
            } else emptyList()
        } else {
            val year = selectedFilter.toIntOrNull()
            if (year != null) {
                (1..12).map { m ->
                    "$year-${m.toString().padStart(2, '0')}" // Months "YYYY-MM"
                }
            } else emptyList()
        }
    }

    fun yearKey(date: String?): String? = date?.take(4)
    fun monthKey(date: String?): String? = date?.take(7)

    val yarnsByYear = remember(yarns) { yarns.groupBy { yearKey(it.added) } }
    val yarnsByMonth = remember(yarns) { yarns.groupBy { monthKey(it.added) } }

    val finishedProjects = remember(projects) { projects.filter { it.status == ProjectStatus.FINISHED } }
    val finishedByYear = remember(finishedProjects) { finishedProjects.groupBy { yearKey(it.endDate) } }
    val finishedByMonth = remember(finishedProjects) { finishedProjects.groupBy { monthKey(it.endDate) } }

    fun projectActiveInCategory(project: Project, category: String): Boolean {
        val start = if (isOverall) yearKey(project.startDate) else monthKey(project.startDate)
        val end = if (isOverall) yearKey(project.endDate) else monthKey(project.endDate)
        val started = start != null && start <= category
        val notFinishedYet = when (project.status) {
            ProjectStatus.IN_PROGRESS -> true
            ProjectStatus.FINISHED -> end != null && end > category
            else -> false
        }
        return started && notFinishedYet
    }

    val inProgressCountByCat: Map<String, Int> = remember(projects, categories, isOverall) {
        categories.associateWith { cat ->
            projects.count { p -> projectActiveInCategory(p, cat) }
        }
    }

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
                    if (meterage != null && weight != null && weight > 0) (available * meterage) / weight else 0
                }
                Text(
                    text = if (settings.lengthUnit == LengthUnit.METER) stringResource(
                        Res.string.statistics_total_yarn_available,
                        totalAvailableWeight,
                        totalAvailableMeterage
                    ) else stringResource(
                        Res.string.statistics_total_yarn_available_yards,
                        totalAvailableWeight,
                        convertLength(totalAvailableMeterage, LengthUnit.YARD).toInt()
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
                        value = selectedFilter,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        dropdownOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedFilter = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            item {
                ChartBlock(
                    title = stringResource(if (isOverall) Res.string.statistics_yarn_bought_vs_used_yearly else Res.string.statistics_yarn_bought_vs_used_monthly),
                    categories = categories,
                    seriesLabels = listOf(stringResource(Res.string.statistics_series_yarn_bought), stringResource(Res.string.statistics_series_yarn_used)),
                    seriesColors = listOf(Color(0xFF1E88E5), Color(0xFFFFA000)),
                    valuesForCategory = { cat ->
                        if (isOverall) {
                            val bought = (yarnsByYear[cat]?.sumOf { it.amount } ?: 0).toFloat()
                            val used = (finishedByYear[cat]?.sumOf { proj ->
                                usages.filter { it.projectId == proj.id }.sumOf { it.amount }
                            } ?: 0).toFloat()
                            listOf(bought, used)
                        } else {
                            val bought = (yarnsByMonth[cat]?.sumOf { it.amount } ?: 0).toFloat()
                            val used = (finishedByMonth[cat]?.sumOf { proj ->
                                usages.filter { it.projectId == proj.id }.sumOf { it.amount }
                            } ?: 0).toFloat()
                            listOf(bought, used)
                        }
                    },
                    labelFormatter = { cat -> formatCategoryLabel(cat, isOverall) }
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                ChartBlock(
                    title = stringResource(if (isOverall) Res.string.statistics_projects_finished_vs_in_progress_yearly else Res.string.statistics_projects_finished_vs_in_progress_monthly),
                    categories = categories,
                    seriesLabels = listOf(stringResource(Res.string.statistics_series_projects_finished), stringResource(Res.string.statistics_series_projects_in_progress)),
                    seriesColors = listOf(Color(0xFF43A047), Color(0xFF8E24AA)),
                    valuesForCategory = { cat ->
                        val finishedCount = (if (isOverall) finishedByYear[cat]?.size else finishedByMonth[cat]?.size)?.toFloat() ?: 0f
                        val inProgressCount = inProgressCountByCat[cat]?.toFloat() ?: 0f
                        listOf(finishedCount, inProgressCount)
                    },
                    labelFormatter = { cat -> formatCategoryLabel(cat, isOverall) }
                )
                Spacer(Modifier.height(24.dp))
            }

            items(categories) { cat ->
                val header = if (isOverall) cat else {
                    val year = cat.take(4)
                    val month = cat.drop(5).take(2).toIntOrNull()
                    val monthName = month?.let { getMonthName(it) } ?: cat
                    "$monthName $year"
                }
                Text(header, style = MaterialTheme.typography.headlineMedium)

                val yarnBoughtList = if (isOverall) yarnsByYear[cat] else yarnsByMonth[cat]
                val yarnBoughtAmount = yarnBoughtList?.sumOf { it.amount } ?: 0
                val yarnBoughtMeterage = yarnBoughtList?.sumOf { yarn ->
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) (yarn.amount * meterage) / weight else 0
                } ?: 0

                Text(
                    text = if (settings.lengthUnit == LengthUnit.METER) stringResource(
                        Res.string.statistics_yarn_bought_title,
                        yarnBoughtAmount,
                        yarnBoughtMeterage
                    ) else stringResource(
                        Res.string.statistics_yarn_bought_title_yards,
                        yarnBoughtAmount,
                        convertLength(yarnBoughtMeterage, LengthUnit.YARD).toInt()
                    )
                )

                val finishedList = if (isOverall) finishedByYear[cat] else finishedByMonth[cat]
                val yarnUsedAmount = finishedList?.sumOf { proj ->
                    usages.filter { it.projectId == proj.id }.sumOf { it.amount }
                } ?: 0
                val yarnUsedMeterage = finishedList?.sumOf { proj ->
                    usages.filter { it.projectId == proj.id }.sumOf { usage ->
                        val yarn = yarns.find { it.id == usage.yarnId }
                        if (yarn != null) {
                            val meterage = yarn.meteragePerSkein
                            val weight = yarn.weightPerSkein
                            if (meterage != null && weight != null && weight > 0) (usage.amount * meterage) / weight else 0
                        } else 0
                    }
                } ?: 0

                Text(
                    text = if (settings.lengthUnit == LengthUnit.METER) stringResource(
                        Res.string.statistics_yarn_used_title,
                        yarnUsedAmount,
                        yarnUsedMeterage
                    ) else stringResource(
                        Res.string.statistics_yarn_used_title_yards,
                        yarnUsedAmount,
                        convertLength(yarnUsedMeterage, LengthUnit.YARD).toInt()
                    )
                )

                val finishedCount = finishedList?.size ?: 0
                Text(stringResource(Res.string.statistics_projects_finished_title, finishedCount))

                val inProgressCount = inProgressCountByCat[cat] ?: 0
                Text(stringResource(Res.string.statistics_projects_in_progress_title, inProgressCount))

                Divider(Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun ChartBlock(
    title: String,
    categories: List<String>,
    seriesLabels: List<String>,
    seriesColors: List<Color>,
    valuesForCategory: (String) -> List<Float>,
    labelFormatter: @Composable (String) -> String
) {
    if (categories.isEmpty()) return

    val xAxisLabelAreaHeight = 40.dp
    val xAxisLabelTextWidth  = 140.dp

    val barData: List<DefaultVerticalBarPlotGroupedPointEntry<String, Float>> =
        categories.mapNotNull { cat ->
            val values = valuesForCategory(cat)
            if (values.all { it == 0f }) null
            else {
                val yPositions = values.map { v -> DefaultVerticalBarPosition(0f, v) }
                DefaultVerticalBarPlotGroupedPointEntry(x = cat, y = yPositions)
            }
        }

    if (barData.isEmpty()) return

    val seriesCount = barData.first().y.size
    require(seriesLabels.size == seriesCount && seriesColors.size == seriesCount) {
        "Series labels/colors must match data series count"
    }

    val yMax = barData.flatMap { it.y.map { pos -> pos.yMax } }.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 56.dp))
        Spacer(Modifier.height(8.dp))

        KoalaPlotTheme {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FlowLegend(
                    itemCount = seriesCount,
                    symbol = { i -> Box(Modifier.size(10.dp).background(seriesColors[i])) },
                    label = { i -> Text(seriesLabels[i]) }
                )
            }

            val xModel = CategoryAxisModel(barData.map { it.x })
            val yModel = rememberFloatLinearAxisModel(range = 0f..yMax, minorTickCount = 0)

            XYGraph(
                modifier = Modifier
                    .height(320.dp + xAxisLabelAreaHeight) // Plot ~320dp + reservierte Labelhöhe
                    .fillMaxWidth(),
                xAxisModel = xModel,
                yAxisModel = yModel,
                xAxisLabels = { cat ->
                    val txt = labelFormatter(cat)
                    Box(
                        modifier = Modifier
                            .height(xAxisLabelAreaHeight)
                            .width(xAxisLabelTextWidth),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Text(
                            text = txt,
                            modifier = Modifier.rotate(-90f),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    }
                },
                xAxisTitle = {},
                yAxisLabels = { y -> Text(y.toInt().toString()) },
                yAxisTitle = {}
            ) {
                GroupedVerticalBarPlot(
                    data = barData,
                    bar = { _, series, _ -> DefaultVerticalBar(color = seriesColors[series]) },
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }
}

@Composable
private fun formatCategoryLabel(category: String, isOverall: Boolean): String {
    return if (isOverall) {
        category
    } else {
        if (category.length >= 7) {
            //val year = category.substring(2, 4)
            val month = category.substring(5, 7).toIntOrNull()
            month?.let { getMonthShort(it) } ?: ""
        } else category
    }
}

@Composable
private fun getMonthShort(month: Int): String {
    val resId = when (month) {
        1 -> Res.string.month_january
        2 -> Res.string.month_february
        3 -> Res.string.month_march
        4 -> Res.string.month_april
        5 -> Res.string.month_may
        6 -> Res.string.month_june
        7 -> Res.string.month_july
        8 -> Res.string.month_august
        9 -> Res.string.month_september
        10 -> Res.string.month_october
        11 -> Res.string.month_november
        12 -> Res.string.month_december
        else -> null
    }
    return resId?.let { stringResource(it).take(3) } ?: ""
}

@Composable
private fun getMonthName(month: Int): String {
    val resId = when (month) {
        1 -> Res.string.month_january
        2 -> Res.string.month_february
        3 -> Res.string.month_march
        4 -> Res.string.month_april
        5 -> Res.string.month_may
        6 -> Res.string.month_june
        7 -> Res.string.month_july
        8 -> Res.string.month_august
        9 -> Res.string.month_september
        10 -> Res.string.month_october
        11 -> Res.string.month_november
        12 -> Res.string.month_december
        else -> null
    }
    return resId?.let { stringResource(it) } ?: ""
}
