package nl.stekkinger.nizi.classes.diary

import java.io.Serializable

class MyFood(
    val id: Int,
    val food: Int,
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable