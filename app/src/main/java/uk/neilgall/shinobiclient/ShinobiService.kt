package uk.neilgall.shinobiclient

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val mail: String,
    val pass: String,
    val function: String = "dash"
)

data class User(
    val ok: Boolean,
    val auth_token: String,
    val ke: String,
    val uid: String
)

data class Login(
    @SerializedName("\$user") val user: User
)

interface ShinobiService {
    @POST("?json=true")
    fun login(@Body request: LoginRequest): Call<Login>
}

fun createShinobiService(server: String, tls: Boolean): ShinobiService {
    val scheme = if (tls) "https" else "http"
    val retrofit = Retrofit.Builder()
        .baseUrl("$scheme://$server")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ShinobiService::class.java)
}
