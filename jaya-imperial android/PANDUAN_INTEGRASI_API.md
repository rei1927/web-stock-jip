# PANDUAN INTEGRASI API: MENGHUBUNGKAN APLIKASI DENGAN WEB APP
*Panduan lengkap untuk mengintegrasikan pengambilan (fetch) dan pembaruan data Unit Perumahan berbasis Klaster dari Web App backend ke aplikasi Jaya Imperial Park.*

Aplikasi saat ini telah siap mendukung koneksi internet (Izin `INTERNET` dan `usesCleartextTraffic` sudah ditambahkan di `AndroidManifest.xml`). Proyek ini juga sudah dilengkapi dependensi **Retrofit**, **OkHttp**, dan **Moshi** (JSON Parser KSP) di `build.gradle.kts`.

---

## 1. STRUKTUR PAYLOAD JSON (DARI WEB APP)

Untuk mengirimkan detail unit berdasarkan klaster, pastikan API dari Web App Anda mengembalikan data berformat JSON Array. Format ini harus selaras dengan entitas lokal `HousingUnit` di dalam aplikasi Android:

### Endpoint Contoh: `GET /api/units?cluster=Imperial+Garden`

```json
[
  {
    "clusterName": "Imperial Garden",
    "block": "A1/08",
    "typeName": "Tipe 70/120",
    "price": 750000000.0,
    "isSold": false,
    "buildingArea": 70,
    "landArea": 120,
    "bedrooms": 3,
    "bathrooms": 2,
    "notes": "Lokasi strategis dekat taman utama",
    "status": "Tersedia",
    "actionByUser": null,
    "actionUserLabel": null,
    "holdTimestamp": null
  },
  {
    "clusterName": "Imperial Garden",
    "block": "B3/12",
    "typeName": "Tipe 36/60",
    "price": 420000000.0,
    "isSold": true,
    "buildingArea": 36,
    "landArea": 60,
    "bedrooms": 2,
    "bathrooms": 1,
    "notes": "Dekat gerbang masuk klaster",
    "status": "Terjual",
    "actionByUser": "siska",
    "actionUserLabel": "Siska Sales",
    "holdTimestamp": null
  }
]
```

---

## 2. LANGKAH-LANGKAH IMPLEMENTASI DI ANDROID

Ikuti 4 langkah sederhana berikut untuk menghubungkan data:

### Langkah 1: Buat DTO (Data Transfer Object)

Buat file baru bernama `HousingUnitDto.kt` di package `com.example.data.api` (atau langsung di package `com.example.data`):

```kotlin
package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HousingUnitDto(
    @Json(name = "clusterName") val clusterName: String,
    @Json(name = "block") val block: String,
    @Json(name = "typeName") val typeName: String,
    @Json(name = "price") val price: Double,
    @Json(name = "isSold") val isSold: Boolean,
    @Json(name = "buildingArea") val buildingArea: Int,
    @Json(name = "landArea") val landArea: Int,
    @Json(name = "bedrooms") val bedrooms: Int,
    @Json(name = "bathrooms") val bathrooms: Int,
    @Json(name = "notes") val notes: String?,
    @Json(name = "status") val status: String,
    @Json(name = "actionByUser") val actionByUser: String?,
    @Json(name = "actionUserLabel") val actionUserLabel: String?,
    @Json(name = "holdTimestamp") val holdTimestamp: Long?
) {
    // Fungsi untuk mengubah DTO menjadi Entitas database Room lokal
    fun toEntity(): HousingUnit {
        return HousingUnit(
            clusterName = this.clusterName,
            block = this.block,
            typeName = this.typeName,
            price = this.price,
            isSold = this.isSold,
            buildingArea = this.buildingArea,
            landArea = this.landArea,
            bedrooms = this.bedrooms,
            bathrooms = this.bathrooms,
            notes = this.notes ?: "",
            status = this.status,
            actionByUser = this.actionByUser,
            actionUserLabel = this.actionUserLabel,
            holdTimestamp = this.holdTimestamp
        )
    }
}
```

---

### Langkah 2: Buat Retrofit Api Service

Buat file `PropertyApiService.kt` untuk mendefinisikan interface pemanggilan HTTP:

```kotlin
package com.example.data

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface PropertyApiService {
    
    // Mengambil data berdasarkan klaster opsional
    @GET("api/units")
    suspend fun getUnitsByCluster(
        @Query("cluster") clusterName: String? = null
    ): List<HousingUnitDto>

    companion object {
        // Ganti BASE_URL dengan domain Web App Anda
        // Gunakan "http://10.0.2.2:8000/" jika mengetes dengan localhost Laravel/Node.js dari emulator Android
        private const val BASE_URL = "https://domain-web-app-anda.com/"

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
```

---

### Langkah 3: Tambahkan Fungsi Sinkronisasi di `PropertyRepository`

Buka file `/app/src/main/java/com/example/data/PropertyRepository.kt`, lalu tambahkan pemanggilan API untuk melakukan sinkronisasi otomatis ke dalam database Room offline kita:

```kotlin
// Tambahkan ProperyApiService ke konstruktor atau buat instance-nya di dalam repository
class PropertyRepository(private val propertyDao: PropertyDao) {
    
    private val apiService = PropertyApiService.create()

    // Fungsi Sinkronisasi Data Unit Perumahan dari Web App
    suspend fun syncUnitsFromWeb(clusterName: String? = null) {
        try {
            // 1. Ambil data segar dari server Web App
            val remoteUnitsDto = apiService.getUnitsByCluster(clusterName)
            
            // 2. Map DTO menjadi Room Entity
            val newEntities = remoteUnitsDto.map { it.toEntity() }
            
            // 3. Masukkan ke database lokal Room (sebagai cache luring berkinerja tinggi)
            if (newEntities.isNotEmpty()) {
                // Opsional: Hapus unit lama berdasarkan klaster tersebut dari database jika ingin pembersihan total
                //Atau masukkan menggunakan strategi "REPLACE" / insertUnit langsung
                newEntities.forEach { unit ->
                    propertyDao.insertUnit(unit)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e // Lemparkan error agar UI tahu ada gangguan koneksi
        }
    }
    
    // ... (fungsi repositori bawaan lainnya tetap dipertahankan)
}
```

---

### Langkah 4: Hubungkan ke `PropertyViewModel`

Di `PropertyViewModel.kt`, Anda tinggal memanggil fungsi sinkronisasi tersebut di dalam coroutine scope, lengkap dengan state loading untuk kenyamanan pengguna:

```kotlin
class PropertyViewModel(private val repository: PropertyRepository) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError = _syncError.asStateFlow()

    fun syncData(clusterName: String? = null) {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            try {
                repository.syncUnitsFromWeb(clusterName)
            } catch (e: Exception) {
                _syncError.value = "Gagal memuat data dari Web App: ${e.localizedMessage}"
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
```

---

## 3. TIPS PENGUJIAN LOKAL

1. **Localhost Server**: Jika Web App Anda berjalan di komputer lokal (misal port `8000` / `3000`), gunakan URL `http://10.0.2.2:8000/` atau `http://10.0.2.2:3000/` di dalam Retrofit. IP `10.0.2.2` adalah alias khusus emulator Android untuk mengakses `localhost` mesin host Anda.
2. **Uji Mock Lebih Cepat**: Anda bisa menggunakan platform mock gratis seperti **Mockoon**, **Postman Mock Server**, atau **Nhost** untuk mensimulasikan respons JSON sebelum mengaktifkan API backend sebenarnya.
3. **Logcat Monitoring**: Gunakan `HttpLoggingInterceptor` (sudah tersedia di Gradle) untuk memantau log request dan response HTTP yang masuk secara real-time di Android Studio.

---
*Sukses untuk integrasinya besok! Selamat menembak datanya! 🚀*
