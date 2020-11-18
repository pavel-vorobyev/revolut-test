package com.revoluttest.businesslogic.models

import java.math.BigDecimal

data class Currency(

    val flag: String,
    val name: String,
    val iso3: String,
    var value: BigDecimal

)