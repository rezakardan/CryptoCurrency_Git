package com.example.cryptoapp.ui.detail


import android.graphics.Color
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cryptoapp.R
import com.example.cryptoapp.data.model.detail.ResponseDetail
import com.example.cryptoapp.databinding.FragmentDetailBinding
import com.example.cryptoapp.utils.base.BaseState
import com.example.cryptoapp.utils.doublePairs
import com.example.cryptoapp.utils.isVisible
import com.example.cryptoapp.utils.loadImage
import com.example.cryptoapp.utils.moneySeparating
import com.example.cryptoapp.utils.network.NetworkChecker
import com.example.cryptoapp.utils.openBrowser
import com.example.cryptoapp.utils.showSnackBar
import com.example.cryptoapp.utils.showTwoDecimal
import com.example.cryptoapp.viewmodel.DetailViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DetailFragment : Fragment() {

    lateinit var binding: FragmentDetailBinding


    val decimalFormatter by lazy { DecimalFormat("#,###.##") }



    //Other
    private val viewModel by viewModels<DetailViewModel>()
    private val args by navArgs<DetailFragmentArgs>()

    var isNetworkAvailable = false

    @Inject
    lateinit var networkChecker: NetworkChecker




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        lifecycleScope.launch {
            networkChecker.checkNetwork().collect {

                isNetworkAvailable = it

            }

        }

binding.backImg.setOnClickListener {
    findNavController().popBackStack()
}



        args.let {
            if (it.id.isNotEmpty()) {
                //Call api
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.CREATED) {
                        if (isNetworkAvailable) {
                            viewModel.detailIntentChannel.send(DetailIntent.CallCoinDetail(it.id))
                        }
                    }
                }
            }
        }
        //Load data
        handleStates()
    }

    private fun handleStates() {
        binding.apply {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.state.collect { state ->
                        when (state) {
                            is BaseState.Idle -> {}
                            is BaseState.Loading -> loading.isVisible = true
                            is BaseState.Error -> {
                                loading.isVisible(false, container)
                                state.error?.let { root.showSnackBar(it) }
                            }

                            is BaseState.Detail.LoadDetail -> initDetailViews(state.data)
                            else -> {}
                        }
                    }
                }
            }
        }
    }


    private fun initDetailViews(detail: ResponseDetail) {
        binding.apply {
            loading.isVisible(false, container)

            // Text
            nameTxt.text = "${detail.name} (${detail.symbol})"
            genesisDate.text = "${getString(R.string.genesisDate)} : ${detail.genesisDate}"

            // Categories (چک کردن برای خالی بودن)
            detail.categories?.takeIf { it.isNotEmpty() }?.let {
                categoryDate.text = "${getString(R.string.category)} : ${it[0]}"
            }

            // Desc
            detail.description?.let {
                val descFormatter = HtmlCompat.fromHtml(it.en ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT)
                descText.text = descFormatter
            }

            // Prices
            detail.marketData?.let { market ->
                // Current
                market.currentPrice?.let { priceTxt.text = "$${it.usd?.toInt()?.moneySeparating()}" }
                // High
                market.high24h?.let { highPriceTxt.text = "$${it.usd?.toInt()?.moneySeparating()}" }
                // Low
                market.low24h?.let { lowPriceTxt.text = "$${it.usd?.toInt()?.moneySeparating()}" }
                // Percent
                market.priceChangePercentage24h?.let { price1Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage7d?.let { price7Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage14d?.let { price14Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage30d?.let { price30Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage60d?.let { price60Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage200d?.let { price200Day.text = "${it.showTwoDecimal()}%" }
                market.priceChangePercentage1y?.let { price1Year.text = "${it.showTwoDecimal()}%" }
            }

            // Links
            detail.links?.let { links ->
                // Homepage
                links.homepage?.takeIf { it.isNotEmpty() }?.let { link ->
                    homePageTxt.apply {
                        text = link[0]
                        setOnClickListener { Uri.parse(link[0]).openBrowser(requireContext()) }
                    }
                } ?: run { homePageLay.isVisible = false }

                // Official Forum
                links.officialForumUrl?.takeIf { it.isNotEmpty() }?.let { link ->
                    officialFormTxt.apply {
                        text = link[0]
                        setOnClickListener { Uri.parse(link[0]).openBrowser(requireContext()) }
                    }
                } ?: run { officialFormLay.isVisible = false }

                // Github
                links.reposUrl?.github?.takeIf { it.isNotEmpty() }?.let { github ->
                    githubTxt.apply {
                        text = github[0]
                        setOnClickListener { Uri.parse(github[0]).openBrowser(requireContext()) }
                    }
                } ?: run { githubLay.isVisible = false }
            }

            // Image
            detail.image?.small?.let { iconImg.loadImage(it) }

            // Chart
            detail.marketData?.let { market ->
                coinChartBig.apply {
                    gradientFillColors = intArrayOf(chartColorAlpha(market.priceChangePercentage24h ?: 0.0), Color.TRANSPARENT)
                    lineColor = chartColorLine(market.priceChangePercentage24h)
                    animation.duration = 800
                    val chartData = market.sparkline7d?.price?.dropLast(100).doublePairs()
                    animate(chartData)
                }
            }
        }
    }

    private fun chartColorLine(percent: Double?): Int {
        return if (percent ?: 0.0 < 0)
            ContextCompat.getColor(requireContext(), R.color.goldenrod)
        else
            ContextCompat.getColor(requireContext(), R.color.turquoise)
    }

    private fun chartColorAlpha(percent: Double?): Int {
        return if (percent ?: 0.0 < 0)
            ContextCompat.getColor(requireContext(), R.color.goldenrodAlpha)
        else
            ContextCompat.getColor(requireContext(), R.color.turquoiseAlpha)
    }
    }