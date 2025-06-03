package com.itevebasa.fotoslabcer.conexion

import com.itevebasa.fotoslabcer.modelos.FotoRequest
import com.itevebasa.fotoslabcer.modelos.FotoResponse
import com.itevebasa.fotoslabcer.modelos.PaginarRequest
import com.itevebasa.fotoslabcer.modelos.PaginarResponse
import com.itevebasa.fotoslabcer.modelos.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    fun login(@Query("login") username: String, @Query("password") password: String): Call<Usuario>
    @POST("api/expedientes/paginar")
    fun paginarExpedientes(@Body request: PaginarRequest): Call<PaginarResponse>
    @POST("api/documentos/sinFirmar")
    fun subirFoto(@Body request: FotoRequest): Call<FotoResponse>
}