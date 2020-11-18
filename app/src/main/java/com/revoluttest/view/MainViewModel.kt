package com.revoluttest.view

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.revoluttest.businesslogic.models.Currency
import com.revoluttest.businesslogic.models.CurrencyRate
import com.revoluttest.businesslogic.repository.CurrencyRateRepository
import com.revoluttest.util.CurrencyUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import rx.Observable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class MainViewModel
@ViewModelInject
constructor(private val currencyRateRepository: CurrencyRateRepository) : ViewModel() {

    val viewState = MutableLiveData<ViewState>()

    private val compositeDisposable = CompositeDisposable()

    private var currencyRate: CurrencyRate? = null
    private var currencies: MutableList<Currency> = mutableListOf()
    var firstResponder: Currency

    init {
        val defaultFirstResponderISO = "EUR"
        val defaultFirstResponderFlag = CurrencyUtil.getFlag(defaultFirstResponderISO)
        val defaultFirstResponderName = CurrencyUtil.getName(defaultFirstResponderISO)
        val defaultFirstResponderValue = BigDecimal(100)

        firstResponder = Currency(
            defaultFirstResponderFlag, defaultFirstResponderName,
            defaultFirstResponderISO, defaultFirstResponderValue
        )

        Observable.interval(1, TimeUnit.SECONDS)
            .subscribe {
                getCurrencyRate()
            }
    }

    fun getCurrencyRate() {
        compositeDisposable.add(currencyRateRepository.getCurrencyRate(firstResponder.iso3)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { response ->
                    consumeResponse(response)
                },
                {
                    it.printStackTrace()
                    viewState.value = ViewState.Error
                }
            ))
    }

    fun onValueChanged(value: String) {
        firstResponder.value = try {
            value.toBigDecimal()
        } catch (e: Exception) {
            BigDecimal(0)
        }

        val currencies = currencies.map { currency ->
            if (currency.iso3 != firstResponder.iso3) {
                val rate = currencyRate?.rates?.get(currency.iso3) ?: BigDecimal(0)
                val nValue = firstResponder.value
                    .multiply(rate)
                    .setScale(2, RoundingMode.HALF_EVEN)

                Currency(currency.flag, currency.name, currency.iso3, nValue)
            } else {
                Currency(currency.flag, currency.name, currency.iso3, currency.value)
            }
        }.toMutableList()

        this.currencies = currencies
        viewState.value = ViewState.Result(currencies)
    }

    private fun consumeResponse(currencyRate: CurrencyRate) {
        this.currencyRate = currencyRate
        val currencies = currencyRate.rates.map { item ->
            val currencyName = CurrencyUtil.getName(item.key)
            val currencyFlag = CurrencyUtil.getFlag(item.key)
            val value = firstResponder.value
                .multiply(item.value)
                .setScale(2, RoundingMode.HALF_EVEN)
            Currency(currencyFlag, currencyName, item.key, value)
        }.toMutableList()
        currencies.add(0, firstResponder)

        this.currencies = currencies
        viewState.value = ViewState.Result(currencies)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    sealed class ViewState {
        data class Result(val currencies: MutableList<Currency>) : ViewState()
        object Error : ViewState()
    }

}
