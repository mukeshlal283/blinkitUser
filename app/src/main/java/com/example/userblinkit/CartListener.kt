package com.example.userblinkit

interface CartListener {
    fun showCartLayout(itemCount: Int)

    fun savingCartItemCount(itemCount: Int)

}