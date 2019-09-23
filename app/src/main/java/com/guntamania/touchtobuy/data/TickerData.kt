package com.guntamania.touchtobuy.data

import java.io.Serializable

data class TickerData (
    var last: Double,
    var bid: Double,
    var ask: Double,
    var high: Double,
    var low: Double,
    var volume: Double,
    var timestamp: Long
) : Serializable