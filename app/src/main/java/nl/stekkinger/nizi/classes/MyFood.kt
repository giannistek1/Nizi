package nl.stekkinger.nizi.classes

import java.io.Serializable

data class MyFood(
    val id: Int,
    val food: Int,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
) : Serializable