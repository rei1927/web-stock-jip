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

interface PropertyApiService {

    @GET("api/units")
    suspend fun getUnitsByCluster(
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

    data class FcmTokenRequest(val fcm_token: String)
    data class LoginRequest(val email: String, val password: String)
    data class LoginResponse(
        val message: String,
        val access_token: String,
        val token_type: String,
        val user: UserDto
    )

    data class UpdateStatusRequest(
        val clusterName: String,
        val block: String,
        val status: String,
        val actionByUser: String?,
        val actionUserLabel: String?
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
        val gimmicks: List<String>? = null
    )

    data class UserDto(
        val id: Int,
        val name: String,
        val email: String,
        val role: String? = null
    )

    companion object {
        // PENTING: Jika menggunakan HP Fisik, ganti '10.0.2.2' dengan alamat IP Laptop Anda (contoh: 192.168.1.5)
        // Gunakan '10.0.2.2' jika hanya menggunakan Emulator.
        private const val BASE_URL = "http://10.0.2.2:8000/"

        fun create(): PropertyApiService {
            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(PropertyApiService::class.java)
        }
    }
}
