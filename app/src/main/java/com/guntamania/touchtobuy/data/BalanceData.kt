package com.guntamania.touchtobuy.data

import java.io.Serializable

data class BalanceData (
    var success: Boolean,
    var jpy: Double,
    var btc:Double,
    var jpyReserved: Double,
    var btcReserved: Double,
    var jpyLendInUse: Double,
    var btcLendInUse: Double,
    var jpyLent: Double,
    var btcLent: Double,
    var jpyDept: Double,
    var btcDept: Double
): Serializable