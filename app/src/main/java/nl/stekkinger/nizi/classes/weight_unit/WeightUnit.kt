package nl.stekkinger.nizi.classes.weight_unit

import java.io.Serializable

data class WeightUnit (
    val id: Int,
    val unit: String,
    val short: String
) : Serializable