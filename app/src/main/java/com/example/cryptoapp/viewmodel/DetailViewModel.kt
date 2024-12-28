package com.example.cryptoapp.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptoapp.data.repository.DetailRepository
import com.example.cryptoapp.ui.detail.DetailIntent
import com.example.cryptoapp.utils.base.BaseState
import com.example.cryptoapp.utils.network.ErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(private val repository: DetailRepository) : ViewModel() {


    val detailIntentChannel=Channel<DetailIntent>()


    private val _state= MutableStateFlow<BaseState>(BaseState.Idle)

    val state:StateFlow<BaseState>get() = _state


    init {
        handleIntent()
    }

    private fun handleIntent()=viewModelScope.launch {


      detailIntentChannel.consumeAsFlow().collect{intent->


          when(intent){


           is   DetailIntent.CallCoinDetail->fetchingDetailCoins(intent.id)








          }







      }








    }

    private fun fetchingDetailCoins(id: String)=viewModelScope.launch {



        _state.emit(BaseState.Loading)



        val response=repository.getCoinDetail(id)

        if (response.isSuccessful){

            response.body()?.let {

                BaseState.Detail.LoadDetail(it).let {


                    _state.emit(it)



                }







            }





        }else{

            val error=ErrorResponse(response).generateResponse()

            _state.emit(error)


        }




    }


}