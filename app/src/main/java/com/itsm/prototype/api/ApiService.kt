package com.itsm.prototype.api

import com.itsm.prototype.data.LoginRequest
import com.itsm.prototype.data.LoginResponse
import com.itsm.prototype.model.CatalogModelItem
import com.itsm.prototype.model.DeleteModelResponse
import com.itsm.prototype.model.GetModelResponse
import com.itsm.prototype.model.PurchaseRequest
import com.itsm.prototype.model.PurchaseResponse
import com.itsm.prototype.model.UpdateModelRequest
import com.itsm.prototype.model.UpdateModelResponse
import com.itsm.prototype.ui.client.ClientProfileResponse
import com.itsm.prototype.ui.login.RegisterRequest
import com.itsm.prototype.ui.login.RegisterResponse
import com.itsm.prototype.ui.seller.ProcessImageResponse
import com.itsm.prototype.ui.seller.ProcessingStatusResponse
import com.itsm.prototype.ui.seller.SellerProfileResponse
import com.itsm.prototype.ui.seller.creation.AvailableModelsResponse
import com.itsm.prototype.ui.seller.receiving.UserModelDto
import com.itsm.prototype.ui.seller.receiving.UserModelsResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @GET("/models")
    suspend fun getModels(): Response<List<CatalogModelItem>>

    @Multipart
    @POST("api/model/process-image")
    suspend fun processImage(
        @Part image: MultipartBody.Part,
        @Part("user_id") userId: okhttp3.RequestBody
    ): Response<ProcessImageResponse>

    @GET("api/model/status/{taskId}")
    suspend fun getProcessingStatus(
        @Path("taskId") taskId: String
    ): Response<ProcessingStatusResponse>

    @GET("seller/profile/{userId}")
    suspend fun getSellerProfile(
        @Path("userId") userId: String
    ): Response<SellerProfileResponse>

    @GET("client/profile/{userId}")
    suspend fun getClientProfile(
        @Path("userId") userId: String
    ): Response<ClientProfileResponse>

    @GET("api/model/user/{userId}/models")
    suspend fun getUserModels(
        @Path("userId") userId: String
    ): Response<UserModelsResponse>

    @GET("api/model/available-models")
    suspend fun getAvailableModels(): Response<AvailableModelsResponse>

    @GET("api/model/{modelId}")
    suspend fun getModelById(
        @Path("modelId") modelId: String
    ): Response<GetModelResponse>

    // Eliminar modelo
    @DELETE("api/model/{modelId}")
    suspend fun deleteModel(
        @Path("modelId") modelId: String
    ): Response<DeleteModelResponse>

    @Streaming
    @GET
    suspend fun downloadModel(
        @Url fileUrl: String
    ): Response<ResponseBody>

    // Comprar modelo
    @POST("api/purchase/model")
    suspend fun purchaseModel(
        @Body request: PurchaseRequest
    ): Response<PurchaseResponse>

    @PUT("api/model/{modelId}")
    suspend fun updateModel(
        @Path("modelId") modelId: String,
        @Body updateData: UpdateModelRequest
    ): Response<UpdateModelResponse>
}