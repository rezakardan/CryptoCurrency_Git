package com.example.cryptoapp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoapp.data.repository.MainRepository
import com.example.cryptoapp.ui.main.MainIntent
import com.example.cryptoapp.utils.base.BaseState
import com.example.cryptoapp.utils.network.ErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: MainRepository) : ViewModel() {


    val intentChannel = Channel<MainIntent>()
    private val _state = MutableStateFlow<BaseState>(BaseState.Idle)
    val state: StateFlow<BaseState> get() = _state

    init {
        handleIntents()
    }

    private fun handleIntents() = viewModelScope.launch {
        intentChannel.consumeAsFlow().collect { intent ->
            when (intent) {
                is MainIntent.CallCoinsList -> fetchingCoinsList()


                is MainIntent.CallSupportedCurrencies -> fetchingSupportedCurrencies()


                is MainIntent.CallCoinPrice -> fetchingCoinsPrice(intent.ids, intent.currencies)


                is MainIntent.CallCoinsMarkets->fetchingCoinsMarkets()

            }
        }
    }

    private fun fetchingCoinsMarkets()=viewModelScope.launch {
        _state.emit(BaseState.Loading)

        val response=repository.getCoinsMarkets()

        if (response.isSuccessful){

            response.body()?.let {


                BaseState.Main.LoadCoinsMarket(it).let {coinsMarket->


                    _state.emit(coinsMarket)



                }




            }


        }else{


            val error=ErrorResponse(response).generateResponse()

            _state.emit(error)


        }


    }

    private fun fetchingCoinsPrice(ids: String, currencies: String) = viewModelScope.launch {


        _state.emit(BaseState.Main.LoadingCoinPrice)


        val response = repository.getCoinPrice(ids, currencies)

        if (response.isSuccessful) {


            val responseData = response.body()

            responseData?.let {


                val coinsInfo = it[ids]?.get(currencies)

                coinsInfo?.let { info ->


                    BaseState.Main.LoadCoinsPrice(info).let { price ->

                        _state.emit(price)


                    }


                }


            }


        } else {
            val error = ErrorResponse(response).generateResponse()

            _state.emit(error)


        }


    }

    private fun fetchingSupportedCurrencies() = viewModelScope.launch {




        _state.emit(BaseState.Main.LoadingCoinPrice)




        val response = repository.getSupportedCurrencies()

        if (response.isSuccessful) {

            response.body()?.let {
                BaseState.Main.LoadSupportedCurrencies(it)
            }?.let {
                _state.emit(it)
            }


        } else {
            val error = ErrorResponse(response).generateResponse()

            _state.emit(error)


        }


    }

    private fun fetchingCoinsList() = viewModelScope.launch {
        val response = repository.getCoinsList()
        if (response.isSuccessful) {
            response.body()?.let { BaseState.Main.LoadCoinsList(it) }?.let { _state.emit(it) }
        } else {
            val error = ErrorResponse(response).generateResponse()
            _state.emit(error)
        }
    }

}