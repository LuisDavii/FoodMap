package com.example.foodmap.network

import com.example.foodmap.models.RefeicaoRequest
import com.example.foodmap.models.RefeicaoResponse
import com.example.foodmap.models.StatusRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/usuarios/")
    fun cadastrarUsuario(@Body usuario: Usuario): Call<ApiResponse>

    @POST("api/login/")
    fun loginUsuario(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("api/refeicoes/")
    fun salvarRefeicao(@Body refeicao: RefeicaoRequest): Call<ApiResponse>

    @GET("api/refeicoes/")
    fun getRefeicoes(@Query("usuario_id") userId: Int): Call<List<RefeicaoResponse>>

    @PATCH("api/refeicoes/{id}/")
    fun atualizarStatus(
        @Path("id") id: Long,
        @Body status: StatusRequest
    ): Call<ApiResponse>
}


data class LoginRequest(
    val username: String,
    val password: String
)


data class LoginResponse(
    val message: String,
    val usuario: Usuario? = null
)


data class UsuarioResponse(
    val id: Int,
    val username: String,
    val name: String,
    val email: String
)


data class ApiResponse(
    val message: String,
    val id: Int? = null,
    val username: String? = null
)