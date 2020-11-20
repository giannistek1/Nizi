package nl.stekkinger.nizi.classes.feedback

import java.io.Serializable
import java.util.*

data class FeedbackShort(
    val id: Int = 0,
    val title: String,
    val comment: String,
    val date: String,
    val is_read: Boolean = false,
    val patient: Int,
    val doctor: Int,

    // Unimportant stuff
    val created_at: String? = null,
    val updated_at: String? = null
) : Serializable