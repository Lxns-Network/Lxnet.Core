package net.lxns.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Price(
    val priceFireworkFire: Int =  100,
    val priceFireworkColorfulStar: Int = 100,
    val priceFireworkNewYear: Int = 100,
    val priceFireworkCreeper: Int = 100,
    val priceFireworkSmall: Int = 5,
    val priceLottery: Int = 10000,
    val priceFireworkSparkle: Int = 40
)
