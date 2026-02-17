package com.autologin.app.ui.login

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autologin.app.BuildConfig
import com.autologin.app.domain.model.AuthState
import com.autologin.app.domain.model.DetectedApp
import com.autologin.app.domain.model.SsoType

@Composable
fun LoginScreen(viewModel: AuthViewModel = hiltViewModel()) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val detectedApps by viewModel.detectedApps.collectAsStateWithLifecycle()
    val brokerInstalled by viewModel.brokerInstalled.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity
    val context = LocalContext.current

    when {
        !brokerInstalled -> NoBrokerContent()
        authState is AuthState.Loading -> LoadingContent()
        authState is AuthState.Authenticated -> AuthenticatedContent(
            state = authState as AuthState.Authenticated,
            isSharedDevice = viewModel.isSharedDevice,
            detectedApps = detectedApps,
            onSignOut = { viewModel.signOut() },
            onOpenApp = { app ->
                viewModel.getLaunchIntent(app.packageName)?.let { intent ->
                    context.startActivity(intent)
                }
            },
        )
        authState is AuthState.Error -> ErrorContent(
            state = authState as AuthState.Error,
            onRetry = { viewModel.signIn(activity) },
        )
        else -> UnauthenticatedContent(
            detectedApps = detectedApps,
            onSignIn = { viewModel.signIn(activity) },
        )
    }
}

@Composable
private fun UnauthenticatedContent(
    detectedApps: List<DetectedApp>,
    onSignIn: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "AutoLogin",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Inicia sesion para acceder a todas tus apps Microsoft",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Iniciar Sesion con Microsoft")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Apps Microsoft detectadas",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        items(detectedApps) { app ->
            AppRow(app = app, ssoActive = false)
        }
    }
}

@Composable
private fun AuthenticatedContent(
    state: AuthState.Authenticated,
    isSharedDevice: Boolean,
    detectedApps: List<DetectedApp>,
    onSignOut: () -> Unit,
    onOpenApp: (DetectedApp) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.account.name.ifEmpty { state.account.email },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = state.account.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSharedDevice) "Sesion activa (Dispositivo compartido)" else "Sesion activa",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Apps disponibles",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Top,
        ) {
            val installedApps = detectedApps.filter { it.isInstalled }
            val fullSso = installedApps.filter { it.ssoType == SsoType.FULL }
            val partialSso = installedApps.filter { it.ssoType == SsoType.PARTIAL }

            // Izquierda: requieren identificacion
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Requiere identificacion",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(bottom = 2.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Escribe tu correo (sin password)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                ) {
                    partialSso.forEach { app ->
                        AppRow(app = app, ssoActive = true, onOpen = { onOpenApp(app) })
                    }
                }
            }

            // Derecha: acceso automatico
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Acceso automatico",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 2.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "No requiere accion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center,
                )
                Column(
                    modifier = Modifier.width(IntrinsicSize.Max),
                ) {
                    fullSso.forEach { app ->
                        AppRow(app = app, ssoActive = true, onOpen = { onOpenApp(app) })
                    }
                }
            }
        }

        Text(
            text = "Cerrar sesion revocara el acceso en todas las apps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Cerrar Sesion")
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppFooter()
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Autenticando...",
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ErrorContent(state: AuthState.Error, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun NoBrokerContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Se requiere Microsoft Authenticator o Company Portal instalado",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppRow(app: DetectedApp, ssoActive: Boolean, onOpen: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (!app.isInstalled) Icons.Default.Close
                else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = when {
                    !app.isInstalled -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    ssoActive && app.ssoType == SsoType.FULL -> MaterialTheme.colorScheme.secondary
                    ssoActive -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        when {
            !app.isInstalled -> Text(
                text = "No instalada",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            app.isInstalled && onOpen != null -> OutlinedButton(
                onClick = onOpen,
            ) {
                Text(
                    text = if (app.ssoType == SsoType.FULL) "Abrir" else "Identificate",
                )
            }
        }
    }
}

@Composable
private fun AppFooter() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(
            onClick = {
                val log = try {
                    val process = Runtime.getRuntime().exec(
                        arrayOf("logcat", "-d", "-t", "1000", "--pid=${android.os.Process.myPid()}")
                    )
                    process.inputStream.bufferedReader().readText()
                } catch (e: Exception) {
                    "Error al recopilar logs: ${e.message}"
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "AutoLogin v${BuildConfig.VERSION_NAME} - Log de errores")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "AutoLogin v${BuildConfig.VERSION_NAME} (build ${BuildConfig.BUILD_NUMBER}, ${BuildConfig.GIT_HASH})\n\n$log"
                    )
                }
                context.startActivity(Intent.createChooser(intent, "Enviar log a IT"))
            },
        ) {
            Text(
                text = "Enviar log de errores a IT",
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Text(
            text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_HASH})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Departamento de IT - Prestige-Expo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}
