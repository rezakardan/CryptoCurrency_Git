package com.example.cryptoapp.utils.base

import com.example.cryptoapp.data.model.detail.ResponseDetail
import com.example.cryptoapp.data.model.main.ResponseCoinsList
import com.example.cryptoapp.data.model.main.ResponseCoinsMarket
import com.example.cryptoapp.data.model.main.ResponseSupportedCurrencies

sealed class BaseState(val error: String? = null) {
    object Idle : BaseState()
    object Loading : BaseState()
    class Error(error: String?) : BaseState(error)

    sealed class Main : BaseState() {
        data class LoadCoinsList(val coinsList: ResponseCoinsList) : Main()

        data class LoadSupportedCurrencies(val supportedCoins:ResponseSupportedCurrencies):Main()

data class LoadCoinsPrice(val price:Double):Main()

        object LoadingCoinPrice:Main()


        data class LoadCoinsMarket(val coinsMarket:ResponseCoinsMarket):Main()



    }



    sealed class Detail:BaseState(){


        data class LoadDetail(val data:ResponseDetail):Detail()





    }


}