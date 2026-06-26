package com.bghitech.momenta.data.remote

import com.bghitech.momenta.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MomentaApi {
    @GET("/health")
    suspend fun health(): HealthDto

    @GET("/ready")
    suspend fun ready(): ReadyDto

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponseDto

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponseDto

    @POST("/api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): AuthResponseDto

    @POST("/api/v1/auth/logout")
    suspend fun logout(): ApiMessageDto

    @GET("/api/v1/me")
    suspend fun getMe(): UserDto

    @GET("/api/v1/challenges/today")
    suspend fun getTodayChallenge(): ChallengeDto

    @GET("/api/v1/feed/today")
    suspend fun getTodayFeed(
        @Query("cursor") cursor: String?,
        @Query("limit") limit: Int = 20
    ): FeedResponseDto

    @Multipart
    @POST("/api/v1/posts")
    suspend fun uploadPost(
        @Part("challenge_id") challengeId: RequestBody,
        @Part("caption") caption: RequestBody?,
        @Part("country") country: RequestBody?,
        @Part("city") city: RequestBody?,
        @Part media: MultipartBody.Part
    ): CreatePostResponseDto

    @POST("/api/v1/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): ApiMessageDto

    @DELETE("/api/v1/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): ApiMessageDto

    @POST("/api/v1/posts/{id}/report")
    suspend fun reportPost(
        @Path("id") postId: String,
        @Body request: ReportRequestDto
    ): ApiMessageDto

    @GET("/api/v1/me/profile")
    suspend fun getMyProfile(): ProfileDto

    @PATCH("/api/v1/me/profile")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequestDto): ProfileDto
}
