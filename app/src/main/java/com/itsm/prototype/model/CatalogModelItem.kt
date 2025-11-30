package com.itsm.prototype.model

data class CatalogModelItem (
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val price: Double,
    val rating: Double?,
    val category: String?
)