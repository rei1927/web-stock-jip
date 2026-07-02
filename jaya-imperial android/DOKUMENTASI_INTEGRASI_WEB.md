# Dokumentasi Integrasi API: Android x Web App (Laravel)
*Panduan teknis untuk tim Web App dalam menyediakan backend bagi aplikasi Jaya Imperial Park.*

---

## 1. Konfigurasi Umum
*   **Base URL:** `https://api.jayaimperial.com/` (Wajib HTTPS)
*   **Format Data:** JSON
*   **Autentikasi:** Laravel Sanctum (Bearer Token)
*   **Header Wajib:** 
    *   `Accept: application/json`
    *   `Authorization: Bearer {token}` (untuk endpoint terproteksi)

---

## 2. Endpoint Autentikasi & FCM

### Login User
`POST /api/login`
*   **Request:** `{ "email": "...", "password": "..." }`
*   **Response:**
    ```json
    {
      "access_token": "1|abcde...",
      "token_type": "Bearer",
      "user": {
        "id": 1,
        "name": "Siska (Sales)",
        "email": "siska@example.com",
        "role": "Sales" 
      }
    }
    ```

### Update FCM Token
`POST /api/fcm-token` (Auth Required)
*   **Tujuan:** Menghubungkan User ID dengan Token HP untuk pengiriman Push Notif.
*   **Request:** `{ "fcm_token": "KODE_TOKEN_DARI_FIREBASE" }`

---

## 3. Manajemen Stok Unit Proyek

### List Semua Unit
`GET /api/units`
*   **Query Param (Opsional):** `?cluster=Imperial+Garden`
*   **Response:** Array of Object
    ```json
    [{
      "clusterName": "Imperial Jade",
      "block": "Blok B-02",
      "typeName": "Tipe 70/120",
      "price": 1500000000.0,
      "status": "Tersedia",
      "isSold": false,
      "buildingArea": 70,
      "landArea": 120,
      "bedrooms": 3,
      "bathrooms": 2,
      "notes": "Posisi Hook",
      "actionByUser": null,
      "actionUserLabel": null,
      "holdTimestamp": null
    }]
    ```

### Update Status Real-time (Hold/Release)
`POST /api/units/update-status` (Auth Required)
*   **Tujuan:** Sinkronisasi saat sales klik tombol "Hold" atau "Lepas Hold" di Android.
*   **Payload:**
    ```json
    {
      "clusterName": "Imperial Jade",
      "block": "Blok B-02",
      "status": "Hold", 
      "actionByUser": "siska",
      "actionUserLabel": "Siska (Sales)"
    }
    ```

---

## 4. Administrasi Penjualan (20 Kolom + Gimmick)

### Submit Penjualan (Final Approve)
`POST /api/units/submit-sold` (Auth Required)
*   **Tujuan:** Dikirim saat Sales Manager menyetujui (Approve) unit yang diajukan.
*   **Payload:**
    ```json
    {
      "unitId": 12,
      "namaLengkap": "Dimas Sungkono",
      "noKtpConsumer": "3174060505890008",
      "alamatKtp": "Jl. Lebak Bulus No. 67...",
      "alamatSurat": "SDA",
      "noTelpSeluler": "081317796313",
      "noNpwp": "72.573...",
      "noKk": "3174062...",
      "typeBlok": "Tipe 70 - B2",
      "cluster": "Jade",
      "tujuanPembelian": "Tempat Tinggal",
      "sumberDana": "Gaji",
      "sistemPembayaran": "KPR",
      "kprPersen": "18 x 10%",
      "hargaJual": 1241783685,
      "plafondKpr": 1117335317,
      "tandaJadi": 10000000,
      "tandaJadiDate": "03-05-2025",
      "uMuka": 114148369,
      "uMukaBln": "18",
      "email": "dimas@gmail.com",
      "gimmicks": ["AC 1/2 PK", "TV 32 Inch", "Voucher Belanja", "Kanopi"]
    }
    ```

---

## 5. Skenario Push Notification (FCM)
Mohon tim Web memicu notifikasi Firebase pada kondisi berikut:

1.  **Sales Melakukan Hold:** Kirim notifikasi ke **Manager**.
    *   *Title:* "Unit di-Hold"
    *   *Body:* "Sales {nama} telah melakukan HOLD pada {blok}."
2.  **Manager Melakukan Approve:** Kirim notifikasi ke **Sales** terkait.
    *   *Title:* "Penjualan Disetujui!"
    *   *Body:* "Unit {blok} telah resmi SOLD. Selamat!"
3.  **Hold Kadaluarsa (12 Jam):** Sistem Web (Cron Job) melepas unit dan kirim notifikasi ke **Sales**.
    *   *Title:* "Hold Expired"
    *   *Body:* "Unit {blok} telah dilepas otomatis karena melewati batas waktu."

---
*Dibuat untuk mempermudah integrasi Go Live Jaya Imperial Park.*
