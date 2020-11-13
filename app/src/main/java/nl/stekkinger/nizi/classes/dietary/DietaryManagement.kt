package nl.stekkinger.nizi.classes.dietary

import java.io.Serializable

data class DietaryManagement(
    var description: String,
    var amount: Int,
    var is_active: Boolean,
    var patient: Int,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
) : Serializable