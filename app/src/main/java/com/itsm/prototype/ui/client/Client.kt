package com.itsm.prototype.ui.client

import com.itsm.prototype.ui.user.User

class Client(
    name: String = "",
    email: String = "",
    password: String = "",
    private var shippingAddress: String = "",
    private var purchaseHistory: MutableList<String> = mutableListOf()
) : User(name, email, password) {

    override fun getUserType(): String = "CLIENT"

    fun getShippingAddress(): String = shippingAddress

    fun setShippingAddress(address: String) {
        this.shippingAddress = address
    }

    fun getPurchaseHistory(): List<String> = purchaseHistory

    fun addPurchase(modelId: String) {
        purchaseHistory.add(modelId)
    }

    fun getTotalPurchases(): Int = purchaseHistory.size

    fun buyModel(modelId: String, price: Double): Boolean {
        addPurchase(modelId)
        return true
    }
}