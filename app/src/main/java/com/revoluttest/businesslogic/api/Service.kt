package com.revoluttest.businesslogic.api

import com.revoluttest.businesslogic.models.CurrencyRate
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface Service {

    @GET("/api/android/latest")
    fun getRates(@Query("base") base: String): Single<CurrencyRate>

}