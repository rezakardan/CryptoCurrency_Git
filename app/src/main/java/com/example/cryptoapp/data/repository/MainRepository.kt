package com.example.cryptoapp.data.repository

import com.example.cryptoapp.data.network.ApiServices
import com.example.cryptoapp.utils.PER_PAGE
import com.example.cryptoapp.utils.USD
import javax.inject.Inject

class MainRepository @Inject constructor(private val api: ApiServices) {


    suspend fun getCoinsList() = api.getCoinsList()


    suspend fun getSupportedCurrencies() = api.getSupportedCurrencies()


    suspend fun getCoinPrice(ids: String, currencies: String) = api.getCoinPrice(ids, currencies)


    suspend fun getCoinsMarkets(

    ) = api.getCoinsMarkets(USD, PER_PAGE, true)


}