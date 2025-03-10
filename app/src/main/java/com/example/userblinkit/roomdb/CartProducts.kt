package com.example.userblinkit.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartProducts")
data class CartProducts (
    @PrimaryKey
    val productId: String = "random",
    val productTitle: String? = null,
    val productQuantity: String? = null,
    val productPrice: String? = null,
    var productStock: Int? = null,
    var productCategory: String? = null,
    var productCount: Int? = null,
    var adminUid: String? = null,
    var productImage: String ?= null,
)