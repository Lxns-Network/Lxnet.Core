package net.lxns.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Price(
    val priceFireworkFire: Int =  50,
    val priceFireworkColorfulStar: Int = 50,
    val priceFireworkNewYear: Int = 50,
    val priceFireworkCreeper: Int = 50,
    val priceFireworkSmall: Int = 5,
    val priceLottery: Int = 233,
    val priceFireworkSparkle: Int = 40
)
