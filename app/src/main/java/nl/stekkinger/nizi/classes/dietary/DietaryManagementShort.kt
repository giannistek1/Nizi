package nl.stekkinger.nizi.classes.dietary

import java.io.Serializable

// Used for POST, PUT or as child from another object
data class DietaryManagementShort(
    val id: Int? = null,
    val dietary_restriction: Int,
    var minimum: Int,
    var maximum: Int,
    var is_active: Boolean,
    val patient: Int,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable