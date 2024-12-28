package com.example.cryptoapp.ui.main

sealed class MainIntent {

    object CallCoinsList:MainIntent()

    object CallSupportedCurrencies:MainIntent()


    data class CallCoinPrice(val ids: String,val currencies: String):MainIntent()


    object CallCoinsMarkets:MainIntent()








}