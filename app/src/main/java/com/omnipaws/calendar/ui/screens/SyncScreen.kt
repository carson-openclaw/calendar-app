package com.omnipaws.calendar.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.data.GoogleConfig
import com.omnipaws.calendar.data.SyncResult
import com.omnipaws.calendar.data.buildCalendarServiceFromAuthCode
import com.omnipaws.calendar.data.buildSignInIntent
import com.omnipaws.calendar.data.googleSignInClient
import com.omnipaws.calendar.data.syncAllEvents
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.AccentDim
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Paper
import com.omnipaws.calendar.ui.theme.SoftDivider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SyncScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var accountEmail by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<SyncResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleEvents = remember(EventRepository.events.size) {
        EventRepository.events.count { it.source == "google" }
    }
    val localEvents = remember(EventRepository.events.size) {
        EventRepository.events.count { it.source == "local" }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                accountEmail = account.email
                val authCode = account.serverAuthCode
                if (authCode != null) {
                    errorMessage = null
                } else {
                    errorMessage = "No auth code received. Please try again."
                }
            } catch (e: ApiException) {
                errorMessage = when (e.statusCode) {
                    12501 -> "Sign-in was cancelled."
                    12500 -> "A sign-in error occurred. Please try again."
                    else -> "Google sign-in failed (error ${e.statusCode})."
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Calendar Sync",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (accountEmail != null) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    contentDescription = null,
                    tint = if (accountEmail != null) Accent else Muted,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "Google Calendar",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (accountEmail != null) {
                        Text(
                            text = accountEmail.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SoftDivider)
            ) {}

            Spacer(modifier = Modifier.height(20.dp))

            if (!GoogleConfig.isConfigured) {
                Text(
                    text = "Google sign-in needs an OAuth client ID (add it in Settings) \u2014 not configured.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted
                )
            } else if (accountEmail == null) {
                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        val intent = buildSignInIntent(context)
                        if (intent != null) {
                            signInLauncher.launch(intent)
                        } else {
                            errorMessage = "Could not start Google sign-in."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Accent
                    )
                ) {
                    Text(
                        text = "Connect Google Calendar",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            isSyncing = true
                            syncResult = null
                            errorMessage = null
                            try {
                                val account = GoogleSignIn.getLastSignedInAccount(context)
                                val authCode = account?.serverAuthCode
                                if (authCode == null) {
                                    errorMessage = "No auth code. Please reconnect."
                                    isSyncing = false
                                    return@launch
                                }
                                val service = withContext(Dispatchers.IO) {
                                    buildCalendarServiceFromAuthCode(authCode)
                                }
                                val result = withContext(Dispatchers.IO) {
                                    syncAllEvents(EventRepository, service)
                                }
                                syncResult = result
                            } catch (e: Exception) {
                                errorMessage = "Sync failed: ${e.message ?: "Unknown error"}"
                            } finally {
                                isSyncing = false
                            }
                        }
                    },
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Accent,
                        contentColor = Paper,
                        disabledContainerColor = AccentDim,
                        disabledContentColor = Paper.copy(alpha = 0.7f)
                    )
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            color = Paper,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Syncing\u2026",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Sync now",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        googleSignInClient(context).signOut().addOnCompleteListener {
                            accountEmail = null
                            syncResult = null
                            errorMessage = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Muted
                    )
                ) {
                    Text(
                        text = "Disconnect",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = syncResult != null || errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 20.dp)) {
                syncResult?.let { result ->
                    val message = when {
                        result.countSynced > 0 -> "Synced ${result.countSynced} events"
                        result.countUpdated > 0 -> "Updated ${result.countUpdated} events"
                        else -> "No changes"
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Accent
                    )
                }
                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SourceCount(
                label = "Local events",
                count = localEvents,
                modifier = Modifier.weight(1f)
            )
            SourceCount(
                label = "Google events",
                count = googleEvents,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SourceCount(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )
    }
}
