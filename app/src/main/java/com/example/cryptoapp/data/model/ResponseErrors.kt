package com.example.cryptoapp.data.model

import com.google.gson.annotations.SerializedName

data class ResponseErrors(
    @SerializedName("error")
    val error: String?
)