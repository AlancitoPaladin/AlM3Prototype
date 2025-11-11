package com.itsm.prototype.api

import com.itsm.prototype.data.LoginRequest
import com.itsm.prototype.data.LoginResponse
import com.itsm.prototype.ui.upload.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part photo: MultipartBody.Part
    ): Response<UploadResponse>
}