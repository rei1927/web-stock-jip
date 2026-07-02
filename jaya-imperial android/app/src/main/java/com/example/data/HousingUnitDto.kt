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
