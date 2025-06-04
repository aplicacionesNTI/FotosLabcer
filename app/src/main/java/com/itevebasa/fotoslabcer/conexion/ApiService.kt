package com.itevebasa.fotoslabcer.conexion

import com.itevebasa.fotoslabcer.modelos.FotoRequest
import com.itevebasa.fotoslabcer.modelos.FotoResponse
import com.itevebasa.fotoslabcer.modelos.PaginarRequest
import com.itevebasa.fotoslabcer.modelos.PaginarResponse
import com.itevebasa.fotoslabcer.modelos.Usuario
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("api/login")
    fun login(@Query("login") username: String, @Query("password") password: String): Call<Usuario>
    @POST("api/expedientes/appMovil")
    fun paginarExpedientes(@Body request: PaginarRequest): Call<PaginarResponse>
    @Multipart
    @POST("api/documentos/sinFirmar")
    fun subirFoto(
            @Part file: MultipartBody.Part,
            @Part("codigoDocumento") codigoDocumento: RequestBody,
            @Part("nombre") nombre: RequestBody,
            @Part("item") item: RequestBody,
            @Part("user_id") userId: RequestBody,
            @Part("expediente_id") expedienteId: RequestBody,
            @Part("tipoDocumento_id") tipoDocumentoId: RequestBody
        ): Call<FotoResponse>
}