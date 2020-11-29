package nl.stekkinger.nizi.classes.weight_unit

import java.io.Serializable

data class WeightUnit (
    val id: Int,
    val unit: String,
    val short: String,

    // Unimportant
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable