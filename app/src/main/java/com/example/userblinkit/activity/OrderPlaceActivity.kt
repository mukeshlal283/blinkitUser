package com.example.userblinkit.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userblinkit.Constants
import com.example.userblinkit.Utils
import com.example.userblinkit.adapters.AdapterCartProduct
import com.example.userblinkit.databinding.ActivityOrderPlaceBinding
import com.example.userblinkit.databinding.AddressLayoutBinding
import com.example.userblinkit.viewmodels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityOrderPlaceBinding
    private lateinit var adapterCartProduct: AdapterCartProduct
    private lateinit var b2BPGRequest: B2BPGRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        backToUserMainActivity()
        initializePhonePay()
//        initializeRazorpay()
        onPlaceOrderClicked()

    }

//    private fun initializeRazorpay() {
//        Checkout.preload(this)
//        val co = Checkout()
//
//        co.setKeyID("rzp_test_HNCPqVeLxrNamA")
//    }

    private fun initializePhonePay() {

        val data = JSONObject()
        PhonePe.init(this, PhonePeEnvironment.UAT_SIMULATOR, Constants.MERCHANT_ID, "")

        data.put("merchantId", Constants.MERCHANT_ID)
        data.put("merchantTransactionId", Constants.merchantTransactionId)
        data.put("amount", 200)
        data.put("mobileNumber", "8178619866")
        data.put("callbackUrl", "https://webhook.site/callback-url")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")

        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")

        data.put("deviceContext", deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY) + "###1";

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this) {status ->
                if (status) {
                    //payment work
//                    initPayment()
                    getPaymentView()
                } else {
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))
                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()

                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding) //hide alertDialog and use the value of addresslayout
                    }
                }
            }
        }
    }

//    private fun initPayment() {
//        val co = Checkout()
//
//        try {
//            val options = JSONObject()
//            options.put("name","Blinkit")
//            options.put("description","Demoing Charges")
//            //You can omit the image option to fetch the image from the Dashboard
//            options.put("image","http://example.com/image/rzp.jpg")
//            options.put("theme.color", "#3399cc");
//            options.put("currency","INR");
//            options.put("order_id", "order_DBJOWzybf0sJbb");
//            options.put("amount","50000")//pass amount in currency subunits
//
//            val retryObj = JSONObject();
//            retryObj.put("enabled", true);
//            retryObj.put("max_count", 4);
//            options.put("retry", retryObj);
//
//            val prefill = JSONObject()
//            prefill.put("email","mlal32790@gmail.com")
//            prefill.put("contact","8178619869")
//
//            options.put("prefill",prefill)
//            co.open(this, options)
//        }catch (e: Exception){
//            Utils.showToast(this,"${e.message}")
//            e.printStackTrace()
//        }
//    }

    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            checkStatus()
        }
    }

    private fun checkStatus() {
        val xVerify = sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID,
        )
        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect{status ->
                if (status) {
                    Utils.showToast(this@OrderPlaceActivity, "Payment done")
                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                    finish()
                } else {
                    Utils.showToast(this@OrderPlaceActivity, "Payment not done")
                }
            }
        }
    }

    private fun getPaymentView() {

        try {
            PhonePe.getImplicitIntent(this@OrderPlaceActivity, b2BPGRequest, "com.phonepe.simulator")
                .let {
                    phonePayView.launch(it)
                }
        }
        catch (e: PhonePeInitException) {
            Utils.showToast(this, e.message.toString())
        }

    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing....")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"

        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus() //save true in shared Preference
        }

        Utils.showToast(this, "Saved...")
        alertDialog.dismiss()
        Utils.hideDialog()

    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this, UsersMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this) {cartProductsList ->

            adapterCartProduct = AdapterCartProduct()
            binding.rvProductsItems.adapter = adapterCartProduct
            adapterCartProduct.differ.submitList(cartProductsList)

            var totalPrice = 0
            for (product in cartProductsList) {
                val price = product.productPrice?.substring(1)?.toInt() //remove "$" sign
                val itemCount = product.productCount!!
                totalPrice += (price?.times(itemCount)!!)
            }

            binding.tvSubTotal.text = totalPrice.toString()
            if(totalPrice < 200) {
                binding.tvDeliveryCharge.text = "$15"
                totalPrice += 15
            }
            binding.tvGrandTotal.text = totalPrice.toString()

        }
    }

//    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
//        Utils.showToast(this, "Payment Success.")
//    }
//
//    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
//        Utils.showToast(this, "$p1")
//    }
}