package com.gerson.demodata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gerson.demodata.data.repository.GpsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

class GpsViewModel(
    private val gpsRepository: GpsRepository
) : ViewModel() {

    val googlePoints = gpsRepository.googlePoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val sensorsPoints = gpsRepository.sensorsPoints.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val comparativeHistory = combine(
        gpsRepository.googlePoints,
        gpsRepository.sensorsPoints
    ) { gList, sList ->

        val allTimestamps =
            (gList.map { it.timestamp } + sList.map { it.timestamp })
                .distinct()
                .sortedDescending()

        allTimestamps.map { timestamp ->

            ComparativeGpsRecord(
                timestamp = timestamp,

                google = gList.find {
                    it.timestamp == timestamp
                },

                sensors = sList.find {
                    it.timestamp == timestamp
                }
            )
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}