package com.example.userblinkit.models

import android.location.Address

data class Users(
    var uid: String? = null,
    val userPhoneNumber: String? = null,
    val userAddress: String? = null
)
