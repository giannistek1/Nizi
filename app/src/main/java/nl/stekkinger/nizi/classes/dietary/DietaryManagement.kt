package nl.stekkinger.nizi.classes

import java.io.Serializable

data class DietaryManagement(
    var description: String,
    var amount: Int,
    var is_active: Boolean,
    var weightUnit: String, // ontbreekt

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
) : Serializable