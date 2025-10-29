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
    onBack: () -> Unit,
    onSettingsChange: (Settings) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // ---- Jahre aus den Daten ermitteln (KMP-sicher) ----
    val yearsFromYarns = remember(yarns) {
        yarns.mapNotNull { it.added?.let { s -> if (s.length >= 4) s.substring(0, 4).toIntOrNull() else null } }
    }
    val yearsFromProjects = remember(projects) {
        projects.flatMap { p ->
            listOfNotNull(
                p.startDate?.let { s -> if (s.length >= 4) s.substring(0, 4).toIntOrNull() else null },
                p.endDate?.let { s -> if (s.length >= 4) s.substring(0, 4).toIntOrNull() else null }
            )
        }
    }
    val allYearsDetected = remember(yearsFromYarns, yearsFromProjects) {
        (yearsFromYarns + yearsFromProjects).distinct().sorted()
    }
    val hasAtLeastTwoYears = allYearsDetected.size >= 2
    val minYear = allYearsDetected.firstOrNull()
    val maxYear = allYearsDetected.lastOrNull()

    // Dropdown-Optionen: "Gesamt" (nur wenn >=2 Jahre), dann pro Jahr (absteigend)
    val yearOptionsDesc = remember(allYearsDetected) { allYearsDetected.asReversed().map { it.toString() } }
    val dropdownOptions = remember(hasAtLeastTwoYears, yearOptionsDesc) {
        buildList {
            if (hasAtLeastTwoYears) add("Gesamt")
            addAll(yearOptionsDesc)
        }
    }

    // Altes Setting ("year"/"month") -> neues Schema
    var selectedFilter by remember(settings.statisticTimespan, hasAtLeastTwoYears, maxYear) {
        mutableStateOf(
            when (settings.statisticTimespan) {
                "year" -> if (hasAtLeastTwoYears) "Gesamt" else (maxYear?.toString() ?: "Gesamt")
                "month" -> (maxYear?.toString() ?: if (hasAtLeastTwoYears) "Gesamt" else "")
                else -> if (hasAtLeastTwoYears) "Gesamt" else (maxYear?.toString() ?: "")
            }
        )
    }
    if (selectedFilter !in dropdownOptions && dropdownOptions.isNotEmpty()) {
        selectedFilter = if (hasAtLeastTwoYears) "Gesamt" else dropdownOptions.first()
    }

    // Kategorien (AUßERHALB der LazyColumn berechnet) + Lücken auffüllen
    val isOverall = selectedFilter == "Gesamt"
    val categories: List<String> = remember(isOverall, minYear, maxYear, selectedFilter) {
        if (isOverall) {
            if (minYear != null && maxYear != null) {
                (minYear..maxYear).map { it.toString() } // Jahre "YYYY"
            } else emptyList()
        } else {
            val year = selectedFilter.toIntOrNull()
            if (year != null) {
                (1..12).map { m ->
                    val y = year.toString().padStart(4, '0')
                    val mm = m.toString().padStart(2, '0')
                    "$y-$mm" // Monate "YYYY-MM"
                }
            } else emptyList()
        }
    }

    // Gruppier-Keys (KMP-sicher)
    fun yearKey(date: String?): String? =
        date?.let { if (it.length >= 4) it.substring(0, 4) else null }

    fun monthKey(date: String?): String? = when {
        date == null -> null
        date.length >= 7 -> date.substring(0, 7) // YYYY-MM
        date.length >= 4 -> date.substring(0, 4) + "-01"
        else -> null
    }

    // Voraggregationen
    val yarnsByYear = remember(yarns) { yarns.groupBy { yearKey(it.added) } }
    val yarnsByMonth = remember(yarns) { yarns.groupBy { monthKey(it.added) } }

    val finishedProjects = remember(projects) { projects.filter { it.status == ProjectStatus.FINISHED } }
    val finishedByYear = remember(finishedProjects) { finishedProjects.groupBy { yearKey(it.endDate) } }
    val finishedByMonth = remember(finishedProjects) { finishedProjects.groupBy { monthKey(it.endDate) } }

    // Helper: Projekt ist in Kategorie aktiv?
    fun projectActiveInCategory(project: Project, category: String, overall: Boolean): Boolean {
        val start = if (overall) yearKey(project.startDate) else monthKey(project.startDate)
        val end = if (overall) yearKey(project.endDate) else monthKey(project.endDate)
        val started = start != null && start <= category
        val notFinishedYet = when (project.status) {
            ProjectStatus.IN_PROGRESS -> true
            ProjectStatus.FINISHED -> end != null && end > category
            else -> false
        }
        return started && notFinishedYet
    }

    // In-Progress pro Kategorie vorberechnen
    val inProgressCountByCat: Map<String, Int> = remember(projects, categories, isOverall) {
        categories.associateWith { cat ->
            projects.count { p -> projectActiveInCategory(p, cat, isOverall) }
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
                    if (meterage != null && weight != null && weight > 0) (available * meterage) / weight else 0
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

                // --- Dropdown ---
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
                                    // Settings: "Gesamt" => "year", sonst "month"
                                    val mapped = if (option == "Gesamt") "year" else "month"
                                    onSettingsChange(settings.copy(statisticTimespan = mapped))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // --- Chart 1: Yarn Bought / Yarn Used ---
            item {
                ChartBlock(
                    title = if (isOverall) "Garn: Gekauft vs. Verbraucht (Jahre)" else "Garn: Gekauft vs. Verbraucht (Monate)",
                    categories = categories,
                    seriesLabels = listOf("Yarn Bought", "Yarn Used"),
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
                    labelFormatter = { cat -> formatCategoryLabel7(cat, isOverall, settings.language) }
                )
                Spacer(Modifier.height(24.dp))
            }

            // --- Chart 2: Finished vs. In Progress ---
            item {
                ChartBlock(
                    title = if (isOverall) "Projekte: Fertig vs. In Arbeit (Jahre)" else "Projekte: Fertig vs. In Arbeit (Monate)",
                    categories = categories,
                    seriesLabels = listOf("Finished", "In Progress"),
                    seriesColors = listOf(Color(0xFF43A047), Color(0xFF8E24AA)),
                    valuesForCategory = { cat ->
                        val finishedCount = if (isOverall) {
                            (finishedByYear[cat]?.size ?: 0).toFloat()
                        } else {
                            (finishedByMonth[cat]?.size ?: 0).toFloat()
                        }
                        val inProgressCount = (inProgressCountByCat[cat] ?: 0).toFloat()
                        listOf(finishedCount, inProgressCount)
                    },
                    labelFormatter = { cat -> formatCategoryLabel7(cat, isOverall, settings.language) }
                )
                Spacer(Modifier.height(24.dp))
            }

            // --- Details (optional) ---
            items(categories) { cat ->
                val header = if (isOverall) {
                    cat
                } else {
                    val year = cat.take(4)
                    val month = cat.drop(5).take(2).toIntOrNull()
                    val monthName = if (month != null) getMonthName(month, settings.language) else cat
                    "$monthName $year"
                }
                Text(header, style = MaterialTheme.typography.headlineMedium)

                val yarnBoughtList = if (isOverall) (yarnsByYear[cat] ?: emptyList()) else (yarnsByMonth[cat] ?: emptyList())
                val yarnBoughtAmount = yarnBoughtList.sumOf { it.amount }
                val yarnBoughtMeterage = yarnBoughtList.sumOf { yarn ->
                    val meterage = yarn.meteragePerSkein
                    val weight = yarn.weightPerSkein
                    if (meterage != null && weight != null && weight > 0) (yarn.amount * meterage) / weight else 0
                }
                Text(stringResource(Res.string.statistics_yarn_bought, yarnBoughtAmount, yarnBoughtMeterage))

                val finishedList = if (isOverall) (finishedByYear[cat] ?: emptyList()) else (finishedByMonth[cat] ?: emptyList())
                val yarnUsedAmount = finishedList.sumOf { proj ->
                    usages.filter { it.projectId == proj.id }.sumOf { it.amount }
                }
                val yarnUsedMeterage = finishedList.sumOf { proj ->
                    usages.filter { it.projectId == proj.id }.sumOf { usage ->
                        val yarn = yarns.find { it.id == usage.yarnId }
                        if (yarn != null) {
                            val meterage = yarn.meteragePerSkein
                            val weight = yarn.weightPerSkein
                            if (meterage != null && weight != null && weight > 0) (usage.amount * meterage) / weight else 0
                        } else 0
                    }
                }
                Text(stringResource(Res.string.statistics_yarn_used, yarnUsedAmount, yarnUsedMeterage))

                val finishedCount = finishedList.size
                Text(stringResource(Res.string.statistics_projects_finished, finishedCount))

                val inProgressCount = inProgressCountByCat[cat] ?: 0
                Text(stringResource(Res.string.statistics_projects_in_progress, inProgressCount))

                Divider(Modifier.padding(vertical = 16.dp))
            }
        }
    }
}

/** Wiederverwendbarer Chart-Block für 2-Serien-Grouped-Barplots. */
@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
private fun ChartBlock(
    title: String,
    categories: List<String>,
    seriesLabels: List<String>,
    seriesColors: List<Color>,
    valuesForCategory: (String) -> List<Float>,
    labelFormatter: (String) -> String
) {
    if (categories.isEmpty()) return

    val barData: List<DefaultVerticalBarPlotGroupedPointEntry<String, Float>> =
        categories.map { cat ->
            val values = valuesForCategory(cat)
            val yPositions = values.map { v -> DefaultVerticalBarPosition(0f, v) }
            DefaultVerticalBarPlotGroupedPointEntry(x = cat, y = yPositions)
        }

    val seriesCount = barData.maxOf { it.y.size }
    require(seriesCount == seriesLabels.size && seriesCount == seriesColors.size) {
        "seriesLabels/colors must match data series count"
    }

    val yMax = barData.flatMap { it.y.map { pos -> pos.yMax } }.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        KoalaPlotTheme {
            // Legende
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FlowLegend(
                    itemCount = seriesCount,
                    symbol = { i: Int ->
                        Box(Modifier.size(10.dp).background(seriesColors[i]))
                    },
                    label = { i: Int ->
                        Text(seriesLabels[i])
                    }
                )
            }

            val xCats = barData.map { it.x }
            val xModel = CategoryAxisModel(xCats)
            val yModel = rememberFloatLinearAxisModel(range = 0f..yMax)

            val barRenderer: @Composable BarScope.(groupIndex: Int, series: Int, entry: DefaultVerticalBarPlotGroupedPointEntry<String, Float>) -> Unit =
                { _, series, _ -> DefaultVerticalBar(color = seriesColors[series]) }

            XYGraph(
                modifier = Modifier
                    .height(380.dp) // extra Höhe für vertikale Labels + mehr Spacing
                    .fillMaxWidth(),
                xAxisModel = xModel,
                yAxisModel = yModel,
                xAxisLabels = { cat: String ->
                    val txt = labelFormatter(cat).take(7)  // max 7 Zeichen
                    Text(
                        text = txt,
                        modifier = Modifier.rotate(-90f),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                },
                yAxisLabels = { y: Float ->
                    Text(y.toInt().toString())
                },
                xAxisTitle = {}, // Composable-Overload erzwingen
                yAxisTitle = {}
            ) {
                GroupedVerticalBarPlot(
                    data = barData,
                    bar = barRenderer,
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }
}

/** Kompakte Label-Formatierung (max 7 Zeichen) entsprechend Modus. */
private fun formatCategoryLabel7(category: String, overall: Boolean, language: String): String {
    return if (overall) {
        // "YYYY"
        category
    } else {
        // "YYYY-MM" -> "Jan 24" / "Jän 24"
        if (category.length >= 7 && category.getOrNull(4) == '-') {
            val year2 = category.substring(2, 4) // zwei Stellen
            val month = category.substring(5, 7).toIntOrNull()
            val mon = month?.let { getMonthShort(it, language) } ?: category
            "$mon $year2" // 6–7 Zeichen
        } else category
    }
}

private fun getMonthShort(month: Int, language: String): String {
    val de = listOf("Jän", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
    val en = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val list = if (language == "de") de else en
    return if (month in 1..12) list[month - 1] else ""
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
