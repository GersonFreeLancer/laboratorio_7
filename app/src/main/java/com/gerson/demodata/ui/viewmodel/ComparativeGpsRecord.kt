package com.gerson.demodata.ui.viewmodel


import com.gerson.demodata.data.local.entity.GpsGoogleEntity
import com.gerson.demodata.data.local.entity.GpsSensorsEntity

data class ComparativeGpsRecord(
    val timestamp: Long,
    val google: GpsGoogleEntity?,
    val sensors: GpsSensorsEntity?
)
