package com.example.cryptoapp.ui.main

import android.annotation.SuppressLint
import com.example.cryptoapp.viewmodel.MainViewModel
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cryptoapp.R
import com.example.cryptoapp.data.model.main.ResponseCoinsList
import com.example.cryptoapp.data.model.main.ResponseCoinsMarket
import com.example.cryptoapp.data.model.main.ResponseSupportedCurrencies
import com.example.cryptoapp.databinding.FragmentMainBinding
import com.example.cryptoapp.utils.base.BaseState
import com.example.cryptoapp.utils.isVisible
import com.example.cryptoapp.utils.network.NetworkChecker
import com.example.cryptoapp.utils.showSnackBar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding


    @Inject
    lateinit var coinsAdapter: CoinsAdapter

    //Other
    private val viewModel by viewModels<MainViewModel>()
    private var coinPriceId = ""
    private var supportedPriceName = ""


    @Inject
    lateinit var networkChecker: NetworkChecker


    var isNetworkAvailable = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        lifecycleScope.launch {
            networkChecker.checkNetwork().collect {

                isNetworkAvailable = it

            }

        }


        viewLifecycleOwner.lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.CREATED) {


                if (isNetworkAvailable) {


                    viewModel.intentChannel.send(MainIntent.CallCoinsList)


                    viewModel.intentChannel.send(MainIntent.CallSupportedCurrencies)


                    viewModel.intentChannel.send(MainIntent.CallCoinsMarkets)
                }


            }


        }
        handleStates()
    }

    private fun handleStates() {
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.state.collect { state ->
                        when (state) {
                            is BaseState.Idle -> {}
                            is BaseState.Loading -> coinsMarketLoading.isVisible=true
                            is BaseState.Error -> {
                                state.error?.let { root.showSnackBar(it) }
                            }

                            is BaseState.Main.LoadCoinsList -> initCoinsSpinner(state.coinsList)
                            is BaseState.Main.LoadSupportedCurrencies -> initCoinsSupported(state.supportedCoins)


                            is BaseState.Main.LoadCoinsPrice -> initCoinsPrice(state.price)

                            is BaseState.Main.LoadingCoinPrice -> {

                                binding.exchangeLoading.isVisible(true, binding.exchangePriceTxt)


                            }

                            is BaseState.Main.LoadCoinsMarket -> initCoinsMarket(state.coinsMarket)


                            else->{}
                        }
                    }
                }
            }
        }
    }

    private fun initCoinsMarket(coinsMarket: MutableList<ResponseCoinsMarket.ResponseCoinsMarketItem>) {



        binding.coinsMarketLoading.isVisible=false


        coinsAdapter.setData(coinsMarket)


        binding.coinsMarketList.adapter = coinsAdapter

        binding.coinsMarketList.layoutManager =
            GridLayoutManager(requireContext(), 2)

        coinsAdapter.setOnItemClickListener {


val directions=MainFragmentDirections.actionToDetail(it.id!!)

            findNavController().navigate(directions)

        }


    }

    private fun initCoinsPrice(price: Double) {


        binding.exchangeLoading.isVisible(false, binding.exchangePriceTxt)

        binding.exchangePriceTxt.text = price.toString()


    }

    private fun initCoinsSupported(data: ResponseSupportedCurrencies) {


        val coinsSupported = mutableListOf<String>()

        data.forEach {

            coinsSupported.add(it)


        }


        val supportedCoinsAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, coinsSupported)


        binding.toCoinAutoTxt.apply {


            setAdapter(supportedCoinsAdapter)

            setOnItemClickListener { _, _, position, _ ->


                supportedPriceName = data[position]

                if (coinPriceId.isNotEmpty()) {

                    callCoinsPriceApi()
                }


            }


        }


    }

    private fun initCoinsSpinner(data: MutableList<ResponseCoinsList.ResponseCoinsListItem>) {
        //Coins name
        val coinsName = mutableListOf<String>()
        data.forEach { coinsName.add(it.name) }
        //Adapter
        val coinsAdapter =
            ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, coinsName)
        //Update view
        binding.fromCoinAutoTxt.apply {
            setAdapter(coinsAdapter)
            setOnItemClickListener { _, _, position, _ ->
                coinPriceId = data[position].id
                //Call api


                if (supportedPriceName.isNotEmpty()) {


                    callCoinsPriceApi()


                }
            }
        }
    }


    private fun callCoinsPriceApi() {


        viewLifecycleOwner.lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.CREATED) {

                if (isNetworkAvailable) {
                    viewModel.intentChannel.send(
                        MainIntent.CallCoinPrice(
                            coinPriceId,
                            supportedPriceName
                        )
                    )

                }


            }


        }


    }
}