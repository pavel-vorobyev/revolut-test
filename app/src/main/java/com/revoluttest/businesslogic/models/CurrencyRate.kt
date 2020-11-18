package com.revoluttest.businesslogic.models

import java.math.BigDecimal

data class CurrencyRate(

    val baseCurrency: String,
    val rates: Map<String, BigDecimal>

)