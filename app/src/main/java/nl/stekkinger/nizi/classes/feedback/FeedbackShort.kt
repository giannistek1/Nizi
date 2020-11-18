package nl.stekkinger.nizi.classes.feedback

import java.util.*

data class FeedbackShort(
    val id: Int,
    val title: String,
    val comment: String,
    val date: Date,
    val is_read: Boolean,
    val patient: Int,
    val doctor: Int,

    // Unimportant stuff
    val created_at: String,
    val updated_at: String
)