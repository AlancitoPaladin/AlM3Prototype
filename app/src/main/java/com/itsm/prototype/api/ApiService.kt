package com.itsm.prototype.api

import com.itsm.prototype.data.LoginRequest
import com.itsm.prototype.data.LoginResponse
import com.itsm.prototype.model.ModelResponse
import com.itsm.prototype.ui.login.RegisterRequest
import com.itsm.prototype.ui.login.RegisterResponse
import com.itsm.prototype.ui.seller.ProcessImageResponse
import com.itsm.prototype.ui.seller.creation.AvailableModelsResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("/models")
    suspend fun getModels(): Response<List<ModelResponse>>

    @Multipart
    @POST("api/model/process-image")
    suspend fun processImage(
        @Part image: MultipartBody.Part
    ): Response<ProcessImageResponse>

    @GET("api/model/available-models")
    suspend fun getAvailableModels(): Response<AvailableModelsResponse>
}