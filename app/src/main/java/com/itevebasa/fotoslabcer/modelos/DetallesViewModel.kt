package com.itevebasa.fotoslabcer.modelos

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class DetallesViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _imageUris = savedStateHandle.getLiveData<MutableMap<String, Uri>>("imageUris", mutableMapOf())
    val imageUris: LiveData<MutableMap<String, Uri>> = _imageUris

    fun saveImageUri(key: String, uri: Uri) {
        val current = _imageUris.value ?: mutableMapOf()
        current[key] = uri
        _imageUris.value = current
    }

    fun getImageUri(key: String): Uri? {
        return _imageUris.value?.get(key)
    }

}