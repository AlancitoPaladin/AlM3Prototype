package com.itsm.prototype.ui.seller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jakarta.inject.Inject

class SellerViewModel @Inject constructor() : ViewModel() {

    private val _myModels = MutableLiveData<List<ModelItem>>()
    val myModels: LiveData<List<ModelItem>> = _myModels

    private val _createModelState = MutableLiveData<CreateModelState>()
    val createModelState: LiveData<CreateModelState> = _createModelState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val modelName = MutableLiveData("")
    val modelDescription = MutableLiveData("")
    val modelPrice = MutableLiveData("")

}