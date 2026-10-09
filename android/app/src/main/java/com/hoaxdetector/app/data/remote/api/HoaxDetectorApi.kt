package com.hoaxdetector.app.data.remote.api

import com.hoaxdetector.app.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface HoaxDetectorApi {

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("api/factcheck/text")
    suspend fun checkText(@Body request: TextCheckRequest): Response<FactCheckResponse>

    @POST("api/factcheck/url")
    suspend fun checkUrl(@Body request: UrlCheckRequest): Response<FactCheckResponse>
}
