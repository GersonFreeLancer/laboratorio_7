package com.gerson.demodata.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.pm.PackageManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.gerson.demodata.DemoDataApp
import com.example.demodata.R
import com.gerson.demodata.data.local.entity.GpsGoogleEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.location.LocationManager
import android.location.LocationListener
import androidx.annotation.RequiresPermission
import com.gerson.demodata.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GpsCaptureService : Service() {

    companion object {

        private const val CHANNEL_ID = "gps_capture_channel"
        private const val NOTIFICATION_ID = 1001

        private const val INTERVAL_MS = 10_000L

        private const val SENSOR_TIMEOUT_MS = 5_000L
    }

    private val serviceScope =
        CoroutineScope(
            Dispatchers.IO + SupervisorJob()
        )

    private var captureJob: Job? = null

    private lateinit var locationManager: LocationManager
    private lateinit var fusedClient:
            FusedLocationProviderClient

    private val gpsRepo by lazy {
        (application as DemoDataApp).gpsRepository
    }

    override fun onCreate() {
        super.onCreate()

        fusedClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationManager =
            getSystemService(LocationManager::class.java)

        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (!hasLocationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )

        if (captureJob == null) {

            captureJob = serviceScope.launch {

                while (isActive) {

                    performCaptures()

                    delay(INTERVAL_MS)
                }
            }
        }

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private suspend fun performCaptures() {

        val timestamp =
            System.currentTimeMillis()

        captureGoogleLocation(
            timestamp
        )

        captureSensorLocation(
            timestamp
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun captureGoogleLocation(
        timestamp: Long
    ) {

        try {

            val location =
                fusedClient
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    .await()

            location?.let {

                gpsRepo.saveGooglePoint(
                    GpsGoogleEntity(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        speed =
                            if (it.hasSpeed())
                                it.speed
                            else
                                null,
                        bearing =
                            if (it.hasBearing())
                                it.bearing
                            else
                                null,
                        timestamp = timestamp
                    )
                )
            }

        } catch (_: Exception) {
        }
    }

    private fun hasLocationPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildNotification(): Notification {

        return NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Captura GPS activa")
            .setContentText(
                "Registrando coordenadas"
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                "GPS Capture",
                NotificationManager.IMPORTANCE_LOW
            )

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()

        captureJob?.cancel()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun captureSensorLocation(
        timestamp: Long
    ) {

        try {

            val sensorLocation =
                withTimeoutOrNull(
                    SENSOR_TIMEOUT_MS
                ) {
                    getRawGpsLocation()
                }

            gpsRepo.saveSensorsPoint(
                GpsSensorsEntity(
                    latitude =
                        sensorLocation?.latitude,

                    longitude =
                        sensorLocation?.longitude,

                    provider =
                        LocationManager.GPS_PROVIDER,

                    altitude =
                        if (sensorLocation?.hasAltitude() == true)
                            sensorLocation.altitude
                        else
                            null,

                    satellites = null,

                    timestamp = timestamp
                )
            )

        } catch (_: Exception) {
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun getRawGpsLocation(): Location? =
        suspendCancellableCoroutine { continuation ->

            val listener = object : LocationListener {

                override fun onLocationChanged(location: Location) {

                    locationManager.removeUpdates(this)

                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener
            )

            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
            }
        }
}