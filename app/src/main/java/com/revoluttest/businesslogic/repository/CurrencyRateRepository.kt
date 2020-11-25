package com.revoluttest.businesslogic.repository

import com.revoluttest.businesslogic.api.ApiHelper
import com.revoluttest.businesslogic.models.CurrencyRate
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CurrencyRateRepository
@Inject
constructor(private val apiHelper: ApiHelper) {

    fun getCurrencyRate(base: String): Single<CurrencyRate> {
        return apiHelper.service.getRates(base)
            .subscribeOn(Schedulers.io())
    }

}