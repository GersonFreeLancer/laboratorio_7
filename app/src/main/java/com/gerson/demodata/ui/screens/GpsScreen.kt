package com.gerson.demodata.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gerson.demodata.DemoDataApp
import com.gerson.demodata.services.GpsCaptureService
import com.gerson.demodata.ui.viewmodel.ComparativeGpsRecord
import com.gerson.demodata.ui.viewmodel.GpsViewModel
import com.gerson.demodata.ui.viewmodel.GpsViewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GpsScreen() {

    val context = LocalContext.current

    val repository =
        (context.applicationContext as DemoDataApp)
            .gpsRepository

    val viewModel: GpsViewModel = viewModel(
        factory = GpsViewModelFactory(repository)
    )

    val googlePoints by
    viewModel.googlePoints.collectAsStateWithLifecycle()

    val sensorsPoints by
    viewModel.sensorsPoints.collectAsStateWithLifecycle()

    val history by
    viewModel.comparativeHistory.collectAsStateWithLifecycle()

    var capturing by remember {
        mutableStateOf(false)
    }

    val permissions = buildList {

        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionsState =
        rememberMultiplePermissionsState(
            permissions = permissions
        )

    if (!permissionsState.allPermissionsGranted) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "La aplicación necesita permisos de ubicación."
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Button(
                onClick = {
                    permissionsState.launchMultiplePermissionRequest()
                }
            ) {
                Text("Conceder permisos")
            }
        }

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(),
            onClick = {

                if (!capturing) {

                    context.startForegroundService(
                        Intent(
                            context,
                            GpsCaptureService::class.java
                        )
                    )

                } else {

                    context.stopService(
                        Intent(
                            context,
                            GpsCaptureService::class.java
                        )
                    )
                }

                capturing = !capturing
            }
        ) {

            Icon(
                imageVector =
                    if (capturing)
                        Icons.Default.Stop
                    else
                        Icons.Default.PlayArrow,
                contentDescription = null
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                if (capturing)
                    "Detener captura"
                else
                    "Iniciar captura"
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors()
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "Google FLP",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${googlePoints.size} registros"
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors()
            ) {

                Column(
                    modifier = Modifier.padding(12.dp)
                ) {

                    Text(
                        text = "GNSS Sensor",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "${sensorsPoints.size} registros"
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(
            text = "Historial Comparativo",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LazyColumn {

            items(
                items = history,
                key = { it.timestamp }
            ) { record ->

                ComparativeCaptureCard(
                    record = record
                )
            }
        }
    }
}

@Composable
private fun ComparativeCaptureCard(
    record: ComparativeGpsRecord
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = "Timestamp: ${record.timestamp}",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "GOOGLE FLP",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Latitud: ${record.google?.latitude ?: "SIN SEÑAL"}"
            )

            Text(
                text = "Longitud: ${record.google?.longitude ?: "SIN SEÑAL"}"
            )

            Text(
                text = "Precisión: ${record.google?.accuracy ?: "--"}"
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "GNSS SENSOR",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Latitud: ${record.sensors?.latitude ?: "SIN SEÑAL"}"
            )

            Text(
                text = "Longitud: ${record.sensors?.longitude ?: "SIN SEÑAL"}"
            )

            Text(
                text = "Proveedor: ${record.sensors?.provider ?: "--"}"
            )

            Text(
                text = "Altitud: ${record.sensors?.altitude ?: "--"}"
            )
        }
    }
}