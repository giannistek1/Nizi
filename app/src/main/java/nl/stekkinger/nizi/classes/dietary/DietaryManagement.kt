package nl.stekkinger.nizi.classes.dietary

import java.io.Serializable

data class DietaryManagement(
    var id: Int? = null,
    var dietary_restriction: Int,
    var amount: Int,
    var is_active: Boolean,
    var patient: Int,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable