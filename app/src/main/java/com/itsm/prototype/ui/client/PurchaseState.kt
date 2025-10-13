package com.itsm.prototype.ui.client

sealed class PurchaseState {
    data class Success(val message: String) : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}