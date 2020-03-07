package com.kadabra.agent.firebase

import com.google.android.gms.location.LocationResult

interface ILocationListener {
    fun locationResponse(locationResult: LocationResult)
}