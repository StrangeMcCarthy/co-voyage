package com.covoyage.data.remote.api

import com.covoyage.data.remote.ApiConfig
import com.covoyage.data.remote.ApiResult
import com.covoyage.data.remote.safeApiCall
import com.covoyage.domain.model.CreateRideRequest
import com.covoyage.domain.model.Ride
import com.covoyage.domain.model.RideFilter
import com.covoyage.domain.model.RideSearchResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class RideApiService(private val httpClient: HttpClient) {
    
    suspend fun createRide(request: CreateRideRequest): ApiResult<Ride> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.CREATE_RIDE) {
            setBody(request)
        }.body()
    }
    
    suspend fun getRide(rideId: String): ApiResult<Ride> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.RIDE_DETAILS.replace("{id}", rideId)).body()
    }
    
    suspend fun searchRides(
        filter: RideFilter,
        page: Int = 1,
        pageSize: Int = 20
    ): ApiResult<RideSearchResponse> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.SEARCH_RIDES) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            filter.departingTown?.let { parameter("departingTown", it) }
            filter.destination?.let { parameter("destination", it) }
            filter.date?.let { parameter("date", it) }
            filter.minSeats?.let { parameter("minSeats", it) }
            filter.maxPrice?.let { parameter("maxPrice", it) }
            filter.minRating?.let { parameter("minRating", it) }
        }.body()
    }
    
    suspend fun getMyRides(): ApiResult<List<Ride>> = safeApiCall {
        httpClient.get(ApiConfig.Endpoints.MY_RIDES).body()
    }
    
    suspend fun updateRide(rideId: String, request: CreateRideRequest): ApiResult<Ride> = safeApiCall {
        httpClient.put(ApiConfig.Endpoints.UPDATE_RIDE.replace("{id}", rideId)) {
            setBody(request)
        }.body()
    }
    
    suspend fun cancelRide(rideId: String, reason: String): ApiResult<Ride> = safeApiCall {
        httpClient.post(ApiConfig.Endpoints.CANCEL_RIDE.replace("{id}", rideId)) {
            setBody(mapOf("reason" to reason))
        }.body()
    }
    
    suspend fun getNearbyRides(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 50.0
    ): ApiResult<List<Ride>> = safeApiCall {
        httpClient.get("${ApiConfig.Endpoints.RIDES}/nearby") {
            parameter("latitude", latitude)
            parameter("longitude", longitude)
            parameter("radius", radiusKm)
        }.body()
    }
}
