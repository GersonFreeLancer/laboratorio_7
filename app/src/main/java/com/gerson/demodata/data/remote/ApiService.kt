package com.gerson.demodata.data.remote

import com.gerson.demodata.data.remote.model.GoogleLoginRequest
import com.gerson.demodata.data.remote.model.LoginRequest
import com.gerson.demodata.data.remote.model.RefreshTokenRequest
import com.gerson.demodata.data.remote.model.RegisterRequest
import com.gerson.demodata.data.remote.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
interface ApiService {
    @POST("{projectSlug}/auth/register")
    suspend fun register(
        @Path("projectSlug") projectSlug: String,
        @Body request: RegisterRequest
    ): Response<Unit>

    @POST("{projectSlug}/auth/login")
    suspend fun login(
        @Path("projectSlug") projectSlug: String,
        @Body request: LoginRequest
    ): Response<TokenResponse>

    @POST("{projectSlug}/auth/google")
    suspend fun loginWithGoogle(
        @Path("projectSlug") projectSlug: String,
        @Body request: GoogleLoginRequest
    ): Response<TokenResponse>

    @POST("{projectSlug}/auth/refresh-token")
    suspend fun refreshToken(
        @Path("projectSlug") projectSlug: String,
        @Body request: RefreshTokenRequest
    ): Response<TokenResponse>
    
}