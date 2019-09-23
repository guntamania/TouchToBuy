package com.guntamania.touchtobuy.data

import java.io.Serializable

data class OrderData (
    var success : Boolean,
    var id: Long,
    var rate: Double,
    var amount: Double,
    var orderType: String,
    var pair: String,
    var createdAt: String
) : Serializable