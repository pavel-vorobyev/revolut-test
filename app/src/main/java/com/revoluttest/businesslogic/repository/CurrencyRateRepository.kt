package com.revoluttest.businesslogic.repository

import com.revoluttest.businesslogic.api.ApiHelper
import com.revoluttest.businesslogic.models.CurrencyRate
import io.reactivex.Single
import javax.inject.Inject

class CurrencyRateRepository
@Inject
constructor(private val apiHelper: ApiHelper) {

    fun getCurrencyRate(base: String): Single<CurrencyRate> {
        return apiHelper.service.getRates(base)
    }

}