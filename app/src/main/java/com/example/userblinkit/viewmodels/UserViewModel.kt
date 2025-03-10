package com.example.userblinkit.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.userblinkit.Constants
import com.example.userblinkit.Utils
import com.example.userblinkit.api.ApiUtilities
import com.example.userblinkit.models.Product
import com.example.userblinkit.models.Users
import com.example.userblinkit.roomdb.CartProductDao
import com.example.userblinkit.roomdb.CartProducts
import com.example.userblinkit.roomdb.CartProductsDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application): AndroidViewModel(application)  {

    //initialization
    val sharedPreferences: SharedPreferences = application.getSharedPreferences("My_pref", MODE_PRIVATE)
    val cartProductDao: CartProductDao = CartProductsDatabase.getDatabaseInstance(application).cartProductsDao()
    private val _paymentStatus = MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    //Room DB
    suspend fun insertCartProduct(products: CartProducts) {
        cartProductDao.insertCartProduct(products)
    }

    fun getAll(): LiveData<List<CartProducts>> {
        return cartProductDao.getAllCartProduct()
    }

    suspend fun updateCartProduct(products: CartProducts) {
        cartProductDao.updateCartProduct(products)
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductDao.deleteCartProduct(productId)
    }

    //firebaseCall
    fun fetAllTheProducts(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        db.addValueEventListener(eventListener)
        awaitClose{ db.removeEventListener(eventListener) }
    }

    fun getCategoryProduct(category: String?): Flow<List<Product>> = callbackFlow {

        val db = FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${category}")

        val eventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)
                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }

        db.addValueEventListener(eventListener)
        awaitClose{ db.removeEventListener(eventListener) }
    }

    fun updateItemCount(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)
    }

    fun saveUserAddress(address: String) {
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUseId()).child("userAddress").setValue(address)

    }

    //sharePreferences
    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalCartItemCount(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount

    }

    fun saveAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    //retrofit
    suspend fun checkPayment(headers: Map<String, String>) {
        val res = ApiUtilities.statusApi.checkStatus(headers, Constants.MERCHANT_ID, Constants.merchantTransactionId)
        _paymentStatus.value = res.body() != null && res.body()!!.success
//        if (res.body() != null && res.body()!!.success) {
//            _paymentStatus.value = true
//        } else {
//            _paymentStatus.value = false
//        }
    }

}