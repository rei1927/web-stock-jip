package com.example.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Body
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

interface PropertyApiService {

    @GET("api/units")
    suspend fun getUnitsByCluster(
        @Header("Authorization") authHeader: String? = null,
        @Query("cluster") clusterName: String? = null
    ): List<HousingUnitDto>

    @Headers("Accept: application/json")
    @POST("api/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") authHeader: String,
        @Body body: FcmTokenRequest
    )

    @Headers("Accept: application/json")
    @POST("api/login")
    suspend fun login(
        @Body body: LoginRequest
    ): LoginResponse

    @Headers("Accept: application/json")
    @POST("api/units/update-status")
    suspend fun updateUnitStatus(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateStatusRequest
    )

    @Headers("Accept: application/json")
    @POST("api/units/submit-sold")
    suspend fun submitSold(
        @Header("Authorization") authHeader: String,
        @Body body: SubmitSoldRequest
    )

    @retrofit2.http.Multipart
    @POST("api/units/upload-utj")
    suspend fun uploadUtjPhoto(
        @Header("Authorization") authHeader: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): UploadResponse

    @retrofit2.http.Multipart
    @POST("api/attendance/upload-photo")
    suspend fun uploadAttendancePhoto(
        @Header("Authorization") authHeader: String,
        @retrofit2.http.Part file: okhttp3.MultipartBody.Part
    ): UploadResponse

    @Headers("Accept: application/json")
    @POST("api/attendance")
    suspend fun submitAttendance(
        @Header("Authorization") authHeader: String,
        @Body body: AttendanceRequest
    ): AttendanceDto

    @Headers("Accept: application/json")
    @GET("api/attendance/status")
    suspend fun getAttendanceStatus(
        @Header("Authorization") authHeader: String
    ): AttendanceStatusResponse

    @Headers("Accept: application/json")
    @GET("api/attendance/all")
    suspend fun getAllAttendance(
        @Header("Authorization") authHeader: String
    ): List<AttendanceDto>

    @Headers("Accept: application/json")
    @POST("api/users/register")
    suspend fun registerUser(
        @Header("Authorization") authHeader: String,
        @Body body: RegisterUserRequest
    )

    @Headers("Accept: application/json")
    @POST("api/users/update")
    suspend fun updateUser(
        @Header("Authorization") authHeader: String,
        @Body body: UpdateUserRequest
    )

    @Headers("Accept: application/json")
    @POST("api/users/change-password")
    suspend fun changePassword(
        @Header("Authorization") authHeader: String,
        @Body body: ChangePasswordRequest
    )

    @Headers("Accept: application/json")
    @GET("api/users")
    suspend fun getUsers(
        @Header("Authorization") authHeader: String
    ): List<UserDto>

    data class FcmTokenRequest(val fcm_token: String)
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(
        val message: String,
        val access_token: String,
        val token_type: String,
        val user: UserDto
    )

    data class RegisterUserRequest(
        val name: String,
        val email: String,
        val password: String,
        val role: String
    )

    data class UpdateUserRequest(
        val email: String,
        val role: String
    )

    data class ChangePasswordRequest(
        val email: String,
        val password_lama: String,
        val password_baru: String
    )

    data class UpdateStatusRequest(
        val clusterName: String,
        val block: String,
        val status: String,
        val actionByUser: String?,
        val actionUserLabel: String?
    )

    data class UploadResponse(
        val status: String,
        val url: String
    )

    data class AttendanceStatusResponse(
        val status: String,
        val last_status: String
    )

    data class AttendanceRequest(
        val type: String, // "Masuk" or "Keluar"
        val lat: String? = null,
        val long: String? = null,
        val address: String? = null,
        val photo: String? = null,
        val photo_url: String? = null,
        val timestamp: String? = null // YYYY-MM-DD HH:mm:ss
    )

    data class AttendanceDto(
        val id: Int?,
        val username: String?,
        val name: String?,
        val type: String?,
        val location: String?,
        val address: String? = null,
        val photo_url: String?,
        val created_at: String?
    )

    data class SubmitSoldRequest(
        val unitId: Int,
        val namaLengkap: String,
        val alamatKtp: String,
        val alamatSurat: String,
        val noTelpRumah: String? = null,
        val noTelpSeluler: String,
        val noKtpConsumer: String,
        val noNpwp: String? = null,
        val noKk: String? = null,
        val typeBlok: String,
        val cluster: String,
        val tujuanPembelian: String,
        val sumberDana: String,
        val sistemPembayaran: String,
        val kprPersen: String? = null,
        val hargaJual: Double,
        val plafondKpr: Double? = null,
        val tandaJadi: Double,
        val tandaJadiDate: String,
        val uMuka: Double? = null,
        val uMukaBln: String? = null,
        val uMukaPertama: Double? = null,
        val uMukaPertamaDate: String? = null,
        val angsuranPertamaText: String? = null,
        val email: String? = null,
        val gimmicks: List<String>? = null,
        val utj_photo_url: String? = null
    )

    data class UserDto(
        val id: Int,
        val name: String,
        val email: String,
        val role: String? = null
    ) {
        fun toEntity(): User {
            val sanitizedRole = when {
                role?.contains("SUPER", ignoreCase = true) == true -> "Super Admin"
                role?.contains("MANAGER", ignoreCase = true) == true -> "Sales Manager"
                role?.contains("ADMIN", ignoreCase = true) == true -> "Admin"
                else -> "Sales"
            }
            return User(
                username = email,
                name = name,
                role = sanitizedRole,
                pin = "123456"
            )
        }
    }

    companion object {
        // PENTING: Jika menggunakan HP Fisik, ganti '10.0.2.2' dengan alamat IP Laptop Anda (contoh: 192.168.1.5)
        // Gunakan '10.0.2.2' jika hanya menggunakan Emulator.
        private const val BASE_URL = "https://webstock.joyvite.id/"

        fun create(): PropertyApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        // Menghapus Origin/Referer jika ada (Stateful prevention)
                        .removeHeader("Origin")
                        .removeHeader("Referer")
                        .build()
                    chain.proceed(request)
                }
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PropertyApiService::class.java)
        }
    }
}
