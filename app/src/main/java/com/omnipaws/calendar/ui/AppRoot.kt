package com.omnipaws.calendar.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.components.AddEventDialog
import com.omnipaws.calendar.ui.navigation.AppNav


@Composable
fun AppRoot() {
    val navController = rememberNavController()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add event"
                )
            }
        }
    ) { innerPadding ->
        AppNav(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (showAddDialog) {
        AddEventDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { event ->
                EventRepository.add(event)
                showAddDialog = false
            }
        )
    }
}
