
package org.example.project

import androidx.compose.runtime.*
import androidx.compose.material3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

// commonMain
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
// ggf. ohne Dispatchers, oder Default wie zuvor

@Composable
fun App(repo: JsonRepository) {

    MaterialTheme {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyLarge
        ) {
            var screen by remember { mutableStateOf<Screen>(Screen.Home) }
            var yarns by remember { mutableStateOf(emptyList<Yarn>()) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                val loaded =
                    repo.load()        // oder withContext(Dispatchers.Default) { repo.load() }
                yarns = loaded.yarns
            }

            when (val s = screen) {
                Screen.Home -> HomeScreen(
                    onOpenYarns = { screen = Screen.YarnList }
                )

                Screen.YarnList -> YarnListScreen(
                    yarns = yarns,
                    onAddClick = { screen = Screen.YarnForm(null) },
                    onOpen = { id -> screen = Screen.YarnForm(id) }
                )

                is Screen.YarnForm -> {
                    val existing = s.yarnId?.let { id -> yarns.firstOrNull { it.id == id } }
                    YarnFormScreen(
                        initial = existing,
                        onCancel = { screen = Screen.YarnList },
                        onDelete = { id ->
                            scope.launch {
                                val updated = repo.deleteYarn(id)
                                yarns = updated.yarns
                                screen = Screen.YarnList
                            }
                        },
                        onSave = { edited ->
                            val toSave = edited.copy(id = edited.id.takeIf { it > 0 }
                                ?: (yarns.maxOfOrNull { it.id } ?: 0) + 1)
                            scope.launch {
                                val updated = repo.addOrUpdateYarn(toSave)
                                yarns = updated.yarns
                                screen = Screen.YarnList
                            }
                        }
                    )
                }
            }
        }
    }
}



/*
package org.example.project

import androidx.compose.runtime.*
import androidx.compose.material3.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Composable
fun App(repo: JsonRepository) {
    // UI-State: Liste der Garne
    var yarns by remember { mutableStateOf<List<Yarn>>(emptyList()) }
    val scope = rememberCoroutineScope()

    // Beim ersten Rendern aus JSON laden
    LaunchedEffect(Unit) {
        // IO im Hintergrund, dann UI updaten
        val loaded = withContext(kotlinx.coroutines.Dispatchers.Default) { repo.load() }
        yarns = loaded.yarns
    }

    // UI zeichnen
    YarnListScreen(
        yarns = yarns,
        onAddClick = {
            // Minimal: Dummy-Eintrag hinzufügen (später Dialog einbauen)
            val new = Yarn(
                id = (yarns.maxOfOrNull { it.id } ?: 0) + 1,
                name = "Neue Wolle",
                color = "Blau",
                amount = 100
            )
            scope.launch {
                val updated = withContext(kotlinx.coroutines.Dispatchers.Default) { repo.addYarn(new) }
                yarns = updated.yarns
            }
        },
        onDelete = { id ->
            scope.launch {
                val updated = withContext(kotlinx.coroutines.Dispatchers.Default) { repo.deleteYarn(id) }
                yarns = updated.yarns
            }
        }
    )
}
*/

/*
package org.example.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import knittingappmultiplatt.composeapp.generated.resources.Res
import knittingappmultiplatt.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App(repo: JsonRepository) {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                    showContent = !showContent
                    println("new value: $showContent")
                 },
                modifier = Modifier.offset(y = (-100).dp)) {
                Text("Helena!")
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}
*/
